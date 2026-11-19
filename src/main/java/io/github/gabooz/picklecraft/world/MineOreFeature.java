package io.github.gabooz.picklecraft.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.feature.*;

import java.util.function.Function;

/** Reuses vanilla cluster shapes, sizes, targets, and air-exposure rules. */
public record MineOreFeature(Holder<Feature> original) implements Feature {
    public static final MapCodec<MineOreFeature> CODEC = Feature.CODEC.fieldOf("original")
        .xmap(MineOreFeature::new, MineOreFeature::original);

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!(generator.getBiomeSource() instanceof HexBiomeSource)) return false;
        var sampler = level.getLevel().getChunkSource().randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        long seed = HexRegions.seed(sampler);
        var ore = ZoneMines.at(origin.getX(), origin.getZ(), seed);
        if (ore == ZoneMines.Ore.NONE) return false;
        if (!(original.value() instanceof AbstractOreFeature vanilla)
                || !(vanilla instanceof OreFeature || vanilla instanceof ScatteredOreFeature)) {
            throw new IllegalStateException("A mine deposit must wrap a vanilla ore feature");
        }
        var targets = vanilla.targetStates().stream().map(target -> new BlockReplacement(target.target(),
            ZoneMines.block(ore, BuiltInRegistries.BLOCK.getKey(target.state().getBlock()).getPath().startsWith("deepslate_"))))
            .toList();

        // Each instance belongs to one placement call. Parallel chunk generation shares no mutable ore selection.
        Feature deposit = vanilla instanceof ScatteredOreFeature
            ? new ScatteredOreFeature(targets, vanilla.size(), vanilla.discardChanceOnAirExposure()) {
                @Override
                public boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> blocks,
                        RandomSource random, BlockReplacement target, BlockPos.MutableBlockPos pos) {
                    return ZoneMines.at(pos.getX(), pos.getZ(), seed) == ore
                        && super.canPlaceOre(state, blocks, random, target, pos);
                }
            }
            : new OreFeature(targets, vanilla.size(), vanilla.discardChanceOnAirExposure()) {
                @Override
                public boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> blocks,
                        RandomSource random, BlockReplacement target, BlockPos.MutableBlockPos pos) {
                    return ZoneMines.at(pos.getX(), pos.getZ(), seed) == ore
                        && super.canPlaceOre(state, blocks, random, target, pos);
                }
            };
        return deposit.place(level, generator, random, origin);
    }
}
