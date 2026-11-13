package io.github.gabooz.picklecraft.mixin;

import net.minecraft.world.level.levelgen.material.MaterialSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MaterialSystem.class)
abstract class MaterialSystemMixin {
    // Frozen oceans also build ice columns outside the biome decoration system.
    @Inject(method = "frozenOceanExtension", at = @At("HEAD"), cancellable = true)
    private void picklecraft$skipFrozenOceanExtension(CallbackInfo callback) {
        callback.cancel();
    }
}
