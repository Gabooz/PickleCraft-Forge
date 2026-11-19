package io.github.gabooz.picklecraft.cattle;

import io.github.gabooz.picklecraft.world.HexRegions;
import io.github.gabooz.picklecraft.world.HexBiomeSource;
import io.github.gabooz.picklecraft.zombies.ZombieLedger;
import io.github.gabooz.picklecraft.zombies.ZombieZones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;

/** A grassland zone gets one permanent founding herd, including exactly one bull. */
public final class CattleHerds {
    private CattleHerds() {}

    public static boolean assigned(HexRegions.Region zone) {
        return zone.kind() == HexRegions.Kind.GRASSLANDS
            && RandomSource.create(zone.seed() ^ 0x61B47E1938AL).nextInt(100) < 5;
    }

    public static int size(HexRegions.Region zone) {
        return 2 + RandomSource.create(zone.seed() ^ 0x2C7ABA4948L).nextInt(2);
    }

    public static void register() {
        TickEvent.LevelTickEvent.Post.BUS.addListener(event -> {
            if (event.level() instanceof ServerLevel level && level.dimension().equals(Level.OVERWORLD)
                    && level.getChunkSource().getGenerator().getBiomeSource() instanceof HexBiomeSource
                    && level.getGameTime() % 100 == 0 && !ZombieZones.night(level)) tick(level);
        });
    }

    private static void tick(ServerLevel level) {
        var data = CattleData.get(level);
        long seed = ZombieZones.seed(level);
        for (var player : level.players()) {
            if (player.isSpectator()) continue;
            var zone = HexRegions.at(player.getX(), player.getZ(), seed);
            if (!assigned(zone)) continue;
            int count = size(zone);
            long key = ZombieLedger.key(zone.q(), zone.r());
            if (data.spawned(key) == (1 << count) - 1) continue;
            for (int attempt = 0; attempt < 20; attempt++) {
                double angle = level.getRandom().nextDouble() * Math.TAU;
                int radius = 26 + level.getRandom().nextInt(35);
                var anchor = BlockPos.containing(player.getX() + Math.cos(angle) * radius, 0,
                    player.getZ() + Math.sin(angle) * radius);
                if (!level.hasChunkAt(anchor)) continue;
                anchor = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, anchor);
                int previousMask = data.spawned(key);
                for (int slot = 0; slot < count; slot++) {
                    if ((data.spawned(key) & 1 << slot) != 0) continue;
                    var pos = anchor.offset((slot % 2) * 3, 0, (slot / 2) * 3);
                    if (!level.hasChunkAt(pos)) continue;
                    pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
                    var candidate = HexRegions.at(pos.getX(), pos.getZ(), seed);
                    if (candidate.q() != zone.q() || candidate.r() != zone.r()
                            || !level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)
                            || !level.isPositionEntityTicking(pos)) continue;
                    var animal = (slot == 0 ? CattleContent.BULL : CattleContent.COW).get().create(level, EntitySpawnReason.NATURAL);
                    if (animal == null) continue;
                    animal.configure();
                    animal.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    if (level.noCollision(animal) && !level.containsAnyLiquid(animal.getBoundingBox())
                            && level.addFreshEntity(animal)) data.markSpawned(key, slot);
                }
                if (data.spawned(key) != previousMask) break;
            }
        }
    }
}
