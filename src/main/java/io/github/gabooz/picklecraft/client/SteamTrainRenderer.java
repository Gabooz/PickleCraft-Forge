package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.gabooz.picklecraft.PickleCraft;
import io.github.gabooz.picklecraft.ground.GroundContent;
import io.github.gabooz.picklecraft.ground.SteamTrain;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class SteamTrainRenderer extends EntityRenderer<SteamTrain, SteamTrainRenderer.State> {
    private final ItemModelResolver models;
    private ItemStack engine;
    private ItemStack tender;
    private ItemStack coal;

    static final class State extends EntityRenderState {
        final ItemStackRenderState engine = new ItemStackRenderState();
        final ItemStackRenderState tender = new ItemStackRenderState();
        final ItemStackRenderState coal = new ItemStackRenderState();
        float heading;
        boolean loaded;
    }

    private SteamTrainRenderer(EntityRendererProvider.Context context) {
        super(context);
        models = context.getItemModelResolver();
    }

    public static void register() {
        EntityRenderers.register(SteamTrain.TYPE.get(), SteamTrainRenderer::new);
    }

    @Override public State createRenderState() { return new State(); }

    @Override public void extractRenderState(SteamTrain train, State state, float delta) {
        super.extractRenderState(train, state, delta);
        if (engine == null) {
            engine = model("steam_train");
            tender = model("steam_train_tender");
            coal = model("steam_train_coal");
        }
        models.updateForNonLiving(state.engine, engine, ItemDisplayContext.NONE, train);
        models.updateForNonLiving(state.tender, tender, ItemDisplayContext.NONE, train);
        state.loaded = train.coal() > 0;
        if (state.loaded) models.updateForNonLiving(state.coal, coal, ItemDisplayContext.NONE, train);
        state.heading = train.heading().toYRot();
    }

    private static ItemStack model(String path) {
        var item = GroundContent.TRAIN_ITEM.get().getDefaultInstance();
        item.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(PickleCraft.MOD_ID, path));
        return item;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.rotateDegrees(Axis.YP, -state.heading);
        section(state.engine, pose, collector, state.lightCoords, 10, 0);
        section(state.tender, pose, collector, state.lightCoords, 4, -7);
        if (state.loaded) section(state.coal, pose, collector, state.lightCoords, 4, -7);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private static void section(ItemStackRenderState model, PoseStack pose, SubmitNodeCollector collector,
            int light, float length, float offset) {
        pose.pushPose();
        pose.translate(0, 1.5F, offset);
        pose.scale(3, 3, length);
        model.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
