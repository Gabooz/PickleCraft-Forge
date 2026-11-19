package io.github.gabooz.picklecraft.cattle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

/** Remembers completed herd spawns and family relationships even while animals are unloaded. */
public final class CattleData extends SavedData {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    public record Child(UUID id, long born) {
        static final Codec<Child> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUID_CODEC.fieldOf("id").forGetter(Child::id), Codec.LONG.fieldOf("born").forGetter(Child::born)
        ).apply(i, Child::new));
    }

    public record Family(UUID mother, List<Child> children) {
        static final Codec<Family> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUID_CODEC.fieldOf("mother").forGetter(Family::mother),
            Child.CODEC.listOf().fieldOf("children").forGetter(Family::children)
        ).apply(i, Family::new));
        public Family { children = new ArrayList<>(children); }
    }

    public record Herd(long zone, int spawned) {
        static final Codec<Herd> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.LONG.fieldOf("zone").forGetter(Herd::zone), Codec.INT.fieldOf("spawned").forGetter(Herd::spawned)
        ).apply(i, Herd::new));
    }

    public static final Codec<CattleData> CODEC = RecordCodecBuilder.create(i -> i.group(
        Herd.CODEC.listOf().fieldOf("herds").forGetter(data -> data.herds.values().stream().toList()),
        Family.CODEC.listOf().fieldOf("families").forGetter(data -> data.families.values().stream().toList())
    ).apply(i, CattleData::new));
    public static final SavedDataType<CattleData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("picklecraft", "cattle"), CattleData::new, CODEC, null);
    private final Map<Long, Herd> herds = new HashMap<>();
    private final Map<UUID, Family> families = new HashMap<>();

    public CattleData() {}

    private CattleData(List<Herd> herds, List<Family> families) {
        herds.forEach(herd -> this.herds.put(herd.zone(), herd));
        families.forEach(family -> this.families.put(family.mother(), family));
    }

    public static CattleData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public int spawned(long zone) {
        return herds.getOrDefault(zone, new Herd(zone, 0)).spawned();
    }

    public void markSpawned(long zone, int slot) {
        herds.put(zone, new Herd(zone, spawned(zone) | 1 << slot));
        setDirty();
    }

    private void prune(long now) {
        boolean changed = families.values().removeIf(family -> {
            boolean removed = family.children().removeIf(child -> now - child.born() >= HerdCow.GROWTH_DAYS * 24000L);
            if (removed) setDirty();
            return family.children().isEmpty();
        });
        if (changed) setDirty();
    }

    public boolean hasCalves(UUID mother, long now) {
        prune(now);
        return families.containsKey(mother);
    }

    public void addCalf(UUID mother, UUID calf, long born) {
        var family = families.computeIfAbsent(mother, id -> new Family(id, List.of()));
        if (family.children().stream().noneMatch(child -> child.id().equals(calf))) {
            family.children().add(new Child(calf, born));
            setDirty();
        }
    }

    public boolean nurtured(UUID mother, UUID calf, long now) {
        prune(now);
        var family = families.get(mother);
        return family != null && !family.children().isEmpty() && family.children().getFirst().id().equals(calf);
    }

    public void finishCalf(UUID calf) {
        for (var family : families.values()) {
            if (family.children().removeIf(child -> child.id().equals(calf))) setDirty();
        }
        if (families.values().removeIf(family -> family.children().isEmpty())) setDirty();
    }
}
