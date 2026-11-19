package io.github.gabooz.picklecraft.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;
import java.util.IdentityHashMap;

/** Vanilla Overworld terrain splines, caves and jaggedness evaluated with mountain climate inputs. */
public record MountainShape(DensityFunction input) implements DensityFunction {
    public static final MapCodec<MountainShape> CODEC = DensityFunction.CODEC.fieldOf("input")
        .xmap(MountainShape::new, MountainShape::input);
    private static DensityFunction mountainClimate(DensityFunction input) {
        // Rewrite before the compiler inlines registry references, retaining every vanilla
        // terrain operation while selecting inland, low-erosion mountain terrain.
        var memo = new IdentityHashMap<DensityFunction, DensityFunction>();
        DfRewriteRule mountainClimate = new DfRewriteRule() {
            @Override public DensityFunction rewrite(DensityFunction function) {
                var cached = memo.get(function);
                if (cached != null) return cached;
                DensityFunction result;
                if (function instanceof DensityFunctions.HolderHolder reference
                        && reference.function().unwrapKey().isPresent()) {
                    String id = reference.function().unwrapKey().orElseThrow().identifier().toString();
                    result = switch (id) {
                        case "minecraft:overworld/continents" -> DensityFunctions.constant(0.65F);
                        case "minecraft:overworld/erosion" -> DensityFunctions.constant(-0.65F);
                        default -> function.rewriteChildren(this);
                    };
                } else result = function.rewriteChildren(this);
                memo.put(function, result);
                return result;
            }
        };
        return mountainClimate.rewrite(input);
    }
    @Override public DensitySampler compileSampler(CompileContext context) { return input.compileSampler(context); }
    @Override public DensityFunction rewriteChildren(DfRewriteRule rule) {
        var rewritten = rule.rewrite(mountainClimate(input));
        return rewritten == input ? this : new MountainShape(rewritten);
    }
    @Override public Interval range() { return input.range(); }
    @Override public int domainAxes() { return input.domainAxes(); }
    @Override public MapCodec<? extends DensityFunction> codec() { return CODEC; }
}
