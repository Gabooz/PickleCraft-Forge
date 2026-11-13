package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public final class IronFurnaceBlock extends BaseEntityBlock {
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape CHAMBER = Shapes.or(
        box(0, 0, 0, 16, 2, 16), box(0, 2, 0, 4, 16, 16), box(12, 2, 0, 16, 16, 16),
        box(4, 2, 0, 12, 16, 4), box(4, 2, 12, 12, 16, 16));

    public IronFurnaceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TOP, false).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TOP, LIT, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(TOP) ? null : new IronFurnace(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TOP) ? CHAMBER : Shapes.block();
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel server) || state.getValue(TOP)) {
            return null;
        }
        return createTickerHelper(type, GroundContent.FURNACE_ENTITY.get(),
            (world, pos, block, furnace) -> IronFurnace.tick(server, pos, block, furnace));
    }

    public static InteractionResult carve(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var player = event.getEntity();
        if (!player.isShiftKeyDown() || !event.getItemStack().is(ItemTags.PICKAXES)
                || !level.getBlockState(pos).is(Blocks.COBBLESTONE)
                || !level.getBlockState(pos.above()).is(Blocks.COBBLESTONE)
                || !level.mayInteract(player, pos) || !level.mayInteract(player, pos.above())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            Direction front = event.getHitVec().getDirection();
            if (front.getAxis().isVertical()) front = player.getDirection().getOpposite();
            var base = GroundContent.IRON_FURNACE.get().defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, front);
            // Suppress neighbor updates until both halves exist.
            var upper = level.getBlockState(pos.above());
            if (level.setBlock(pos.above(), base.setValue(TOP, true), Block.UPDATE_CLIENTS)) {
                if (level.setBlock(pos, base, Block.UPDATE_ALL)) {
                    event.getItemStack().hurtAndBreak(1, player, event.getHand());
                } else {
                    level.setBlockAndUpdate(pos.above(), upper);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult interact(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        if (state.getValue(TOP) || !(level.getBlockEntity(pos) instanceof IronFurnace furnace)) {
            return InteractionResult.PASS;
        }
        var held = event.getItemStack();
        var player = event.getEntity();
        if (held.is(Items.COAL) || held.is(Items.CHARCOAL)) {
            if (event.getHitVec().getDirection() != state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                    || !furnace.canFuel()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                furnace.insertFuel(held);
                held.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty()) {
            if (level instanceof ServerLevel server) {
                furnace.retrieve(server, player, player.isShiftKeyDown());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor,
            Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            boolean top = state.getValue(TOP);
            var other = level.getBlockState(top ? pos.below() : pos.above());
            if (!other.is(this) || other.getValue(TOP) == top) {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(TOP)) {
            return List.of();
        }
        var drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(Items.COBBLESTONE, 2));
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof IronFurnace furnace) {
            drops.addAll(furnace.drops());
        }
        return drops;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT) || state.getValue(TOP)) {
            return;
        }
        level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 2.03, pos.getZ() + 0.5, 0, 0.03, 0);
        var front = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        level.addParticle(ParticleTypes.SMALL_FLAME, pos.getX() + 0.5 + front.getStepX() * 0.52,
            pos.getY() + 0.28, pos.getZ() + 0.5 + front.getStepZ() * 0.52, 0, random.nextDouble() * 0.03, 0);
    }
}
