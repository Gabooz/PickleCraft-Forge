package io.github.gabooz.picklecraft.world;

import net.minecraft.world.level.biome.Climate;

/** Pointy-topped hexagons with roughly 1,071-block sides and smoothly warped boundaries. */
public final class HexRegions {
    public static final double SCALE = 3.0 / 7.0;
    public static final double SIDE = 2500 * SCALE;
    private static final double ROOT_THREE = Math.sqrt(3);

    public enum Kind { GRASSLANDS, MOUNTAINS, OAK_FOREST, OCEAN, ISLANDS, ARCHIPELAGO }

    public record Region(int q, int r, Kind kind, long seed) {}

    private HexRegions() {}

    public static long seed(Climate.Sampler sampler) {
        return Double.doubleToLongBits(sampler.temperature().sampleValue(0, 0, 0));
    }

    public static Region at(double x, double z, long seed) {
        double warpedX = x + noise(x / (740 * SCALE), z / (740 * SCALE), seed + 11) * 230 * SCALE
            + noise(x / (210 * SCALE), z / (210 * SCALE), seed + 12) * 48 * SCALE;
        double warpedZ = z + noise(x / (740 * SCALE), z / (740 * SCALE), seed + 21) * 230 * SCALE
            + noise(x / (210 * SCALE), z / (210 * SCALE), seed + 22) * 48 * SCALE;
        double q = (ROOT_THREE / 3 * warpedX - warpedZ / 3) / SIDE;
        double r = warpedZ * 2 / (3 * SIDE);
        int rq = (int) Math.round(q);
        int rr = (int) Math.round(r);
        int rs = (int) Math.round(-q - r);
        double dq = Math.abs(rq - q);
        double dr = Math.abs(rr - r);
        double ds = Math.abs(rs + q + r);
        if (dq > dr && dq > ds) rq = -rr - rs;
        else if (dr > ds) rr = -rq - rs;
        return cell(rq, rr, seed);
    }

    public static Region cell(int q, int r, long seed) {
        double x = SIDE * ROOT_THREE * (q + r / 2.0);
        double z = SIDE * 1.5 * r;
        // Long coherent ridges group mountain cells into chains. A broader moisture
        // field groups forests, with grasslands a little more common than forest.
        Kind kind = inlandKind(x, z, seed);
        long cellSeed = hash(q, r, seed);
        if (OceanTerrain.continent(x, z, seed) < 0) {
            int islandRoll = (int) Math.floorMod(cellSeed, 20);
            kind = islandRoll == 0 ? Kind.ARCHIPELAGO : islandRoll <= 2 ? Kind.ISLANDS : Kind.OCEAN;
        }
        return new Region(q, r, kind, cellSeed);
    }

    private static Kind landKind(double ridge, double woodland) {
        return ridge < 0.15 ? Kind.MOUNTAINS : woodland > 0.08 ? Kind.OAK_FOREST : Kind.GRASSLANDS;
    }

    public static Kind inlandKind(double x, double z, long seed) {
        double ridge = Math.abs(noise(x / (17000 * SCALE), z / (17000 * SCALE), seed + 91)
            + noise(x / (6500 * SCALE), z / (6500 * SCALE), seed + 92) * 0.22);
        double woodland = noise(x / (10500 * SCALE), z / (10500 * SCALE), seed + 61)
            + noise(x / (4700 * SCALE), z / (4700 * SCALE), seed + 62) * 0.18;
        return landKind(ridge, woodland);
    }

    /** A continuous mountain mask prevents terrain cliffs at biome borders. */
    public static double mountainWeight(double x, double z, long seed) {
        var region = at(x, z, seed);
        double nearest = Double.POSITIVE_INFINITY;
        double mountains = 0;
        double weight = 0;
        // Blend the seven nearest cells over a few hundred blocks.
        for (int q = region.q - 1; q <= region.q + 1; q++) {
            for (int r = region.r - 1; r <= region.r + 1; r++) {
                double cx = SIDE * ROOT_THREE * (q + r / 2.0);
                double cz = SIDE * 1.5 * r;
                nearest = Math.min(nearest, Math.hypot(x - cx, z - cz));
            }
        }
        for (int q = region.q - 1; q <= region.q + 1; q++) {
            for (int r = region.r - 1; r <= region.r + 1; r++) {
                double cx = SIDE * ROOT_THREE * (q + r / 2.0);
                double cz = SIDE * 1.5 * r;
                double w = Math.exp(-(Math.hypot(x - cx, z - cz) - nearest) / (210 * SCALE));
                weight += w;
                if (cell(q, r, seed).kind == Kind.MOUNTAINS) mountains += w;
            }
        }
        return mountains / weight;
    }

    public static double noise(double x, double z, long seed) {
        int ix = (int) Math.floor(x);
        int iz = (int) Math.floor(z);
        double tx = smooth(x - ix);
        double tz = smooth(z - iz);
        return lerp(tz, lerp(tx, value(ix, iz, seed), value(ix + 1, iz, seed)),
            lerp(tx, value(ix, iz + 1, seed), value(ix + 1, iz + 1, seed)));
    }

    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double value(int x, int z, long seed) {
        return (hash(x, z, seed) >>> 11) * 0x1.0p-52 - 1;
    }

    private static long hash(int x, int z, long seed) {
        long value = seed ^ x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL;
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }
}
