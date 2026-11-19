package io.github.gabooz.picklecraft.world;

import net.minecraft.util.RandomSource;

/** Continuous coastline and seeded island groups inside ocean hexes. */
public final class OceanTerrain {
    private OceanTerrain() {}

    public static double continent(double x, double z, long seed) {
        return 0.25 + HexRegions.noise(x / 6800, z / 6800, seed + 428)
            + 0.16 * HexRegions.noise(x / 1100, z / 1100, seed + 429);
    }

    public static boolean steep(double x, double z, long seed) {
        return HexRegions.noise(x / 510, z / 510, seed + 430) > 0.20;
    }

    public record Surface(double seaWeight, double height) {}

    public static Surface sample(double x, double z, long seed) {
        double coast = continent(x, z, seed);
        double width = steep(x, z, seed) ? 0.028 : 0.055;
        double sea = Math.clamp((width - coast) / (2 * width), 0, 1);
        sea = sea * sea * (3 - 2 * sea);
        double height = 46 + HexRegions.noise(x / 380, z / 380, seed + 431) * 5;
        if (sea == 0) return new Surface(0, height);
        var region = HexRegions.at(x, z, seed);
        if (region.kind() != HexRegions.Kind.ISLANDS && region.kind() != HexRegions.Kind.ARCHIPELAGO) {
            return new Surface(sea, height);
        }

        var random = RandomSource.create(region.seed());
        int count = region.kind() == HexRegions.Kind.ISLANDS ? 1 + random.nextInt(2) : 5 + random.nextInt(4);
        double cx = HexRegions.SIDE * Math.sqrt(3) * (region.q() + region.r() / 2.0);
        double cz = HexRegions.SIDE * 1.5 * region.r();
        double rotation = random.nextDouble() * Math.TAU;
        for (int i = 0; i < count; i++) {
            double angle = rotation + i * Math.TAU / count;
            double offset = count == 1 ? 0 : count <= 2 ? 365 : 440;
            double distance = Math.hypot(x - cx - Math.cos(angle) * offset, z - cz - Math.sin(angle) * offset);
            double radius = count <= 2 ? 260 + random.nextInt(75) : 110 + random.nextInt(45);
            double rise = Math.clamp(1 - distance / radius, 0, 1);
            height = Math.max(height, 46 + rise * (31 + random.nextInt(9)));
        }
        return new Surface(sea, height);
    }
}
