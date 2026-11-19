package io.github.gabooz.picklecraft.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

/** Preserved first-generation highlands, intentionally not selected by the current biome source. */
public record LegacyHighlandsTerrain(DensityFunction input, DensityFunction temperature, DensityFunction ridges) implements DensityFunction {
    public static final MapCodec<LegacyHighlandsTerrain> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DensityFunction.CODEC.fieldOf("input").forGetter(LegacyHighlandsTerrain::input),
        DensityFunction.CODEC.fieldOf("temperature").forGetter(LegacyHighlandsTerrain::temperature),
        DensityFunction.CODEC.fieldOf("ridges").forGetter(LegacyHighlandsTerrain::ridges)
    ).apply(instance, LegacyHighlandsTerrain::new));

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        var base = input.compileSampler(context);
        var climate = temperature.compileSampler(context);
        var ridge = ridges.compileSampler(context);
        return new DensitySampler() {
            private volatile Long seed;
            private record Column(int x, int z, double height) {}
            private final ThreadLocal<Column> columnCache = new ThreadLocal<>();

            @Override
            public float sampleValue(SamplerContext sampling, int x, int y, int z) {
                return shape(sampling, x, y, z, base.sampleValue(sampling, x, y, z));
            }

            private float shape(SamplerContext sampling, int x, int y, int z, float original) {
                if (seed == null) seed = Double.doubleToLongBits(climate.sampleValue(sampling, 0, 0, 0));
                if (y < 48) return original;
                var column = columnCache.get();
                if (column == null || column.x() != x || column.z() != z) {
                    double mountains = HexRegions.mountainWeight(x, z, seed);
                    double valley = Math.clamp((Math.abs(ridge.sampleValue(sampling, x, 0, z)) - 0.025) / 0.10, 0, 1);
                    valley = valley * valley * (3 - 2 * valley);
                    double hills = HexRegions.noise(x / 790.0, z / 790.0, seed + 113) * 6;
                    double peaks = 38 + HexRegions.noise(x / 490.0, z / 490.0, seed + 111) * 22;
                    double height = 61 + valley * (9 + hills + mountains * peaks);
                    column = new Column(x, z, height);
                    columnCache.set(column);
                }
                double surface = (column.height() - y) / 60 + Math.clamp(original, -1F, 1F) * 0.045;
                double blend = Math.clamp((y - 48) / 16.0, 0, 1);
                return (float) (original * (1 - blend) + surface * blend);
            }

            @Override
            public void sampleVolume(SamplerContext sampling, DensityBuffer output, DensityVolume volume) {
                // Keep vanilla noise sampling batched instead of recomputing a cached volume for every voxel.
                base.sampleVolume(sampling, output, volume);
                int index = 0;
                for (int z = 0; z < volume.sizeZ(); z++) {
                    for (int x = 0; x < volume.sizeX(); x++) {
                        for (int y = 0; y < volume.sizeY(); y++) {
                            output.set(index, shape(sampling, volume.blockX(x), volume.blockY(y), volume.blockZ(z), output.get(index)));
                            index++;
                        }
                    }
                }
            }
        };
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        var newInput = rule.rewrite(input);
        var newTemperature = rule.rewrite(temperature);
        var newRidges = rule.rewrite(ridges);
        return newInput == input && newTemperature == temperature && newRidges == ridges
            ? this : new LegacyHighlandsTerrain(newInput, newTemperature, newRidges);
    }

    @Override
    public Interval range() {
        return Interval.of(Math.min(input.range().min(), -10F), Math.max(input.range().max(), 4F));
    }

    @Override
    public int domainAxes() {
        return ALL_AXES;
    }

    @Override
    public MapCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
