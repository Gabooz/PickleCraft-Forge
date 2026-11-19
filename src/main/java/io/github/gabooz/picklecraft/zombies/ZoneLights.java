package io.github.gabooz.picklecraft.zombies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import java.util.ArrayList;
import java.util.List;

/** Ordinary light has a small safe edge; campfires protect a larger circle. No chunks are loaded. */
public final class ZoneLights {
    public static final int LIGHT_LIMIT = 7;
    public static final int CAMPFIRE_RADIUS = 10;
    private ZoneLights() {}

    public static List<BlockPos> campfires(Level level, BlockPos center, int radius) {
        var result = new ArrayList<BlockPos>();
        for (var pos : BlockPos.betweenClosed(center.offset(-radius, -4, -radius), center.offset(radius, 4, radius))) {
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            if ((state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) && state.getValue(CampfireBlock.LIT)) {
                result.add(pos.immutable());
            }
        }
        return result;
    }

    public static int pressure(Level level, BlockPos pos, List<BlockPos> fires) {
        int light = Math.max(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.BLOCK, pos.above()));
        int pressure = Math.max(0, light - LIGHT_LIMIT);
        for (var fire : fires) {
            double distance = Math.sqrt(fire.distSqr(pos));
            if (distance < CAMPFIRE_RADIUS) pressure = Math.max(pressure, (int) Math.ceil((CAMPFIRE_RADIUS - distance) * 3));
        }
        return pressure;
    }
}
