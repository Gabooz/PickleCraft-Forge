package io.github.gabooz.picklecraft.mixin;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
abstract class NoiseBasedChunkGeneratorMixin {
    // Initial creature groups are written to proto-chunks before Forge's entity join event.
    @Inject(method = "spawnOriginalMobs", at = @At("HEAD"), cancellable = true)
    private void picklecraft$skipOverworldMobPopulation(WorldGenRegion region, CallbackInfo callback) {
        if (region.getLevel().dimension().equals(Level.OVERWORLD)) {
            callback.cancel();
        }
    }
}
