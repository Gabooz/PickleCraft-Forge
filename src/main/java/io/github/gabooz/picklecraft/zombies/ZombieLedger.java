package io.github.gabooz.picklecraft.zombies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import io.github.gabooz.picklecraft.world.HexRegions;
import net.minecraft.util.RandomSource;

import java.util.*;

/** The roster outlives entity instances. Unloading a zombie never resurrects a killed member. */
public final class ZombieLedger extends SavedData {
    public record Soul(int variant, int incarnation, float health) {
        public static final Codec<Soul> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("variant").forGetter(Soul::variant),
            Codec.INT.fieldOf("incarnation").forGetter(Soul::incarnation),
            Codec.FLOAT.fieldOf("health").forGetter(Soul::health)
        ).apply(i, Soul::new));
    }

    public static final class Zone {
        public final int q;
        public final int r;
        public final long seed;
        public final List<Soul> souls;
        public int dead;
        public long dawn;

        public static final Codec<Zone> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("q").forGetter(z -> z.q),
            Codec.INT.fieldOf("r").forGetter(z -> z.r),
            Codec.LONG.fieldOf("seed").forGetter(z -> z.seed),
            Soul.CODEC.listOf().fieldOf("souls").forGetter(z -> z.souls),
            Codec.INT.fieldOf("dead").forGetter(z -> z.dead),
            Codec.LONG.fieldOf("dawn").forGetter(z -> z.dawn)
        ).apply(i, Zone::new));

        private Zone(int q, int r, long seed, List<Soul> souls, int dead, long dawn) {
            this.q = q;
            this.r = r;
            this.seed = seed;
            this.souls = new ArrayList<>(souls);
            this.dead = dead;
            this.dawn = dawn;
        }

        public boolean alive(int slot) {
            return slot >= 0 && slot < souls.size() && (dead & 1 << slot) == 0;
        }

        public int remaining() {
            return souls.size() - Integer.bitCount(dead);
        }
    }

    public static final Codec<ZombieLedger> CODEC = Zone.CODEC.listOf()
        .xmap(ZombieLedger::new, ledger -> List.copyOf(ledger.zones.values()));
    public static final SavedDataType<ZombieLedger> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("picklecraft", "zombie_rosters"), ZombieLedger::new, CODEC, null);
    private final Map<Long, Zone> zones = new LinkedHashMap<>();

    public ZombieLedger() {}

    private ZombieLedger(List<Zone> records) {
        records.forEach(zone -> zones.put(key(zone.q, zone.r), zone));
    }

    public static ZombieLedger get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static long key(int q, int r) {
        return (long) q << 32 ^ Integer.toUnsignedLong(r);
    }

    public Zone find(int q, int r) {
        return zones.get(key(q, r));
    }

    public Collection<Zone> zones() {
        return List.copyOf(zones.values());
    }

    public Zone zone(HexRegions.Region region) {
        return zones.computeIfAbsent(key(region.q(), region.r()), unused -> {
            var random = RandomSource.create(region.seed() ^ 0x579ABA19137L);
            int count = 18 + random.nextInt(8);
            var members = new ArrayList<Soul>();
            for (int slot = 0; slot < count; slot++) {
                int variant = random.nextInt(3);
                members.add(new Soul(variant, 0, ZoneZombie.healthFor(variant)));
            }
            setDirty();
            return new Zone(region.q(), region.r(), region.seed(), members, 0, Long.MIN_VALUE);
        });
    }

    public void dawn(Zone zone, long dawn) {
        if (zone.dawn == dawn) return;
        zone.dawn = dawn;
        for (int slot = 0; slot < zone.souls.size(); slot++) {
            var soul = zone.souls.get(slot);
            zone.souls.set(slot, new Soul(soul.variant(), soul.incarnation(), ZoneZombie.healthFor(soul.variant())));
        }
        setDirty();
    }

    public void kill(Zone zone, int slot) {
        if (zone.alive(slot)) {
            zone.dead |= 1 << slot;
            setDirty();
        }
    }
}
