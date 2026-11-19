package io.github.gabooz.picklecraft.mixin;

import io.github.gabooz.picklecraft.ground.GroundContent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
abstract class TorchItemMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void picklecraft$placeTimedTorch(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> callback) {
        if ((Object) this == Items.TORCH) {
            callback.setReturnValue(GroundContent.TIMED_TORCH_ITEM.get().place(context));
        }
    }
}
