package io.github.gabooz.picklecraft.mixin;

import io.github.gabooz.picklecraft.world.PickleWorldgen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(WorldCreationUiState.class)
abstract class WorldCreationUiStateMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Optional<ResourceKey<WorldPreset>> picklecraft$defaultPreset(Optional<ResourceKey<WorldPreset>> preset) {
        return preset.isEmpty() || preset.get().equals(WorldPresets.NORMAL)
            ? Optional.of(PickleWorldgen.PRESET) : preset;
    }
}
