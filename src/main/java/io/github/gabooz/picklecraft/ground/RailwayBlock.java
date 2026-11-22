package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public final class RailwayBlock extends Block {
    public static final BooleanProperty TIED = BooleanProperty.create("tied");
    private static final VoxelShape LENGTHWISE = box(5, 0, 0, 11, 3, 16);
    private static final VoxelShape CROSSWISE = box(0, 0, 5, 16, 3, 11);
    private final boolean sleeper;

    RailwayBlock(Properties properties, boolean sleeper) {
        super(properties);
        this.sleeper = sleeper;
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z)
            .setValue(TIED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_AXIS, TIED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var axis = context.getHorizontalDirection().getAxis();
        for (var direction : Direction.Plane.HORIZONTAL) {
            var adjacent = context.getLevel().getBlockState(context.getClickedPos().relative(direction));
            if (adjacent.is(this)) {
                axis = adjacent.getValue(BlockStateProperties.HORIZONTAL_AXIS);
                break;
            }
        }
        return oriented(axis);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90
            ? state.setValue(BlockStateProperties.HORIZONTAL_AXIS, across(state.getValue(BlockStateProperties.HORIZONTAL_AXIS)).getAxis())
            : state;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.Z) != sleeper
            ? LENGTHWISE : CROSSWISE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(asItem().getDefaultInstance());
    }

    BlockState oriented(Direction.Axis axis) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
    }

    private static Direction across(Direction.Axis axis) {
        return axis == Direction.Axis.Z ? Direction.EAST : Direction.SOUTH;
    }

    static InteractionResult interact(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var player = event.getEntity();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RailwayBlock track) || !GroundContent.isHammer(event.getItemStack())
                || !level.mayInteract(player, pos)) return InteractionResult.PASS;

        var axis = state.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        var otherHand = event.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var ties = player.getItemInHand(otherHand);
        if (player.isShiftKeyDown() || !ties.is(GroundContent.SLEEPER_ITEM.get())) {
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, track.oriented(across(axis).getAxis()));
                event.getItemStack().hurtAndBreak(1, player, event.getHand());
            }
            return InteractionResult.SUCCESS;
        }

        var cross = across(axis);
        var center = pos;
        if (!track.sleeper) {
            center = pos.relative(cross);
            if (!canTie(level, center, axis)) center = pos.relative(cross.getOpposite());
        }
        if (!canTie(level, center, axis) || !level.mayInteract(player, center)
                || !level.mayInteract(player, center.relative(cross))
                || !level.mayInteract(player, center.relative(cross.getOpposite()))) return InteractionResult.FAIL;

        if (!level.isClientSide()) {
            boolean placed = level.isEmptyBlock(center);
            var tied = GroundContent.SLEEPER.get().oriented(axis).setValue(TIED, true);
            if (level.setBlockAndUpdate(center, tied)) {
                if (placed) ties.consume(1, player);
                event.getItemStack().hurtAndBreak(1, player, event.getHand());
                level.playSound(null, center, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.45F, 1.4F);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean canTie(Level level, BlockPos center, Direction.Axis axis) {
        if (!level.hasChunkAt(center)) return false;
        var support = level.getBlockState(center);
        return (level.isEmptyBlock(center) || support.is(GroundContent.SLEEPER.get()) && !support.getValue(TIED))
            && sidesMatch(level, center, axis);
    }

    private static boolean sidesMatch(Level level, BlockPos center, Direction.Axis axis) {
        var cross = across(axis);
        for (var side : List.of(cross, cross.getOpposite())) {
            var pos = center.relative(side);
            if (!level.hasChunkAt(pos)) return false;
            var rail = level.getBlockState(pos);
            if (!rail.is(GroundContent.RAIL.get()) || rail.getValue(BlockStateProperties.HORIZONTAL_AXIS) != axis) return false;
        }
        return true;
    }

    public static boolean finished(Level level, BlockPos center, Direction.Axis axis) {
        if (!level.hasChunkAt(center)) return false;
        var sleeper = level.getBlockState(center);
        return sleeper.is(GroundContent.SLEEPER.get()) && sleeper.getValue(TIED)
            && sleeper.getValue(BlockStateProperties.HORIZONTAL_AXIS) == axis && sidesMatch(level, center, axis);
    }

    /** Loose ground gives way only when a train tests a finished section. */
    public static boolean bearLoad(ServerLevel level, BlockPos center, Direction.Axis axis) {
        boolean stable = true;
        var cross = across(axis);
        for (var pos : List.of(center.relative(cross), center.relative(cross.getOpposite()), center)) {
            var basePos = pos.below();
            var base = level.getBlockState(basePos);
            if (!(base.getBlock() instanceof FallingBlock) && !base.is(Blocks.POWDER_SNOW)
                    && base.isFaceSturdy(level, basePos, Direction.UP)) continue;
            if (base.getBlock() instanceof FallingBlock || base.is(Blocks.POWDER_SNOW)) {
                level.destroyBlock(basePos, true);
            }
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof RailwayBlock) {
                FallingBlockEntity.fall(level, pos, state.setValue(TIED, false));
            }
            stable = false;
        }
        return stable;
    }
}
