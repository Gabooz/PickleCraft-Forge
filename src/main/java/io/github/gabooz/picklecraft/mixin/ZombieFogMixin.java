package io.github.gabooz.picklecraft.mixin;

import io.github.gabooz.picklecraft.client.ZombieZoneClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
abstract class ZombieFogMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void picklecraft$zoneFog(CallbackInfoReturnable<FogData> callback) {
        if (!ZombieZoneClient.active()) return;
        var fog = callback.getReturnValue();
        fog.environmentalStart = Math.min(fog.environmentalStart, 1.5F);
        fog.environmentalEnd = Math.min(fog.environmentalEnd, 7F);
        fog.renderDistanceStart = 1.5F;
        fog.renderDistanceEnd = 7;
        fog.skyEnd = 7;
        fog.cloudEnd = 7;
        var level = Minecraft.getInstance().level;
        long time = Math.floorMod(level.getDefaultClockTime(), 24000);
        float night = Math.clamp(Math.min(time - 12000, 24000 - time) / 1500F, 0, 1);
        float light = 1 - night * 0.86F;
        fog.color.set(0.27F * light, 0.30F * light, 0.26F * light, 1);
    }
}
