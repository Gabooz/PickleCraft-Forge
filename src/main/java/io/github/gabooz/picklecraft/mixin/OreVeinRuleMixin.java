package io.github.gabooz.picklecraft.mixin;

import net.minecraft.world.level.levelgen.material.rule.OreVeinRule;
import net.minecraft.world.level.levelgen.material.rule.RuleEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OreVeinRule.class)
abstract class OreVeinRuleMixin {
    // The vanilla Overworld's large iron and copper veins are material rules, separate from placed ores.
    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    private void picklecraft$skipVanillaVeins(CallbackInfoReturnable<RuleEvaluator> callback) {
        callback.setReturnValue((x, y, z) -> null);
    }
}
