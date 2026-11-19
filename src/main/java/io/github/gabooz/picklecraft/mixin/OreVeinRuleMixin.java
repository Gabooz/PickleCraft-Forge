package io.github.gabooz.picklecraft.mixin;

import io.github.gabooz.picklecraft.world.HexRegions;
import io.github.gabooz.picklecraft.world.ZoneMines;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreVeinRule.class)
abstract class OreVeinRuleMixin {
    private static boolean picklecraft$mineWorld(MaterialRuleContext context) {
        var biomes = context.possibleBiomes();
        return biomes != null && biomes.stream().anyMatch(biome -> biome.unwrapKey().map(key -> {
            var id = key.identifier();
            return id.getNamespace().equals("picklecraft")
                && java.util.Set.of("grasslands", "mountains", "oak_forest").contains(id.getPath());
        }).orElse(false));
    }

    // Large veins are terrain material rules, separate from the placed ore clusters.
    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    private void picklecraft$skipOtherWorldVeins(MaterialRuleContext context, CallbackInfoReturnable<RuleEvaluator> callback) {
        if (!picklecraft$mineWorld(context)) {
            callback.setReturnValue((x, y, z) -> null);
        }
    }

    @Inject(method = "compile", at = @At("RETURN"), cancellable = true)
    private void picklecraft$mapMineVeins(MaterialRuleContext context, CallbackInfoReturnable<RuleEvaluator> callback) {
        if (!picklecraft$mineWorld(context)) return;
        var sampler = ((MaterialRuleContextAccessor) (Object) context).picklecraft$randomState()
            .createClimateSampler(SamplerContext.EMPTY_UNCACHED);
        long seed = HexRegions.seed(sampler);
        var original = callback.getReturnValue();
        callback.setReturnValue((x, y, z) -> {
            var state = original.tryApply(x, y, z);
            if (state == null) return null;
            if (!(state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE) || state.is(Blocks.RAW_IRON_BLOCK)
                    || state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE) || state.is(Blocks.RAW_COPPER_BLOCK))) {
                return state;
            }
            var ore = ZoneMines.at(x, z, seed);
            return ore == ZoneMines.Ore.NONE ? null : ZoneMines.block(ore, y < 0);
        });
    }
}
