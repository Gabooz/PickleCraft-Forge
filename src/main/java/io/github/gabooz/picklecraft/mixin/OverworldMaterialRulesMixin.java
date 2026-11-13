package io.github.gabooz.picklecraft.mixin;

import net.minecraft.data.worldgen.material.OverworldMaterialRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OverworldMaterialRules.class)
abstract class OverworldMaterialRulesMixin {
    @Inject(method = "makeStateRule", at = @At("HEAD"), cancellable = true)
    private static void picklecraft$replaceFrozenSurface(Block block, CallbackInfoReturnable<MaterialRule> callback) {
        if (block == Blocks.SNOW_BLOCK || block == Blocks.POWDER_SNOW || block == Blocks.ICE || block == Blocks.PACKED_ICE) {
            callback.setReturnValue(MaterialRules.state(Blocks.STONE.defaultBlockState()));
        }
    }
}
