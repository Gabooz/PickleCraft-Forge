package io.github.gabooz.picklecraft.map;

import io.github.gabooz.picklecraft.world.HexBiomeSource;
import io.github.gabooz.picklecraft.zombies.ZombieZones;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class ZoneMapItem extends Item {
    public ZoneMapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isCreative()) return InteractionResult.FAIL;
        if (level instanceof ServerLevel server && player instanceof net.minecraft.server.level.ServerPlayer viewer) {
            if (!(server.getChunkSource().getGenerator().getBiomeSource() instanceof HexBiomeSource)) {
                viewer.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("message.picklecraft.zone_map_world"));
                return InteractionResult.FAIL;
            }
            ZoneMapNetwork.open(viewer, ZombieZones.seed(server));
        }
        return InteractionResult.SUCCESS;
    }
}
