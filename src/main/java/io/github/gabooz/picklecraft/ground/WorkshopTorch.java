package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.*;
import java.util.List;

public final class WorkshopTorch extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WET = BlockStateProperties.WATERLOGGED;
    private static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    WorkshopTorch(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(LIT, true).setValue(WET, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, WET);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        boolean wet = level.getFluidState(pos).is(Fluids.WATER) || level.getFluidState(pos).is(Fluids.FLOWING_WATER);
        var base = defaultBlockState().setValue(WET, wet)
            .setValue(LIT, !wet && !context.getItemInHand().is(GroundContent.UNLIT_TORCH.get()));
        for (var direction : context.getNearestLookingDirections()) {
            if (direction == Direction.UP) continue;
            var state = base.setValue(FACING, direction.getOpposite());
            if (state.canSurvive(level, pos)) return state;
        }
        return null;
    }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        var support = pos.relative(direction.getOpposite());
        return direction != Direction.DOWN && level.getBlockState(support).isFaceSturdy(level, support, direction);
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block other,
            net.minecraft.world.level.redstone.Orientation orientation, boolean piston) {
        if (!level.isClientSide() && !state.canSurvive(level, pos)) level.destroyBlock(pos, true);
        if (state.getValue(WET)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.LivingEntity placer, ItemStack item) {
        if (state.getValue(LIT) && level.getBlockEntity(pos) instanceof TorchClock clock && !level.isClientSide()) {
            clock.ignite();
        }
    }
    @Override protected FluidState getFluidState(BlockState state) {
        return state.getValue(WET) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        if (state.getValue(WET) || !fluid.is(net.minecraft.tags.FluidTags.WATER)) return false;
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(LIT, false).setValue(WET, true), Block.UPDATE_ALL);
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return true;
    }
    @Override public boolean canPlaceLiquid(net.minecraft.world.entity.LivingEntity user, BlockGetter level, BlockPos pos,
            BlockState state, Fluid fluid) {
        return !state.getValue(WET) && (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER);
    }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of((state.getValue(LIT) ? Items.TORCH : GroundContent.UNLIT_TORCH.get()).getDefaultInstance());
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> Block.box(5, 3, 9, 11, 13, 16);
            case SOUTH -> Block.box(5, 3, 0, 11, 13, 7);
            case WEST -> Block.box(9, 3, 5, 16, 13, 11);
            case EAST -> Block.box(0, 3, 5, 7, 13, 11);
            default -> Block.box(6, 0, 6, 10, 10, 10);
        };
    }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;
        var direction = state.getValue(FACING);
        double x = pos.getX() + 0.5 - direction.getStepX() * 0.23;
        double z = pos.getZ() + 0.5 - direction.getStepZ() * 0.23;
        double y = pos.getY() + (direction == Direction.UP ? 0.7 : 0.88);
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }
    @Override public TorchClock newBlockEntity(BlockPos pos, BlockState state) { return new TorchClock(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() || !state.getValue(LIT) ? null
            : createTickerHelper(type, GroundContent.TORCH_CLOCK.get(), (world, pos, current, clock) -> {
                if (world.getGameTime() % 20 == 0) clock.update((ServerLevel) world);
            });
    }
}
