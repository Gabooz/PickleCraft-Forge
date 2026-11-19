package io.github.gabooz.picklecraft.zombies;

import io.github.gabooz.picklecraft.world.*;
import io.github.gabooz.picklecraft.ground.GroundStackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;

import java.util.HashSet;
import java.util.Set;

public final class ZombieZones {
    private ZombieZones() {}

    public static boolean assigned(HexRegions.Region region) {
        return region.kind() == HexRegions.Kind.GRASSLANDS
            && RandomSource.create(region.seed() ^ 0x7D30B18FE419L).nextInt(100) < 15;
    }

    public static long seed(ServerLevel level) {
        return HexRegions.seed(level.getChunkSource().randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED));
    }

    public static boolean night(Level level) {
        long time = Math.floorMod(level.getDefaultClockTime(), 24000);
        return time >= 13000 && time < 23000;
    }

    public static long dawn(Level level) {
        return Math.floorDiv(level.getDefaultClockTime() + 1000, 24000);
    }

    public static void register() {
        BiomeChallenges.register(new BiomeChallenges.Definition(
            Identifier.fromNamespaceAndPath("picklecraft", "zombies"), Set.of(HexRegions.Kind.GRASSLANDS), 1,
            (level, generator, random, origin, region) -> {
                if (random.nextInt(10) != 0) return false;
                var pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    origin.offset(random.nextInt(16), 0, random.nextInt(16)));
                return level.getBlockState(pos.below()).is(BlockTags.DIRT)
                    && GroundStackBlock.place(level, pos, Items.BONE.getDefaultInstance());
            }, mob -> mob instanceof ZoneZombie, ZombieZones::assigned));
        TickEvent.LevelTickEvent.Post.BUS.addListener(event -> {
            if (event.level() instanceof ServerLevel level && level.dimension().equals(Level.OVERWORLD)
                    && level.getChunkSource().getGenerator().getBiomeSource() instanceof HexBiomeSource
                    && level.getGameTime() % 20 == 0) tick(level);
        });
        EntityLeaveLevelEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ZoneZombie zombie && event.getLevel() instanceof ServerLevel level) {
                zombie.detached(level);
            }
        });
    }

    public static boolean allows(Mob mob) {
        if (!(mob instanceof ZoneZombie zombie) || !(mob.level() instanceof ServerLevel level)) return false;
        var zone = ZombieLedger.get(level).find(zombie.zoneQ(), zombie.zoneR());
        return zone != null && zone.alive(zombie.slot())
            && zone.souls.get(zombie.slot()).incarnation() == zombie.incarnation();
    }

    private static void tick(ServerLevel level) {
        long seed = seed(level);
        var ledger = ZombieLedger.get(level);
        ZoneBones.nearby(level, seed);
        if (!night(level)) {
            for (var zone : ledger.zones()) ledger.dawn(zone, dawn(level));
        }
        Set<String> present = new HashSet<>();
        for (var entity : level.getAllEntities()) {
            if (entity instanceof ZoneZombie zombie && zombie.isAlive()) {
                present.add(zombie.identity());
            }
        }
        Set<Long> visited = new HashSet<>();
        for (var player : level.players()) {
            var region = HexRegions.at(player.getX(), player.getZ(), seed);
            boolean active = assigned(region);
            var zone = active ? ledger.zone(region) : null;
            ZombieZoneNetwork.send(player, zone != null && zone.remaining() > 0);
            if (zone == null || zone.remaining() == 0 || !night(level) || level.getDifficulty() == Difficulty.PEACEFUL
                    || player.isSpectator() || player.isCreative()
                    || !visited.add(ZombieLedger.key(region.q(), region.r()))) continue;
            for (int slot = 0; slot < zone.souls.size(); slot++) {
                String identity = zone.q + "/" + zone.r + "/" + slot;
                if (!zone.alive(slot) || present.contains(identity)) continue;
                // Ordinary surface spawning: away from players, on solid ground, in loaded dark chunks.
                for (int attempt = 0; attempt < 12; attempt++) {
                    double angle = level.getRandom().nextDouble() * Math.TAU;
                    double radius = 28 + level.getRandom().nextInt(48);
                    int x = (int) Math.floor(player.getX() + Math.cos(angle) * radius);
                    int z = (int) Math.floor(player.getZ() + Math.sin(angle) * radius);
                    var pos = new BlockPos(x, 0, z);
                    if (!level.hasChunk(x >> 4, z >> 4) || !level.areEntitiesActuallyLoadedAndTicking(new net.minecraft.world.level.ChunkPos(x >> 4, z >> 4))) continue;
                    pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
                    var candidate = HexRegions.at(x, z, seed);
                    if (candidate.q() != region.q() || candidate.r() != region.r()
                            || level.getBrightness(LightLayer.BLOCK, pos) > 0
                            || !level.getBlockState(pos.below()).isSolidRender()
                            || level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 24, false) != null) continue;
                    var zombie = ZombieContent.ZOMBIE.get().create(level, EntitySpawnReason.NATURAL);
                    if (zombie == null) continue;
                    var soul = zone.souls.get(slot);
                    int incarnation = soul.incarnation() + 1;
                    zombie.assign(zone, slot, incarnation);
                    zombie.setPos(x + 0.5, pos.getY(), z + 0.5);
                    if (!level.noCollision(zombie) || level.containsAnyLiquid(zombie.getBoundingBox())) continue;
                    zone.souls.set(slot, new ZombieLedger.Soul(soul.variant(), incarnation, soul.health()));
                    ledger.setDirty();
                    if (level.addFreshEntity(zombie)) present.add(identity);
                    break;
                }
            }
        }
    }
}
