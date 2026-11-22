package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gabooz.picklecraft.PickleCraft;
import io.github.gabooz.picklecraft.ground.GroundContent;
import io.github.gabooz.picklecraft.ground.Sailboat;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SailboatRenderer extends BoatRenderer {
    private final ItemModelResolver models;
    private ItemStack rig;

    private static final class State extends BoatRenderState {
        final ItemStackRenderState rig = new ItemStackRenderState();
    }

    private SailboatRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.OAK_BOAT);
        models = context.getItemModelResolver();
    }

    public static void register() {
        EntityRenderers.register(Sailboat.TYPE.get(), SailboatRenderer::new);
    }

    @Override public BoatRenderState createRenderState() { return new State(); }

    @Override
    public void extractRenderState(AbstractBoat boat, BoatRenderState state, float delta) {
        super.extractRenderState(boat, state, delta);
        if (rig == null) {
            rig = GroundContent.SAILBOAT.get().getDefaultInstance();
            rig.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(PickleCraft.MOD_ID, "sailboat_rig"));
        }
        models.updateForNonLiving(((State) state).rig, rig, ItemDisplayContext.NONE, boat);
    }

    @Override
    protected void submitTypeAdditions(BoatRenderState state, PoseStack pose, SubmitNodeCollector collector, int light) {
        super.submitTypeAdditions(state, pose, collector, light);
        // Work in the hull's coordinates so the rig follows turns, hurt wobble, and waves.
        pose.pushPose();
        pose.translate(-0.42F, -0.75F, 0);
        pose.rotateDegrees(Axis.XP, 180);
        pose.scale(1.35F, 1.35F, 1.35F);
        ((State) state).rig.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
    }
}
