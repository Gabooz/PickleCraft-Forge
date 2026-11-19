package io.github.gabooz.picklecraft.zombies;

import com.mojang.serialization.Codec;
import io.github.gabooz.picklecraft.ground.GroundStack;
import io.github.gabooz.picklecraft.ground.GroundStackBlock;
import io.github.gabooz.picklecraft.world.HexRegions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** One-time surface decoration also repairs explored chunks. Harvested bones never regenerate. */
public final class ZoneBones extends SavedData {
    public static final Codec<ZoneBones> CODEC = Codec.LONG.listOf().xmap(ZoneBones::new, data -> List.copyOf(data.checked));
    private static final SavedDataType<ZoneBones> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("picklecraft", "zone_bones"), ZoneBones::new, CODEC, null);
    private final Set<Long> checked = new HashSet<>();
    public ZoneBones() {}
    private ZoneBones(List<Long> chunks) { checked.addAll(chunks); }

    public static void nearby(ServerLevel level, long seed) {
        var data = level.getDataStorage().computeIfAbsent(TYPE);
        for (var player : level.players()) {
            var center = player.chunkPosition();
            for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                var chunk = new ChunkPos(center.x() + dx, center.z() + dz);
                if (data.checked.contains(chunk.pack()) || !level.hasChunk(chunk.x(), chunk.z())
                        || !level.areEntitiesActuallyLoadedAndTicking(chunk)) continue;
                decorate(level, seed, chunk);
                data.checked.add(chunk.pack());
                data.setDirty();
            }
        }
    }

    /** Roughly one bone per four zombie-zone chunks, with retries for grass and occupied surfaces. */
    public static boolean decorate(ServerLevel level, long seed, ChunkPos chunk) {
        var random = RandomSource.create(seed ^ chunk.x() * 0x9E3779B97F4A7C15L ^ chunk.z() * 0xC2B2AE3D27D4EB4FL ^ 0x72B01E5A48L);
        if (random.nextInt(4) != 0) return false;
        for (int attempt = 0; attempt < 24; attempt++) {
            var pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(chunk.getMinBlockX() + random.nextInt(16), 0, chunk.getMinBlockZ() + random.nextInt(16)));
            if (!ZombieZones.assigned(HexRegions.at(pos.getX(), pos.getZ(), seed))) continue;
            // Heightmaps include existing ground items: don't bury or replace them.
            if (level.getBlockEntity(pos.below()) instanceof GroundStack stack
                    && stack.items().stream().anyMatch(item -> item.is(Items.BONE))) return false;
            if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)
                    && GroundStackBlock.place(level, pos, Items.BONE.getDefaultInstance())) return true;
        }
        return false;
    }
}
