package io.github.gabooz.picklecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import java.util.function.Predicate;
import net.minecraft.world.level.chunk.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Register challenge definitions during mod setup, before worlds start generating.
 * Each hex chooses one definition deterministically, and invokes it once per chunk.
 * The empty registry deliberately leaves every region without a challenge.
 */
public final class BiomeChallenges {
    @FunctionalInterface
    public interface Generation {
        boolean generate(WorldGenLevel level, ChunkGenerator generator, RandomSource random,
            BlockPos chunkOrigin, HexRegions.Region region);
    }

    public record Definition(Identifier id, Set<HexRegions.Kind> biomes, int weight, Generation generation,
            Predicate<Mob> allowedMobs) {
        public Definition(Identifier id, Set<HexRegions.Kind> biomes, int weight, Generation generation) {
            this(id, biomes, weight, generation, mob -> false);
        }
        public Definition {
            java.util.Objects.requireNonNull(id);
            java.util.Objects.requireNonNull(generation);
            java.util.Objects.requireNonNull(allowedMobs);
            biomes = Set.copyOf(biomes);
            if (weight <= 0) throw new IllegalArgumentException("Challenge weight must be positive");
        }
    }

    private static volatile List<Definition> definitions = List.of();

    private BiomeChallenges() {}

    public static synchronized void register(Definition definition) {
        if (definitions.stream().anyMatch(existing -> existing.id().equals(definition.id()))) {
            throw new IllegalArgumentException("Duplicate biome challenge " + definition.id());
        }
        var updated = new ArrayList<>(definitions);
        updated.add(definition);
        updated.sort(java.util.Comparator.comparing(entry -> entry.id().toString()));
        definitions = List.copyOf(updated);
    }

    /** Future mob challenges can explicitly allow their own creatures in their region. */
    public static boolean allowsMob(Level level, Mob mob) {
        if (definitions.isEmpty() || !(level instanceof ServerLevel server)
                || !(server.getChunkSource().getGenerator().getBiomeSource() instanceof HexBiomeSource)) {
            return false;
        }
        var sampler = server.getChunkSource().randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        var region = HexRegions.at(mob.getX(), mob.getZ(), HexRegions.seed(sampler));
        return select(region).map(definition -> definition.allowedMobs().test(mob)).orElse(false);
    }

    public static Optional<Definition> select(HexRegions.Region region) {
        var eligible = definitions.stream().filter(entry -> entry.biomes().contains(region.kind())).toList();
        int total = eligible.stream().mapToInt(Definition::weight).sum();
        if (total == 0) return Optional.empty();
        long choice = Math.floorMod(region.seed(), (long) total);
        for (var entry : eligible) {
            choice -= entry.weight();
            if (choice < 0) return Optional.of(entry);
        }
        return Optional.empty();
    }
}
