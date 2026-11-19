package io.github.gabooz.picklecraft.mixin;

import net.minecraft.client.renderer.BiomeColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
abstract class SeasonColorsMixin {
    @Inject(method = {"getAverageGrassColor", "getAverageFoliageColor", "getAverageDryFoliageColor"}, at = @At("RETURN"), cancellable = true)
    private static void picklecraft$season(CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(io.github.gabooz.picklecraft.client.SeasonClient.tint(callback.getReturnValue()));
    }
}
