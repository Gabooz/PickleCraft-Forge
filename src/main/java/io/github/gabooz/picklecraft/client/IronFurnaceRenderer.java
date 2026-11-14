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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        private final ItemStackRenderState flame = new ItemStackRenderState();
        private Direction front;
        private boolean lit;
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
        state.lit = furnace.getBlockState().getValue(BlockStateProperties.LIT);
        var fuel = furnace.fuel().copy();
        if (state.lit && !fuel.isEmpty()) {
            fuel.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", "hot_coal"));
        }
        resolver.updateForTopItem(state.fuel, fuel, ItemDisplayContext.NONE, furnace.getLevel(), null, 0);
        var flame = state.lit ? new ItemStack(Items.COAL) : ItemStack.EMPTY;
        if (state.lit) {
            flame.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", "firebox_flame"));
        }
        resolver.updateForTopItem(state.flame, flame, ItemDisplayContext.NONE, furnace.getLevel(), null, 0);
        var iron = furnace.iron().copy();
        int time = furnace.elapsed();
        state.glowing = !iron.isEmpty() && time >= 0 && time < IronFurnace.SMELT_TICKS + IronFurnace.COOL_TICKS;
        if (state.glowing) {
            boolean ingot = time >= IronFurnace.INGOT_TICKS;
            float heat = ingot
                ? 1F - Math.max(0, time - IronFurnace.SMELT_TICKS) / (float) IronFurnace.COOL_TICKS
                : time / (float) IronFurnace.INGOT_TICKS;
            int red = Math.round(165 + 90 * heat);
            int green = Math.round(135 + 93 * heat);
            int blue = Math.round(105 + 64 * heat);
            int color = red << 16 | green << 8 | blue;
            iron.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", ingot ? "hot_ingot" : "hot_ore"));
            iron.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), List.of(color)));
        }
        resolver.updateForTopItem(state.iron, iron, ItemDisplayContext.NONE, furnace.getLevel(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        poses.pushPose();
        poses.translate(0.5, 0.225, 0.5);
        poses.rotateDegrees(Axis.XP, -90);
        poses.scale(0.43F, 0.43F, 0.53F);
        state.iron.submit(poses, collector, state.glowing ? 0xF000F0 : state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
        poses.pushPose();
        poses.translate(0.5, 0.16, 0.5);
        poses.rotateDegrees(Axis.XP, -90);
        poses.scale(0.54F, 0.54F, 0.95F);
        state.fuel.submit(poses, collector, state.lit ? 0xF000F0 : state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();

        if (state.lit) {
            for (int angle : new int[]{0, 90}) {
                poses.pushPose();
                // Cross two upright flame planes behind the iron so it stays visible.
                poses.translate(0.5 - state.front.getStepX() * 0.16, 0.46,
                    0.5 - state.front.getStepZ() * 0.16);
                poses.rotateDegrees(Axis.YP, angle);
                poses.scale(0.85F, 0.71F, 0.1F);
                state.flame.submit(poses, collector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
                poses.popPose();
            }
        }
    }
}
