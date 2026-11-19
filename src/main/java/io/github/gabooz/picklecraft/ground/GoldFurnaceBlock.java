package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public final class GoldFurnaceBlock extends BaseEntityBlock {
    private static final VoxelShape GRATE = Shapes.or(
        box(0, 0, 0, 16, 2, 16), box(0, 14, 0, 16, 16, 16),
        box(0, 2, 0, 2, 14, 16), box(14, 2, 0, 16, 14, 16),
        box(2, 2, 1, 14, 3, 3), box(2, 13, 1, 14, 14, 3),
        box(3, 3, 1, 4, 13, 2), box(7.5, 3, 1, 8.5, 13, 2), box(12, 3, 1, 13, 13, 2));
    private static final java.util.Map<Direction, VoxelShape> GRATE_BY_FACING = Shapes.rotateHorizontal(GRATE);

    public GoldFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
            .setValue(BlockStateProperties.LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.LIT);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldFurnace(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return GRATE_BY_FACING.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel server
            ? createTickerHelper(type, GroundContent.GOLD_FURNACE_ENTITY.get(), (world, pos, block, furnace) -> furnace.tick(server))
            : null;
    }

    static InteractionResult assemble(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var player = event.getEntity();
        var hammer = event.getItemStack();
        if (!player.isShiftKeyDown() || !GroundContent.isHammer(hammer)
                || !level.getBlockState(pos).is(Blocks.IRON_BARS) || !level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }

        // Use the clicked side first, then check the other sides for a valid shell.
        var front = event.getHitVec().getDirection();
        if (!front.getAxis().isHorizontal() || !hasShell(level, pos, front)) {
            front = null;
            for (Direction candidate : Direction.Plane.HORIZONTAL) {
                if (hasShell(level, pos, candidate)) {
                    front = candidate;
                    break;
                }
            }
        }
        if (front == null) {
            return InteractionResult.PASS;
        }
        var center = pos.relative(front.getOpposite());
        for (var cell : shellCells(center)) {
            if (!level.mayInteract(player, cell)) {
                return InteractionResult.FAIL;
            }
        }
        var state = GroundContent.GOLD_FURNACE.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, front);
        if (!level.isClientSide() && level.setBlockAndUpdate(pos, state)) {
            shapeShell(level, pos, front);
            hammer.hurtAndBreak(1, player, event.getHand());
            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 0.7F);
        }
        return InteractionResult.SUCCESS;
    }

    static boolean hasShell(Level level, BlockPos grate, Direction front) {
        var center = grate.relative(front.getOpposite());
        for (var pos : shellCells(center)) {
            if (pos.equals(grate)) {
                continue;
            }
            if (!level.hasChunkAt(pos)) {
                return false;
            }
            boolean chimney = pos.equals(center) || pos.equals(center.above());
            var state = level.getBlockState(pos);
            boolean shell = state.is(Blocks.RED_SANDSTONE)
                || (state.is(GroundContent.GOLD_FURNACE_SHELL.get())
                    && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == front
                    && state.getValue(GoldFurnaceShellBlock.PART) == GoldFurnaceShellBlock.part(pos, center, front));
            if (chimney ? !state.isAir() : !shell) {
                return false;
            }
        }
        return true;
    }

    private static Iterable<BlockPos> shellCells(BlockPos center) {
        return BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1));
    }

    static void shapeShell(Level level, BlockPos grate, Direction front) {
        var center = grate.relative(front.getOpposite());
        var shell = GroundContent.GOLD_FURNACE_SHELL.get().defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, front);
        for (var pos : shellCells(center)) {
            if (!pos.equals(grate) && level.getBlockState(pos).is(Blocks.RED_SANDSTONE)) {
                level.setBlockAndUpdate(pos, shell.setValue(GoldFurnaceShellBlock.PART,
                    GoldFurnaceShellBlock.part(pos, center, front)));
            }
        }
    }

    static InteractionResult use(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof GoldFurnace furnace)) {
            return InteractionResult.PASS;
        }
        var held = event.getItemStack();
        if (event.getLevel() instanceof ServerLevel server
                && event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            if (held.is(Items.COAL) || held.is(Items.CHARCOAL)) {
                if (furnace.addFuel(held.getItem())) {
                    held.consume(1, player);
                    server.playSound(null, event.getPos(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.55F, 0.8F);
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(
                        "message.picklecraft.gold_furnace_fuel", furnace.fuelCount(), GoldFurnace.FUEL_REQUIRED));
                }
            } else if (GroundContent.isIgniter(held)) {
                if (furnace.ignite()) {
                    held.hurtAndBreak(1, player, event.getHand());
                    server.playSound(null, event.getPos(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.9F, 0.8F);
                } else {
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(
                        "message.picklecraft.gold_furnace_ignite"));
                }
            } else if (held.isEmpty() || held.is(GroundContent.IRON_TONGS.get())) {
                furnace.retrieve(server, player, held, event.getHand());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected List<net.minecraft.world.item.ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var drops = new ArrayList<net.minecraft.world.item.ItemStack>();
        drops.add(Items.IRON_BARS.getDefaultInstance());
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof GoldFurnace furnace) {
            drops.addAll(furnace.contents());
        }
        return drops;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!state.getValue(BlockStateProperties.LIT)) {
            return;
        }
        var front = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        var vent = pos.relative(front.getOpposite()).above(2);
        level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, vent.getX() + 0.5, vent.getY(),
            vent.getZ() + 0.5, 0, 0.045, 0);
        level.addParticle(ParticleTypes.SMALL_FLAME, pos.getX() + 0.5 + front.getStepX() * 0.51,
            pos.getY() + 0.28, pos.getZ() + 0.5 + front.getStepZ() * 0.51, 0, 0.026, 0);
        if (random.nextInt(12) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.8F, 0.7F, false);
        }
    }
}
