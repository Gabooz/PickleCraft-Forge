package io.github.gabooz.picklecraft.map;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import io.github.gabooz.picklecraft.world.HexBiomeSource;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public final class ZoneMapNetwork {
    private record OpenMap(long seed) {}
    private record Teleport(int x, int z) {}
    private static final SimpleChannel CHANNEL = ChannelBuilder.named("picklecraft:zone_map")
        .networkProtocolVersion(2).simpleChannel();

    private ZoneMapNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(Teleport.class, NetworkDirection.PLAY_TO_SERVER)
            .encoder((message, buffer) -> { buffer.writeInt(message.x()); buffer.writeInt(message.z()); })
            .decoder(buffer -> new Teleport(buffer.readInt(), buffer.readInt()))
            .consumerMainThread((message, context) -> teleport(context.getSender(), message.x(), message.z())).add();
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

    public static void requestTeleport(int x, int z) {
        CHANNEL.send(new Teleport(x, z), PacketDistributor.SERVER.noArg());
    }

    /** The server checks permissions and terrain; the client never supplies a height. */
    public static boolean teleport(ServerPlayer player, int x, int z) {
        if (player == null || !player.isCreative()
                || !(player.level().getChunkSource().getGenerator().getBiomeSource()
                    instanceof HexBiomeSource)
                || Math.abs((long) x) > 29_999_900 || Math.abs((long) z) > 29_999_900) return false;
        var level = player.level();
        var column = new BlockPos(x, 0, z);
        if (!level.getWorldBorder().isWithinBounds(column)) return false;
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        // Arrive above the surface, including water or foliage, with room for the player's head.
        var feet = new BlockPos.MutableBlockPos(x, y, z);
        while (y < level.getMaxY() - 2) {
            feet.setY(y);
            var head = feet.above();
            if (level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                    && level.getBlockState(head).getCollisionShape(level, head).isEmpty()) break;
            y++;
        }
        player.stopRiding();
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
        return player.teleportTo(level, x + 0.5, y + 0.1, z + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot(), true);
    }

    public static void open(ServerPlayer player, long seed) {
        CHANNEL.send(new OpenMap(seed), PacketDistributor.PLAYER.with(player));
    }
}
