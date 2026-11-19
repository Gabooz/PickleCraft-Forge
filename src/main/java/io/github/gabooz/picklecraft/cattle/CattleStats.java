package io.github.gabooz.picklecraft.cattle;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import net.minecraft.util.RandomSource;

/** Stored genetics. The seven traits deliberately do not alter yields or growth yet. */
public final class CattleStats {
    public enum Stat {
        BEEF_YIELD, LEATHER_YIELD, BEEF_QUALITY, LEATHER_QUALITY, MILK_YIELD, GROWTH_SPEED, CONSTITUTION
    }

    public static final Codec<CattleStats> CODEC = Codec.unboundedMap(
        Codec.STRING.xmap(Stat::valueOf, Stat::name), Codec.intRange(0, 50))
        .xmap(CattleStats::new, stats -> stats.values);
    private final EnumMap<Stat, Integer> values = new EnumMap<>(Stat.class);

    public CattleStats() {
        for (var stat : Stat.values()) values.put(stat, 25);
    }

    private CattleStats(java.util.Map<Stat, Integer> values) {
        this();
        this.values.putAll(values);
    }

    public int get(Stat stat) { return values.get(stat); }

    public static CattleStats inherit(CattleStats mother, CattleStats father, RandomSource random) {
        var child = new CattleStats();
        for (var stat : Stat.values()) {
            int value = (mother.get(stat) + father.get(stat) + 1) / 2;
            if (random.nextInt(100) < 5) value += random.nextBoolean() ? 1 : -1;
            child.values.put(stat, Math.clamp(value, 0, 50));
        }
        return child;
    }
}
