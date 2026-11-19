package io.github.gabooz.picklecraft.map;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class ZoneMapNetwork {
    private record OpenMap(long seed) {}
    private static final SimpleChannel CHANNEL = ChannelBuilder.named("picklecraft:zone_map")
        .networkProtocolVersion(1).simpleChannel();

    private ZoneMapNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(OpenMap.class, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((message, buffer) -> buffer.writeLong(message.seed()))
            .decoder(buffer -> new OpenMap(buffer.readLong()))
            .consumerMainThread((message, context) -> {
                var game = net.minecraft.client.Minecraft.getInstance();
                if (game.player != null && game.player.isCreative()) {
                    game.gui.setScreen(new io.github.gabooz.picklecraft.client.ZoneMapScreen(message.seed()));
                }
            }).add();
    }

    public static void open(ServerPlayer player, long seed) {
        CHANNEL.send(new OpenMap(seed), PacketDistributor.PLAYER.with(player));
    }
}
