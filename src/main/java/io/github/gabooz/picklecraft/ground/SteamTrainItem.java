package io.github.gabooz.picklecraft.ground;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

final class SteamTrainItem extends Item {
    SteamTrainItem(Properties properties) { super(properties); }

    @Override public InteractionResult useOn(UseOnContext context) {
        var track = context.getClickedPos();
        var level = context.getLevel();
        var player = context.getPlayer();
        if (player == null || !player.mayBuild() || !level.mayInteract(player, track)
                || !level.getBlockState(track).is(GroundContent.SLEEPER.get())) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        var heading = context.getHorizontalDirection();
        var train = SteamTrain.TYPE.get().create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (train == null) return InteractionResult.FAIL;
        train.align(heading, track);
        if (!train.routeFits(train.position()) || !server.addFreshEntity(train)) {
            ((ServerPlayer) player).sendSystemMessage(Component.translatable("message.picklecraft.train_track"), true);
            return InteractionResult.FAIL;
        }
        context.getItemInHand().consume(1, player);
        return InteractionResult.SUCCESS_SERVER;
    }
}
