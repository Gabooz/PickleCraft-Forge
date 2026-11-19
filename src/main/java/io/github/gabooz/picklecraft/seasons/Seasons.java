package io.github.gabooz.picklecraft.seasons;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraftforge.event.TickEvent;

/** Calendar offset keeps creative season changes separate from animal and crop ages. */
public final class Seasons extends SavedData {
    public static final int DAYS_PER_SEASON = 10;
    public static final int DAYS_PER_YEAR = 40;
    public static final long DAY = 24000L;
    public enum Season { SPRING, SUMMER, AUTUMN, WINTER }
    public record Date(Season season, int day) {
        public String label() { return season.name().charAt(0) + season.name().substring(1).toLowerCase(java.util.Locale.ROOT) + " " + day; }
    }
    public static final Codec<Seasons> CODEC = Codec.LONG.xmap(Seasons::new, calendar -> calendar.offset);
    private static final SavedDataType<Seasons> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("picklecraft", "seasons"), Seasons::new, CODEC, null);
    private long offset;
    public Seasons() {}
    private Seasons(long offset) { this.offset = offset; }
    public static Seasons get(ServerLevel level) { return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE); }
    public long offset() { return offset; }
    public static Date at(long time, long offset) {
        int day = (int) Math.floorMod(Math.floorDiv(time, DAY) + offset, DAYS_PER_YEAR);
        return new Date(Season.values()[day / DAYS_PER_SEASON], day % DAYS_PER_SEASON + 1);
    }
    public static Date date(ServerLevel level) { return at(level.getServer().overworld().getDefaultClockTime(), get(level).offset); }
    public void select(ServerLevel level, Season season) {
        int current = (int) Math.floorMod(Math.floorDiv(level.getDefaultClockTime(), DAY) + offset, DAYS_PER_YEAR);
        offset += season.ordinal() * DAYS_PER_SEASON - current;
        setDirty();
    }
    public static void register() {
        TickEvent.LevelTickEvent.Post.BUS.addListener(event -> {
            if (event.level() instanceof ServerLevel level && level == level.getServer().overworld()
                    && level.getGameTime() % 20 == 0) {
                for (var player : level.getServer().getPlayerList().getPlayers()) {
                    io.github.gabooz.picklecraft.map.ZoneMapNetwork.calendar(player, get(level).offset);
                }
            }
        });
    }
}
