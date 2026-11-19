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
        BiomeSource.CODEC.fieldOf("rivers").forGetter(source -> source.rivers)
    ).apply(instance, HexBiomeSource::new));

    private final Holder<Biome> grasslands;
    private final Holder<Biome> mountains;
    private final Holder<Biome> forest;
    private final Holder<Biome> river;
    private final BiomeSource rivers;

    public HexBiomeSource(Holder<Biome> grasslands, Holder<Biome> mountains, Holder<Biome> forest,
            Holder<Biome> river, BiomeSource rivers) {
        this.grasslands = grasslands;
        this.mountains = mountains;
        this.forest = forest;
        this.river = river;
        this.rivers = rivers;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(grasslands, mountains, forest, river);
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        long seed = HexRegions.seed(sampler);
        var riverResolver = rivers.createResolver(sampler);
        return (x, y, z) -> {
            var vanilla = riverResolver.getNoiseBiome(x, y, z);
            if (vanilla.is(Biomes.RIVER) || vanilla.is(Biomes.FROZEN_RIVER)) return river;
            return switch (HexRegions.at(x * 4.0, z * 4.0, seed).kind()) {
                case GRASSLANDS -> grasslands;
                case MOUNTAINS -> mountains;
                case OAK_FOREST -> forest;
            };
        };
    }
}
