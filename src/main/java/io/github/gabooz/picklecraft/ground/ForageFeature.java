package io.github.gabooz.picklecraft.ground;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public record ForageFeature(boolean flowers) implements Feature {
    public static final MapCodec<ForageFeature> CODEC = Codec.BOOL.fieldOf("flowers").xmap(ForageFeature::new, ForageFeature::flowers);

    @Override
    public MapCodec<ForageFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (flowers) {
            return random.nextInt(20) == 0 && placeFlowers(level, random, origin);
        }
        // Roll once per chunk for each surface, then look for a suitable column.
        boolean grass = random.nextInt(2) == 0;
        boolean sand = random.nextInt(20) == 0;
        boolean placed = false;
        for (int attempt = 0; attempt < 32 && (grass || sand); attempt++) {
            var pos = surface(level, origin.offset(random.nextInt(16), 0, random.nextInt(16)));
            if (!canForage(level, pos)) {
                continue;
            }
            var soil = level.getBlockState(pos.below());
            boolean onGrass = grass && soil.is(Blocks.GRASS_BLOCK);
            boolean onSand = sand && soil.is(BlockTags.SAND);
            if (!onGrass && !onSand) {
                continue;
            }
            boolean stick = random.nextInt(nearOak(level, pos) ? 2 : 6) == 0;
            if (GroundStackBlock.place(level, pos, new ItemStack(stick ? Items.STICK : GroundContent.STONE.get()))) {
                placed = true;
                grass &= !onGrass;
                sand &= !onSand;
            }
        }
        return placed;
    }

    private static boolean placeFlowers(WorldGenLevel level, RandomSource random, BlockPos origin) {
        for (int attempt = 0; attempt < 24; attempt++) {
            var pos = surface(level, origin.offset(2 + random.nextInt(12), 0, 2 + random.nextInt(12)));
            if (placeFlower(level, pos)) {
                if (random.nextBoolean()) {
                    placeFlower(level, surface(level, pos.offset(random.nextBoolean() ? 2 : -2, 0, 0)));
                }
                return true;
            }
        }
        return false;
    }

    private static boolean placeFlower(WorldGenLevel level, BlockPos pos) {
        return canForage(level, pos) && level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)
            && level.setBlock(pos, GroundContent.PLUMARIA.get().defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static BlockPos surface(WorldGenLevel level, BlockPos pos) {
        // Use the current decorated surface. The worldgen surface can still point
        // into a canopy or to the height before the surface pass.
        var surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        while (surface.getY() > level.getMinY()) {
            var below = level.getBlockState(surface.below());
            if (!below.is(BlockTags.LOGS) && !below.is(BlockTags.LEAVES)) {
                break;
            }
            surface = surface.below();
        }
        return surface;
    }

    private static boolean canForage(WorldGenLevel level, BlockPos pos) {
        return level.getBiome(pos).is(BiomeTags.IS_OVERWORLD) && level.isEmptyBlock(pos)
            && GroundStackBlock.supported(level, pos);
    }

    private static boolean nearOak(WorldGenLevel level, BlockPos pos) {
        for (var nearby : BlockPos.betweenClosed(pos.offset(-5, -2, -5), pos.offset(5, 4, 5))) {
            if (level.getBlockState(nearby).is(Blocks.OAK_LOG)) {
                return true;
            }
        }
        return false;
    }
}
