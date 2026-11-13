package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gabooz.picklecraft.ground.GroundContent;
import io.github.gabooz.picklecraft.ground.IronFurnace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public final class IronFurnaceRenderer implements BlockEntityRenderer<IronFurnace, IronFurnaceRenderer.State> {
    public static void register() {
        BlockEntityRenderers.register(GroundContent.FURNACE_ENTITY.get(), context -> new IronFurnaceRenderer());
    }

    public static final class State extends BlockEntityRenderState {
        private final ItemStackRenderState iron = new ItemStackRenderState();
        private final ItemStackRenderState fuel = new ItemStackRenderState();
        private Direction front;
        private boolean glowing;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IronFurnace furnace, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(furnace, state, partialTicks, camera, breaking);
        state.front = furnace.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        var resolver = Minecraft.getInstance().getItemModelResolver();
        resolver.updateForTopItem(state.fuel, furnace.fuel(), ItemDisplayContext.NONE, furnace.getLevel(), null, 0);
        var iron = furnace.iron().copy();
        int time = furnace.elapsed();
        state.glowing = !iron.isEmpty() && time >= 0 && time < IronFurnace.SMELT_TICKS + IronFurnace.COOL_TICKS;
        if (state.glowing) {
            boolean ingot = time >= IronFurnace.INGOT_TICKS;
            float heat = ingot
                ? 1F - Math.max(0, time - IronFurnace.SMELT_TICKS) / (float) IronFurnace.COOL_TICKS
                : time / (float) IronFurnace.INGOT_TICKS;
            int red = Math.round(105 + 150 * heat);
            int green = Math.round(95 + 48 * heat);
            int blue = Math.round(90 - 70 * heat);
            int color = red << 16 | green << 8 | blue;
            iron.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", ingot ? "hot_ingot" : "hot_ore"));
            iron.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), List.of(color)));
        }
        resolver.updateForTopItem(state.iron, iron, ItemDisplayContext.NONE, furnace.getLevel(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        poses.pushPose();
        poses.translate(0.5, 1.18, 0.5);
        poses.rotateDegrees(Axis.XP, -90);
        poses.scale(0.37F, 0.37F, 0.4F);
        state.iron.submit(poses, collector, state.glowing ? 0xF000F0 : state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
        poses.pushPose();
        poses.translate(0.5 + state.front.getStepX() * 0.35, 0.32, 0.5 + state.front.getStepZ() * 0.35);
        if (state.front.getAxis() == Direction.Axis.X) {
            poses.rotateDegrees(Axis.YP, 90);
        }
        poses.scale(0.38F, 0.38F, 0.38F);
        state.fuel.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
    }
}
