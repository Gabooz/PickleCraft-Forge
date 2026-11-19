package io.github.gabooz.picklecraft.client;

import io.github.gabooz.picklecraft.seasons.Seasons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/** Server-synchronized calendar and subtle vanilla-palette vegetation colors. */
public final class SeasonClient {
    private static ClientPacketListener connection;
    private static long offset;
    private static Seasons.Season last;
    private SeasonClient() {}
    public static void receive(long days) {
        var game = Minecraft.getInstance();
        connection = game.getConnection();
        offset = days;
        var current = date().season();
        if (current != last) { last = current; if (game.level != null) game.levelRenderer.invalidateCompiledGeometry(game.level, game.options, game.gameRenderer.mainCamera(), game.getBlockColors()); }
    }
    public static Seasons.Date date() {
        var game = Minecraft.getInstance();
        return Seasons.at(game.level == null ? 0 : game.level.getDefaultClockTime(),
            connection == game.getConnection() ? offset : 0);
    }
    public static int tint(int original) {
        var game = Minecraft.getInstance();
        if (game.level == null || !game.level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return original;
        int target = switch (date().season()) {
            case SPRING -> 0x65b94c;
            case SUMMER -> 0xb2b34e;
            case AUTUMN -> 0xcd7c38;
            case WINTER -> 0xadb39b;
        };
        float blend = switch (date().season()) { case SPRING -> 0.25F; case SUMMER -> 0.30F; case AUTUMN -> 0.78F; case WINTER -> 0.65F; };
        int result = 0;
        for (int shift : new int[] {16, 8, 0}) {
            int value = Math.round(((original >> shift) & 255) * (1 - blend) + ((target >> shift) & 255) * blend);
            result |= value << shift;
        }
        return result;
    }
}
