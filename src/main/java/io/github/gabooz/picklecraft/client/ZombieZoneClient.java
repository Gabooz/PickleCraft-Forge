package io.github.gabooz.picklecraft.client;

import io.github.gabooz.picklecraft.zombies.ZombieContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.world.level.Level;

public final class ZombieZoneClient {
    private static ClientPacketListener connection;
    private static boolean active;
    private static net.minecraft.world.level.Level fireLevel;
    private static net.minecraft.core.BlockPos fireCenter = net.minecraft.core.BlockPos.ZERO;
    private static long fireTick = Long.MIN_VALUE;
    private static java.util.List<net.minecraft.core.BlockPos> fires = java.util.List.of();

    private ZombieZoneClient() {}

    public static void register() {
        EntityRenderers.register(ZombieContent.ZOMBIE.get(), ZombieRenderer::new);
    }

    public static void receive(boolean fog) {
        connection = Minecraft.getInstance().getConnection();
        active = fog;
        fireLevel = null;
    }

    public static boolean nearCampfire() {
        var game = Minecraft.getInstance();
        if (game.level == null || game.player == null) return false;
        // Cache the scan for ten ticks rather than scanning once per rendered frame.
        if (fireLevel != game.level || game.level.getGameTime() - fireTick >= 10 || game.level.getGameTime() < fireTick
                || fireCenter.distSqr(game.player.blockPosition()) > 49) {
            fireLevel = game.level;
            fireCenter = game.player.blockPosition();
            fireTick = game.level.getGameTime();
            fires = io.github.gabooz.picklecraft.zombies.ZoneLights.campfires(game.level, game.player.blockPosition(), 18);
        }
        return fires.stream().anyMatch(pos -> pos.distSqr(game.player.blockPosition()) <= 100);
    }

    public static boolean active() {
        var game = Minecraft.getInstance();
        return active && connection != null && connection == game.getConnection()
            && game.level != null && game.level.dimension().equals(Level.OVERWORLD);
    }
}
