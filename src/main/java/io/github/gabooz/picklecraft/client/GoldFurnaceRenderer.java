package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gabooz.picklecraft.ground.GoldFurnace;
import io.github.gabooz.picklecraft.ground.GroundContent;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public final class GoldFurnaceRenderer implements BlockEntityRenderer<GoldFurnace, GoldFurnaceRenderer.State> {
    public static void register() {
        BlockEntityRenderers.register(GroundContent.GOLD_FURNACE_ENTITY.get(), context -> new GoldFurnaceRenderer());
    }

    public static final class State extends BlockEntityRenderState {
        private final ItemStackRenderState ore = new ItemStackRenderState();
        private final ItemStackRenderState fire = new ItemStackRenderState();
        private final ItemStackRenderState fuel = new ItemStackRenderState();
        private Direction front;
        private boolean lit;
        private boolean hot;
        private int fuelCount;
        private float heat;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GoldFurnace furnace, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay breaking) {
        BlockEntityRenderer.super.extractRenderState(furnace, state, partialTicks, camera, breaking);
        state.front = furnace.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.lit = furnace.getBlockState().getValue(BlockStateProperties.LIT);
        state.hot = furnace.hot();
        state.heat = furnace.heat();
        // Show a small pile while the furnace tracks all ten fuel items.
        state.fuelCount = state.lit ? 4 : (furnace.fuelCount() * 4 + GoldFurnace.FUEL_REQUIRED - 1)
            / GoldFurnace.FUEL_REQUIRED;
        var gold = furnace.gold().copy();
        if (state.hot) {
            gold.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", "hot_gold"));
        } else if (state.lit && state.heat > 0.55F) {
            gold.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", "heating_gold"));
        }
        var fire = state.lit ? new ItemStack(Items.COAL) : ItemStack.EMPTY;
        if (state.lit) fire.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", "firebox_flame"));
        var fuel = state.fuelCount > 0 ? new ItemStack(Items.COAL) : ItemStack.EMPTY;
        if (state.lit) fuel.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", "hot_coal"));
        var resolver = Minecraft.getInstance().getItemModelResolver();
        resolver.updateForTopItem(state.ore, gold, ItemDisplayContext.NONE, furnace.getLevel(), null, 0);
        resolver.updateForTopItem(state.fire, fire, ItemDisplayContext.NONE, furnace.getLevel(), null, 0);
        resolver.updateForTopItem(state.fuel, fuel, ItemDisplayContext.NONE, furnace.getLevel(), null, 0);
    }

    @Override
    public void submit(State state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        poses.pushPose();
        poses.translate(0.5 - state.front.getStepX() * 0.76, 0, 0.5 - state.front.getStepZ() * 0.76);
        poses.rotateDegrees(Axis.YP, switch (state.front) {
            case EAST -> -90;
            case SOUTH -> 180;
            case WEST -> 90;
            default -> 0;
        });
        int light = state.lit || state.hot ? 0xF000F0 : state.lightCoords;
        for (int i = 0; i < state.fuelCount; i++) {
            poses.pushPose();
            poses.translate((i % 2 == 0 ? -0.045 : 0.045), 0.1 + i * 0.038, -0.02 + (i % 3 - 1) * 0.032);
            poses.rotateDegrees(Axis.YP, i * 29);
            poses.rotateDegrees(Axis.XP, -90);
            poses.scale(0.43F, 0.37F, 0.68F);
            state.fuel.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poses.popPose();
        }
        poses.pushPose();
        poses.translate(0, 0.15 + state.fuelCount * 0.036, -0.11);
        poses.rotateDegrees(Axis.XP, -90);
        poses.scale(0.62F, 0.62F, 0.62F);
        state.ore.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poses.popPose();
        if (state.lit) {
            for (int i = -1; i <= 1; i++) {
                poses.pushPose();
                poses.translate(i * 0.27, 0.49 + i * 0.055, 0.28);
                poses.rotateDegrees(Axis.YP, i * 22);
                poses.scale(0.48F, 0.83F, 0.12F);
                state.fire.submit(poses, collector, light, OverlayTexture.NO_OVERLAY, 0);
                poses.popPose();
            }
        }
        poses.popPose();
    }
}
