package io.github.gabooz.picklecraft.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import java.util.List;

/** Vanilla wheat geometry, with persistent calendar growth instead of random-tick growth. */
public final class SeasonalWheatBlock extends CropBlock implements EntityBlock {
    public static final BooleanProperty DEAD = BooleanProperty.create("dead");
    public SeasonalWheatBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(DEAD, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); builder.add(DEAD);
    }
    @Override public WheatGrowth newBlockEntity(BlockPos pos, BlockState state) { return new WheatGrowth(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() || type != FarmingContent.GROWTH.get() ? null :
            (world, pos, current, entity) -> { if (world.getGameTime() % 20 == 0) ((WheatGrowth) entity).update((ServerLevel) world); };
    }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}
    @Override protected boolean isRandomlyTicking(BlockState state) { return false; }
    @Override public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) { return false; }
    @Override public void growCrops(Level level, BlockPos pos, BlockState state) {}
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(DEAD)) return List.of();
        if (!isMaxAge(state)) return List.of(new ItemStack(Items.WHEAT_SEEDS));
        var random = params.getLevel().getRandom();
        return List.of(new ItemStack(Items.WHEAT_SEEDS, 3 + random.nextInt(3)), new ItemStack(Items.WHEAT, 17 + random.nextInt(9)));
    }
}
