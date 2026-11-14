package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gabooz.picklecraft.ground.GroundContent;
import io.github.gabooz.picklecraft.ground.GroundStack;
import io.github.gabooz.picklecraft.ground.WorkbenchBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public final class GroundStackRenderer implements BlockEntityRenderer<GroundStack, GroundStackRenderer.State> {
    public static void register() {
        BlockEntityRenderers.register(GroundContent.STACK_ENTITY.get(), context -> new GroundStackRenderer());
    }

    public static final class State extends BlockEntityRenderState {
        private boolean workbench;
        private boolean anvil;
        private boolean cube;
        private final List<ItemStackRenderState> layers = new ArrayList<>();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GroundStack stack, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(stack, state, partialTicks, camera, breaking);
        state.workbench = stack.getBlockState().is(GroundContent.WORKBENCH.get());
        state.anvil = stack.getBlockState().is(GroundContent.ANVIL.get());
        state.layers.clear();
        var resolver = Minecraft.getInstance().getItemModelResolver();
        var items = stack.items();
        state.cube = state.workbench && !items.isEmpty()
            && items.getFirst().getItem() instanceof net.minecraft.world.item.BlockItem;
        for (var item : items) {
            var model = new ItemStackRenderState();
            resolver.updateForTopItem(model, item, ItemDisplayContext.NONE, stack.getLevel(), null, 0);
            state.layers.add(model);
        }
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < state.layers.size(); i++) {
            poses.pushPose();
            if (state.cube) {
                float size = 0.4F;
                // The model origin is centered, so raise it by half its height minus the buried third.
                poses.translate(0.5, WorkbenchBlock.SURFACE_HEIGHT + size / 6.0 + i * 0.04, 0.5);
                poses.scale(size, size, size);
                state.layers.get(i).submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poses.popPose();
                continue;
            }
            if (state.workbench) {
                poses.translate(0.5, 0.9 + i / 32.0, 0.5);
                poses.rotateDegrees(Axis.YP, i * 13);
            } else {
                poses.translate(0.5, (state.anvil ? 1.0 : 0.0) + (i + 0.5) / 16.0, 0.5);
                poses.rotateDegrees(Axis.YP, i * 37);
            }
            poses.rotateDegrees(Axis.XP, -90);
            float scale = state.anvil ? 0.57F : state.workbench ? 0.55F : 0.92F;
            poses.scale(scale, scale, state.workbench ? 0.16F : 0.9F);
            state.layers.get(i).submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poses.popPose();
        }
    }
}
