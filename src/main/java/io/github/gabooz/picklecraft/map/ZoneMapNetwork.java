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
    private record Calendar(long offset) {}
    private record ChangeTime(int phase) {}
    private record ChangeSeason(int season) {}
    private static final SimpleChannel CHANNEL = ChannelBuilder.named("picklecraft:zone_map")
        .networkProtocolVersion(3).simpleChannel();

    private ZoneMapNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(Calendar.class, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((message, buffer) -> buffer.writeLong(message.offset()))
            .decoder(buffer -> new Calendar(buffer.readLong()))
            .consumerMainThread((message, context) -> io.github.gabooz.picklecraft.client.SeasonClient.receive(message.offset())).add();
        CHANNEL.messageBuilder(ChangeTime.class, NetworkDirection.PLAY_TO_SERVER)
            .encoder((message, buffer) -> buffer.writeInt(message.phase()))
            .decoder(buffer -> new ChangeTime(buffer.readInt()))
            .consumerMainThread((message, context) -> changeTime(context.getSender(), message.phase())).add();
        CHANNEL.messageBuilder(ChangeSeason.class, NetworkDirection.PLAY_TO_SERVER)
            .encoder((message, buffer) -> buffer.writeInt(message.season()))
            .decoder(buffer -> new ChangeSeason(buffer.readInt()))
            .consumerMainThread((message, context) -> changeSeason(context.getSender(), message.season())).add();
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

    public static void calendar(ServerPlayer player, long offset) {
        CHANNEL.send(new Calendar(offset), PacketDistributor.PLAYER.with(player));
    }
    public static void requestTime(int phase) { CHANNEL.send(new ChangeTime(phase), PacketDistributor.SERVER.noArg()); }
    public static void requestSeason(int season) { CHANNEL.send(new ChangeSeason(season), PacketDistributor.SERVER.noArg()); }
    public static boolean changeTime(ServerPlayer player, int phase) {
        if (player == null || !player.isCreative() || phase < 0 || phase >= 24000) return false;
        var level = player.level().getServer().overworld();
        level.dimensionType().defaultClock().ifPresent(clock -> level.clockManager().setTotalTicks(clock,
            Math.floorDiv(level.getDefaultClockTime(), 24000) * 24000 + phase));
        return true;
    }
    public static boolean changeSeason(ServerPlayer player, int season) {
        if (player == null || !player.isCreative() || season < 0 || season >= 4) return false;
        var level = player.level().getServer().overworld();
        var calendar = io.github.gabooz.picklecraft.seasons.Seasons.get(level);
        calendar.select(level, io.github.gabooz.picklecraft.seasons.Seasons.Season.values()[season]);
        for (var viewer : level.getServer().getPlayerList().getPlayers()) calendar(viewer, calendar.offset());
        return true;
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
