package io.github.gabooz.picklecraft.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
abstract class NightLightMixin {
    @Inject(method = "extract", at = @At("RETURN"))
    private void picklecraft$dimMoonlight(LightmapRenderState state, float partialTicks, CallbackInfo callback) {
        var level = Minecraft.getInstance().level;
        if (!state.needsUpdate || Minecraft.getInstance().player == null || level == null || !level.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        long time = Math.floorMod(level.getDefaultClockTime(), 24000L);
        float night = Math.min(1F, Math.max(0F, Math.min(time - 12000, 24000 - time) / 1700F));
        if (night == 0) {
            return;
        }
        int phase = level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE,
            Minecraft.getInstance().player.blockPosition()).index();
        float moon = 1F - Math.min(phase, 8 - phase) / 4F;
        float darkness = 1F - night + night * (0.13F + 0.039F * moon);
        state.skyFactor *= darkness;
        state.ambientColor = new Vector3f(state.ambientColor).mul(darkness);
        state.brightness *= 1F - night;
    }
}
