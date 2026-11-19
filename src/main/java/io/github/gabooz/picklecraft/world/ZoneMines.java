package io.github.gabooz.picklecraft.world;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Each hex independently chooses no mine, iron, or gold in a 12:5:1 ratio. */
public final class ZoneMines {
    public enum Ore { NONE, IRON, GOLD }

    private ZoneMines() {}

    public static Ore ore(HexRegions.Region zone) {
        long value = zone.seed() ^ 0x6A09E667F3BCC909L;
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        int choice = (int) Long.remainderUnsigned(value ^ value >>> 31, 18);
        return choice < 12 ? Ore.NONE : choice < 17 ? Ore.IRON : Ore.GOLD;
    }

    public static Ore at(int x, int z, long worldSeed) {
        return ore(HexRegions.at(x, z, worldSeed));
    }

    public static BlockState block(Ore ore, boolean deepslate) {
        return switch (ore) {
            case IRON -> (deepslate ? Blocks.DEEPSLATE_IRON_ORE : Blocks.IRON_ORE).defaultBlockState();
            case GOLD -> (deepslate ? Blocks.DEEPSLATE_GOLD_ORE : Blocks.GOLD_ORE).defaultBlockState();
            case NONE -> throw new IllegalArgumentException("An ore-free zone has no ore block");
        };
    }
}
