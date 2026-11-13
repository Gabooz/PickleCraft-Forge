package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class PlumariaBlock extends BaseEntityBlock {
    public static final BooleanProperty BLOOMING = BooleanProperty.create("blooming");
    public static final long WEEK = 7 * 24000L;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);

    public PlumariaBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BLOOMING, true));
    }

    @Override
    public PlumariaGrowth newBlockEntity(BlockPos pos, BlockState state) {
        return new PlumariaGrowth(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BLOOMING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!state.getValue(BLOOMING)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof PlumariaGrowth growth) {
            growth.regrowAt = server.getDefaultClockTime() + WEEK;
            growth.setChanged();
            level.setBlockAndUpdate(pos, state.setValue(BLOOMING, false));
            level.scheduleTick(pos, this, 20);
            ItemStack blossoms = new ItemStack(GroundContent.BLOSSOMS.get());
            if (!player.addItem(blossoms)) {
                popResource(level, pos, blossoms);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BLOOMING) && level.getBlockEntity(pos) instanceof PlumariaGrowth growth) {
            if (level.getDefaultClockTime() >= growth.regrowAt) {
                level.setBlockAndUpdate(pos, state.setValue(BLOOMING, true));
            } else {
                level.scheduleTick(pos, this, 20);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide() && !GroundStackBlock.supported(level, pos)) {
            level.destroyBlock(pos, false);
        }
    }
}
