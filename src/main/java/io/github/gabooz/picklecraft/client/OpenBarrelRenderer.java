package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gabooz.picklecraft.ground.GroundContent;
import io.github.gabooz.picklecraft.ground.OpenBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class OpenBarrelRenderer implements BlockEntityRenderer<OpenBarrel, OpenBarrelRenderer.State> {
    // Divide the space above the floor into 64 layers.
    private static final double FLOOR = 1.0 / 16;
    private static final double LAYER_HEIGHT = (1 - FLOOR) / OpenBarrel.CAPACITY;

    public static void register() {
        BlockEntityRenderers.register(GroundContent.BARREL_ENTITY.get(), context -> new OpenBarrelRenderer());
    }

    private record Run(ItemStackRenderState model, int count, boolean fullCube) {}

    public static final class State extends BlockEntityRenderState {
        private final List<Run> runs = new ArrayList<>();
        private long layoutSeed;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(OpenBarrel barrel, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(barrel, state, partialTicks, camera, breaking);
        state.runs.clear();
        state.layoutSeed = barrel.getBlockPos().asLong();
        var resolver = Minecraft.getInstance().getItemModelResolver();
        for (var stack : barrel.contents()) {
            var model = new ItemStackRenderState();
            resolver.updateForTopItem(model, stack, ItemDisplayContext.NONE, barrel.getLevel(), null, 0);
            state.runs.add(new Run(model, stack.getCount(), fillsSlab(stack.getItem())));
        }
    }

    private static boolean fillsSlab(net.minecraft.world.item.Item item) {
        if (item instanceof BlockItem blockItem) {
            var block = blockItem.getBlock().defaultBlockState();
            return !block.hasBlockEntity() && block.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        }
        return false;
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        // The same barrel and layer always get the same pose, even as the pile grows.
        var layout = RandomSource.create(state.layoutSeed);
        int layer = 0;
        for (var run : state.runs) {
            for (int i = 0; i < run.count(); i++, layer++) {
                poses.pushPose();
                double x = (layout.nextFloat() - 0.5) * 0.22;
                double z = (layout.nextFloat() - 0.5) * 0.22;
                float angle = layout.nextFloat() * 360;
                double y = FLOOR + (layer + 0.5) * LAYER_HEIGHT;
                if (run.fullCube()) {
                    poses.translate(0.5, y, 0.5);
                    poses.scale(0.875F, (float) LAYER_HEIGHT, 0.875F);
                } else {
                    poses.translate(0.5 + x, y, 0.5 + z);
                    poses.rotateDegrees(Axis.YP, angle);
                    poses.rotateDegrees(Axis.XP, -90);
                    poses.scale(0.45F, 0.45F, (float) (LAYER_HEIGHT * 0.9));
                }
                run.model().submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poses.popPose();
            }
        }
    }
}
