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

    private ZombieZoneClient() {}

    public static void register() {
        EntityRenderers.register(ZombieContent.ZOMBIE.get(), ZombieRenderer::new);
    }

    public static void receive(boolean fog) {
        connection = Minecraft.getInstance().getConnection();
        active = fog;
    }

    public static boolean active() {
        var game = Minecraft.getInstance();
        return active && connection != null && connection == game.getConnection()
            && game.level != null && game.level.dimension().equals(Level.OVERWORLD);
    }
}
