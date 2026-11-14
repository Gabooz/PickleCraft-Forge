package io.github.gabooz.picklecraft.ground;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Check inventories already in memory. Never request chunks or unpack unopened loot. */
public final class TongsContainers {
    private static final Set<LevelChunk> LOADED = Collections.newSetFromMap(new WeakHashMap<>());

    private TongsContainers() {}

    public static void register() {
        ChunkEvent.Load.BUS.addListener(event -> {
            if (event.getChunk() instanceof LevelChunk chunk && chunk.getLevel() instanceof ServerLevel) {
                LOADED.add(chunk);
            }
        });
        ChunkEvent.Unload.BUS.addListener(event -> LOADED.remove(event.getChunk()));
        TickEvent.LevelTickEvent.Post.BUS.addListener(TongsContainers::tick);
    }

    private static void tick(TickEvent.LevelTickEvent.Post event) {
        if (!(event.level() instanceof ServerLevel level) || level.getGameTime() % 20 != 0) {
            return;
        }
        for (var chunk : LOADED) {
            if (chunk.getLevel() != level) {
                continue;
            }
            for (var blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof Container container && level.shouldTickBlocksAt(blockEntity.getBlockPos())) {
                    cool(level, container, blockEntity.getBlockPos());
                }
            }
        }
        for (var entity : level.getAllEntities()) {
            if (entity instanceof Container container && level.isPositionEntityTicking(entity.blockPosition())) {
                cool(level, container, entity.blockPosition());
            }
        }
        for (var player : level.players()) {
            cool(level, player.getEnderChestInventory(), player.blockPosition());
        }
    }

    private static void cool(ServerLevel level, Container container, net.minecraft.core.BlockPos pos) {
        if (container instanceof RandomizableContainer chest && chest.getLootTable() != null) {
            return;
        }
        for (int i = 0; i < container.getContainerSize(); i++) {
            var tongs = container.getItem(i);
            if (!IronTongs.cool(tongs, level)) {
                continue;
            }
            var ingot = IronTongs.release(tongs);
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                if (!container.canPlaceItem(slot, ingot)) {
                    continue;
                }
                var current = container.getItem(slot);
                if (current.isEmpty()) {
                    container.setItem(slot, ingot.copy());
                    ingot.setCount(0);
                    break;
                }
                if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(current, ingot)
                        && current.getCount() < Math.min(current.getMaxStackSize(), container.getMaxStackSize())) {
                    current.grow(1);
                    ingot.setCount(0);
                    break;
                }
            }
            container.setChanged();
            if (!ingot.isEmpty()) {
                Block.popResource(level, pos.above(), ingot);
            }
        }
    }
}
