package io.github.gabooz.picklecraft.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(RecipeMap.class)
abstract class RecipeMapMixin {
    // Filter before Minecraft builds its lookup tables and sends recipe book entries to clients.
    @Redirect(method = "create", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;"))
    private static Stream<Holder.Reference<Recipe<?>>> picklecraft$removeVanillaCrafting(HolderLookup<Recipe<?>> recipes) {
        return recipes.listElements().filter(recipe ->
            !recipe.key().identifier().getNamespace().equals("minecraft") || recipe.value().getType() != RecipeType.CRAFTING);
    }
}
