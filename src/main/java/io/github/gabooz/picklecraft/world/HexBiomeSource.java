package io.github.gabooz.picklecraft.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;

import java.util.stream.Stream;

/** Leaves vanilla river climate sampling independent of the hex grid. */
public final class HexBiomeSource extends BiomeSource {
    public static final MapCodec<HexBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Biome.CODEC.fieldOf("grasslands").forGetter(source -> source.grasslands),
        Biome.CODEC.fieldOf("mountains").forGetter(source -> source.mountains),
        Biome.CODEC.fieldOf("oak_forest").forGetter(source -> source.forest),
        Biome.CODEC.fieldOf("river").forGetter(source -> source.river),
        BiomeSource.CODEC.fieldOf("rivers").forGetter(source -> source.rivers),
        Biome.CODEC.optionalFieldOf("ocean").forGetter(source -> source.ocean),
        Biome.CODEC.optionalFieldOf("beach").forGetter(source -> source.beach),
        Biome.CODEC.optionalFieldOf("cliff").forGetter(source -> source.cliff)
    ).apply(instance, HexBiomeSource::new));

    private final Holder<Biome> grasslands;
    private final Holder<Biome> mountains;
    private final Holder<Biome> forest;
    private final Holder<Biome> river;
    private final BiomeSource rivers;
    private final java.util.Optional<Holder<Biome>> ocean;
    private final java.util.Optional<Holder<Biome>> beach;
    private final java.util.Optional<Holder<Biome>> cliff;

    public HexBiomeSource(Holder<Biome> grasslands, Holder<Biome> mountains, Holder<Biome> forest,
            Holder<Biome> river, BiomeSource rivers, java.util.Optional<Holder<Biome>> ocean,
            java.util.Optional<Holder<Biome>> beach, java.util.Optional<Holder<Biome>> cliff) {
        this.grasslands = grasslands;
        this.mountains = mountains;
        this.forest = forest;
        this.river = river;
        this.rivers = rivers;
        this.ocean = ocean;
        this.beach = beach;
        this.cliff = cliff;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(Stream.of(grasslands, mountains, forest, river),
            Stream.of(ocean, beach, cliff).flatMap(java.util.Optional::stream));
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        long seed = HexRegions.seed(sampler);
        var riverResolver = rivers.createResolver(sampler);
        return (x, y, z) -> {
            double blockX = x * 4.0;
            double blockZ = z * 4.0;
            var region = HexRegions.at(blockX, blockZ, seed);
            double coast = OceanTerrain.continent(blockX, blockZ, seed);
            if (coast < 0) {
                double island = OceanTerrain.sample(blockX, blockZ, seed).height();
                if (island > 66) return grasslands;
                if (island >= 61) return beach.orElse(grasslands);
                return ocean.orElse(river);
            }
            boolean steep = OceanTerrain.steep(blockX, blockZ, seed);
            if (coast < (steep ? 0.046 : 0.092)) {
                return (steep ? cliff : beach).orElse(grasslands);
            }
            var vanilla = riverResolver.getNoiseBiome(x, y, z);
            if (vanilla.is(Biomes.RIVER) || vanilla.is(Biomes.FROZEN_RIVER)) return river;
            return switch (region.kind()) {
                case GRASSLANDS -> grasslands;
                case MOUNTAINS -> mountains;
                case OAK_FOREST -> forest;
                case OCEAN, ISLANDS, ARCHIPELAGO -> ocean.orElse(river);
            };
        };
    }
}
