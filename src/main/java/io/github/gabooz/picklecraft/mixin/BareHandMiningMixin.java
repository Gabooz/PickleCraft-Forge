package io.github.gabooz.picklecraft.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
abstract class BareHandMiningMixin {
    @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
    private void picklecraft$timeBareHandMining(BlockState state, Player player, BlockGetter level,
            BlockPos pos, CallbackInfoReturnable<Float> callback) {
        if (!player.getMainHandItem().isEmpty() || player.hasInfiniteMaterials()
                || state.is(BlockTags.LEAVES) || state.getDestroySpeed(level, pos) <= 0) {
            return;
        }

        int seconds = 60;
        if (state.is(BlockTags.LOGS)) {
            seconds = 120;
        } else if (state.is(BlockTags.DIRT)) {
            seconds = 30;
        } else if (state.is(BlockTags.SAND)) {
            seconds = 15;
        } else if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)) {
            seconds = 300;
        }

        // Both the server and client measure block breaking in progress per game tick.
        callback.setReturnValue(1.0F / (seconds * 20));
    }
}
