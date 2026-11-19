package io.github.gabooz.picklecraft.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Runs future challenge generation only for the PickleCraft biome source. */
public final class BiomeChallengeFeature implements Feature {
    public static final MapCodec<BiomeChallengeFeature> CODEC = MapCodec.unit(new BiomeChallengeFeature());

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!(generator.getBiomeSource() instanceof HexBiomeSource)) return false;
        var sampler = level.getLevel().getChunkSource().randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        var region = HexRegions.at(origin.getX() + 8, origin.getZ() + 8, HexRegions.seed(sampler));
        return BiomeChallenges.select(region)
            .map(definition -> definition.generation().generate(level, generator, random, origin, region)).orElse(false);
    }
}
