package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gabooz.picklecraft.ground.GroundContent;
import io.github.gabooz.picklecraft.ground.Stockpile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LightLayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class StockpileRenderer implements BlockEntityRenderer<Stockpile, StockpileRenderer.State> {
    private enum Arrangement { BLOCKS, ITEMS, COAL, DUST, SAND, ROCK, LOGS, STONE, BRICKS }

    public static void register() {
        BlockEntityRenderers.register(GroundContent.STOCKPILE_ENTITY.get(), context -> new StockpileRenderer());
    }

    public static final class State extends BlockEntityRenderState {
        private final ItemStackRenderState material = new ItemStackRenderState();
        private Arrangement arrangement;
        private float fill;
        private double centerX;
        private double centerZ;
        private int interior;
        private long seed;
        private boolean flatBricks;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void extractRenderState(Stockpile pile, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(pile, state, partialTicks, camera, breaking);
        var sample = pile.displayItem();
        state.arrangement = arrangement(sample);
        state.flatBricks = state.arrangement == Arrangement.BRICKS && !(sample.getItem() instanceof BlockItem);
        state.fill = pile.displayFill();
        state.interior = pile.size() - 2;
        state.centerX = pile.corner().getX() - pile.getBlockPos().getX() + pile.size() / 2.0;
        state.centerZ = pile.corner().getZ() - pile.getBlockPos().getZ() + pile.size() / 2.0;
        state.seed = pile.getBlockPos().asLong();
        var level = pile.getLevel();
        if (level != null) {
            // The controller is solid and may be shaded by the rim. Light the pile from open air above it.
            var surface = pile.corner().offset(pile.size() / 2, 1, pile.size() / 2);
            var overRim = pile.getBlockPos().above();
            int block = Math.max(level.getBrightness(LightLayer.BLOCK, surface),
                level.getBrightness(LightLayer.BLOCK, overRim));
            int sky = Math.max(level.getBrightness(LightLayer.SKY, surface),
                level.getBrightness(LightLayer.SKY, overRim));
            state.lightCoords = sky << 20 | block << 4;
        }
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state.material, sample,
            ItemDisplayContext.NONE, pile.getLevel(), null, 0);
    }

    private static Arrangement arrangement(ItemStack sample) {
        if (sample.is(Items.COAL) || sample.is(Items.CHARCOAL)) return Arrangement.COAL;
        if (sample.is(Items.REDSTONE)) return Arrangement.DUST;
        if (sample.is(Items.BRICK) || sample.is(Items.NETHER_BRICK)) return Arrangement.BRICKS;
        if (sample.getItem() instanceof BlockItem item) {
            var block = item.getBlock().defaultBlockState();
            if (block.is(BlockTags.LOGS)) return Arrangement.LOGS;
            if (block.is(BlockTags.SAND)) return Arrangement.SAND;
            if (block.is(Blocks.COBBLESTONE) || block.is(Blocks.COBBLED_DEEPSLATE)) return Arrangement.ROCK;
            if (block.is(Blocks.STONE) || block.is(Blocks.SMOOTH_STONE)) return Arrangement.STONE;
            if (block.is(Blocks.BRICKS) || block.is(Blocks.STONE_BRICKS) || block.is(Blocks.NETHER_BRICKS)) {
                return Arrangement.BRICKS;
            }
            return Arrangement.BLOCKS;
        }
        return Arrangement.ITEMS;
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.fill <= 0) return;
        var random = RandomSource.create(state.seed);
        poses.pushPose();
        poses.translate(state.centerX, -1.0 / 16, state.centerZ);
        switch (state.arrangement) {
            case LOGS, STONE, BRICKS -> stacks(state, poses, collector);
            default -> mound(state, poses, collector, random);
        }
        poses.popPose();
    }

    private static void stacks(State state, PoseStack poses, SubmitNodeCollector collector) {
        boolean logs = state.arrangement == Arrangement.LOGS;
        boolean bricks = state.arrangement == Arrangement.BRICKS;
        int width = Math.min(7, state.interior + 1);
        float cell = state.interior * 0.97F / width;
        int columns = logs ? width : width * width;
        int pieces = Math.max(1, (int) Math.ceil(Math.sqrt(state.fill) * columns * (bricks ? 7 : 4)));
        // Build each layer from the center out so a partially filled pile stays balanced.
        for (int i = 0; i < pieces; i++) {
            int layer = i / columns;
            int column = i % columns;
            int x = centeredIndex(column % width, width);
            int z = logs ? 0 : centeredIndex(column / width, width);
            float height = cell * (bricks ? 0.46F : 0.78F);
            poses.pushPose();
            poses.translate((x - (width - 1) / 2.0) * cell, height * (layer + 0.5),
                logs ? 0 : (z - (width - 1) / 2.0) * cell);
            if (logs) {
                poses.scale(cell * 0.92F, height * 0.98F, state.interior * 0.90F);
                poses.rotateDegrees(Axis.XP, 90);
            } else {
                poses.scale(cell * 0.92F, height * 0.98F, cell * 0.92F);
                if (bricks) {
                    poses.rotateDegrees(Axis.YP, (layer % 2) * 90);
                    if (state.flatBricks) {
                        // A sprite is only one pixel thick; use overlapping courses so a tower reads as masonry.
                        poses.rotateDegrees(Axis.XP, -90);
                        poses.scale(1, 1, 12);
                    }
                }
            }
            state.material.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poses.popPose();
        }
    }

    private static int centeredIndex(int index, int width) {
        return (width - 1) / 2 + (index % 2 == 0 ? -index / 2 : (index + 1) / 2);
    }

    private static void sandBank(State state, PoseStack poses, SubmitNodeCollector collector,
            float radius, float fullness) {
        // Sand forms a continuous bank of close-packed columns with a tapered profile.
        int grid = Math.clamp((int) Math.ceil(radius * 5), 1, 8);
        float cell = radius / grid;
        for (int z = -grid; z <= grid; z++) {
            for (int x = -grid; x <= grid; x++) {
                double distance = Math.hypot(x, z) / grid;
                if (distance > 1) continue;
                float height = (float) (0.045 + (1 - distance) * fullness * (0.65 + state.interior * 0.15));
                poses.pushPose();
                poses.translate(x * cell, height / 2, z * cell);
                poses.scale(cell, height, cell);
                state.material.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poses.popPose();
            }
        }
    }

    private static void mound(State state, PoseStack poses, SubmitNodeCollector collector, RandomSource random) {
        var type = state.arrangement;
        boolean granules = type == Arrangement.DUST;
        boolean flat = type == Arrangement.ITEMS || type == Arrangement.COAL || type == Arrangement.DUST;
        float fullness = (float) Math.sqrt(state.fill);
        float reach = Math.min(state.interior * 0.48F, 0.22F + state.interior * 0.40F * fullness);
        if (type == Arrangement.SAND) {
            sandBank(state, poses, collector, reach, fullness);
            return;
        }
        float pieceScale = switch (type) {
            case DUST -> 0.31F;
            case COAL, ROCK -> 0.50F;
            case ITEMS -> 0.57F;
            default -> 0.65F;
        };
        int grid = Math.clamp((int) Math.ceil(reach / (pieceScale * 0.7)), 1, 7);
        float step = reach / grid;
        float height = pieceScale * (flat ? 0.23F : 0.72F);
        for (int z = -grid; z <= grid; z++) {
            for (int x = -grid; x <= grid; x++) {
                float radius = (float) Math.hypot(x, z) / grid;
                if ((state.fill < 0.006F && (x != 0 || z != 0)) || radius > 1 || (radius > 0.72F && random.nextFloat() < 0.20F)) continue;
                float scatterX = (random.nextFloat() - 0.5F) * step * 0.62F;
                float scatterZ = (random.nextFloat() - 0.5F) * step * 0.62F;
                int layers = 1 + Math.round((1 - radius) * fullness * (granules ? 6 : 5));
                for (int layer = 0; layer < layers; layer++) {
                    float scale = pieceScale * (0.82F + random.nextFloat() * 0.30F);
                    poses.pushPose();
                    poses.translate(x * step + scatterX, height * (layer + 0.5),
                        z * step + scatterZ);
                    poses.rotateDegrees(Axis.YP, random.nextFloat() * 360);
                    if (flat) poses.rotateDegrees(Axis.XP, -90 + random.nextFloat() * 20 - 10);
                    else if (type == Arrangement.ROCK) poses.rotateDegrees(Axis.ZP, random.nextFloat() * 36 - 18);
                    poses.scale(scale, scale, flat ? scale * 2.5F : scale);
                    int light = granules && random.nextInt(7) == 0 ? 0xF000F0 : state.lightCoords;
                    state.material.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
                    poses.popPose();
                }
            }
        }
    }
}
