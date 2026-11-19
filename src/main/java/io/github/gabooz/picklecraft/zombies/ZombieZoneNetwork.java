package io.github.gabooz.picklecraft.zombies;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class ZombieZoneNetwork {
    private record ZoneState(boolean active) {}
    private static final SimpleChannel CHANNEL = ChannelBuilder.named("picklecraft:zombie_zones")
        .networkProtocolVersion(1).simpleChannel();

    private ZombieZoneNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(ZoneState.class, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((message, buffer) -> buffer.writeBoolean(message.active()))
            .decoder(buffer -> new ZoneState(buffer.readBoolean()))
            .consumerMainThread((message, context) ->
                io.github.gabooz.picklecraft.client.ZombieZoneClient.receive(message.active()))
            .add();
    }

    public static void send(ServerPlayer player, boolean active) {
        CHANNEL.send(new ZoneState(active), PacketDistributor.PLAYER.with(player));
    }
}
