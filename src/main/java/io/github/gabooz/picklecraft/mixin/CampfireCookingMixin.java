package io.github.gabooz.picklecraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
abstract class CampfireCookingMixin {
    @Shadow private int[] cookingProgress;
    @Shadow private int[] cookingTime;

    @Inject(method = "cookTick", at = @At("HEAD"), cancellable = true)
    private static void picklecraft$cook(ServerLevel level, BlockPos pos, BlockState state,
            CampfireBlockEntity fire, RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipes,
            CallbackInfo callback) {
        callback.cancel();
        var timers = (CampfireCookingMixin) (Object) fire;
        for (int slot = 0; slot < fire.getItems().size(); slot++) {
            var item = fire.getItems().get(slot);
            if (item.isEmpty() || timers.cookingTime[slot] < 0) continue;
            if (++timers.cookingProgress[slot] >= 20 * 60 * 5) {
                var input = new SingleRecipeInput(item);
                var cooked = recipes.getRecipeFor(input, level).map(recipe -> recipe.value().assemble(input));
                if (cooked.isPresent()) fire.getItems().set(slot, cooked.get());
                timers.cookingTime[slot] = -1;
                timers.cookingProgress[slot] = 0;
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
            fire.setChanged();
        }
    }
}
