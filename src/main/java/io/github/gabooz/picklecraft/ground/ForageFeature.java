package io.github.gabooz.picklecraft.ground;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
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
        boolean placed = false;
        for (int attempt = 0; attempt < 4; attempt++) {
            var pos = surface(level, origin.offset(random.nextInt(16), 0, random.nextInt(16)));
            if (!level.getBlockState(pos.below()).is(BlockTags.DIRT)) {
                continue;
            }
            boolean stick = random.nextInt(nearOak(level, pos) ? 2 : 30) == 0;
            if (stick || random.nextInt(3) == 0) {
                var item = stick ? Items.STICK : GroundContent.STONE.get();
                placed |= GroundStackBlock.place(level, pos, new ItemStack(item));
            }
        }
        return placed;
    }

    private static boolean placeFlowers(WorldGenLevel level, RandomSource random, BlockPos origin) {
        for (int attempt = 0; attempt < 8; attempt++) {
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
        return level.isEmptyBlock(pos) && level.getBlockState(pos.below()).is(BlockTags.DIRT)
            && level.setBlock(pos, GroundContent.PLUMARIA.get().defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static BlockPos surface(WorldGenLevel level, BlockPos pos) {
        return level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, pos);
    }

    private static boolean nearOak(WorldGenLevel level, BlockPos pos) {
        for (var nearby : BlockPos.betweenClosed(pos.offset(-5, -2, -5), pos.offset(5, 4, 5))) {
            if (level.getBlockState(nearby).is(net.minecraft.world.level.block.Blocks.OAK_LOG)) {
                return true;
            }
        }
        return false;
    }
}
