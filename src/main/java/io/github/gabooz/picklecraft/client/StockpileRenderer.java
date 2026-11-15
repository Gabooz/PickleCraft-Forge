package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gabooz.picklecraft.ground.GroundContent;
import io.github.gabooz.picklecraft.ground.Stockpile;
import net.minecraft.client.Minecraft;
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
        state.fill = pile.displayFill();
        state.interior = pile.size() - 2;
        state.centerX = pile.corner().getX() - pile.getBlockPos().getX() + pile.size() / 2.0;
        state.centerZ = pile.corner().getZ() - pile.getBlockPos().getZ() + pile.size() / 2.0;
        state.seed = pile.getBlockPos().asLong();
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
        float cell = state.interior * 0.85F / width;
        int columns = logs ? width : width * width;
        int pieces = Math.max(1, (int) Math.ceil(Math.sqrt(state.fill) * columns * (bricks ? 5 : 3)));
        // Build each layer from the center out so a partially filled pile stays balanced.
        for (int i = 0; i < pieces; i++) {
            int layer = i / columns;
            int column = i % columns;
            int x = centeredIndex(column % width, width);
            int z = logs ? 0 : centeredIndex(column / width, width);
            float height = cell * (bricks ? 0.32F : 0.55F);
            poses.pushPose();
            poses.translate((x - (width - 1) / 2.0) * cell, height * (layer + 0.5),
                logs ? 0 : (z - (width - 1) / 2.0) * cell);
            if (logs) {
                poses.scale(cell * 0.92F, height * 0.98F, state.interior * 0.74F);
                poses.rotateDegrees(Axis.XP, 90);
            } else {
                poses.scale(cell * 0.92F, height * 0.98F, cell * 0.92F);
                if (bricks) poses.rotateDegrees(Axis.YP, (layer % 2) * 90);
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
                float height = (float) (0.045 + (1 - distance) * fullness * (0.35 + state.interior * 0.11));
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
        float reach = state.interior * (0.17F + 0.24F * fullness);
        if (type == Arrangement.SAND) {
            sandBank(state, poses, collector, reach, fullness);
            return;
        }
        float size = switch (type) {
            case DUST -> 0.20F;
            case COAL, ROCK -> 0.34F;
            case ITEMS -> 0.40F;
            default -> 0.55F;
        };
        int pieces = Math.clamp((int) Math.ceil((granules ? 50 : 26) * state.interior * fullness), 1, 160);
        for (int i = 0; i < pieces; i++) {
            double angle = random.nextDouble() * Math.TAU;
            double radius = Math.sqrt(random.nextDouble());
            float scale = size * (0.68F + random.nextFloat() * 0.55F);
            double height = (1 - radius) * fullness * (0.24 + state.interior * 0.075);
            // Ground some pieces at every radius, then heap the others towards the middle.
            if (i % 3 == 0) height = 0;
            poses.pushPose();
            poses.translate(Math.cos(angle) * radius * reach, height + scale * (flat ? 0.18 : 0.5),
                Math.sin(angle) * radius * reach);
            poses.rotateDegrees(Axis.YP, random.nextFloat() * 360);
            if (flat) poses.rotateDegrees(Axis.XP, -70 - random.nextFloat() * 35);
            else if (type == Arrangement.ROCK) poses.rotateDegrees(Axis.ZP, random.nextFloat() * 45 - 22.5F);
            poses.scale(scale, scale, scale);
            int light = type == Arrangement.DUST && i % 7 == 0 ? 0xF000F0 : state.lightCoords;
            state.material.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poses.popPose();
        }
    }
}
