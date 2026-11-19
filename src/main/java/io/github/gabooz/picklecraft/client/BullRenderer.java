package io.github.gabooz.picklecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.gabooz.picklecraft.cattle.HerdCow;
import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

/** Uses Minecraft's warm-cow atlas and horn layout with a male body. */
public final class BullRenderer extends MobRenderer<HerdCow, CowRenderState, CowModel> {
    private final AdultAndBabyModelPair<CowModel> models;

    public BullRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel(layer().bakeRoot()), 0.8F);
        var layer = layer();
        models = new AdultAndBabyModelPair<>(new CowModel(layer.bakeRoot()),
            new CowModel(layer.apply(CowModel.BABY_TRANSFORMER).bakeRoot()));
    }

    private static LayerDefinition layer() {
        var mesh = CowModel.createBaseCowModel();
        mesh.getRoot().addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(18, 4).addBox(-6, -10, -7, 12, 18, 10),
            PartPose.offsetAndRotation(0, 5, 2, (float) Math.PI / 2, 0, 0));
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4, -4, -6, 8, 8, 6)
            .texOffs(1, 33).addBox(-3, 1, -7, 6, 3, 1)
            .texOffs(27, 0).addBox(-8, -3, -5, 4, 2, 2)
            .texOffs(39, 0).addBox(-8, -5, -5, 2, 2, 2)
            .texOffs(27, 0).mirror().addBox(4, -3, -5, 4, 2, 2).mirror(false)
            .texOffs(39, 0).mirror().addBox(6, -5, -5, 2, 2, 2).mirror(false),
            PartPose.offset(0, 4, -8));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public CowRenderState createRenderState() { return new CowRenderState(); }

    @Override
    public void extractRenderState(HerdCow cow, CowRenderState state, float partialTicks) {
        super.extractRenderState(cow, state, partialTicks);
        state.variant = cow.getVariant().value();
    }

    @Override
    public Identifier getTextureLocation(CowRenderState state) {
        return state.isBaby ? state.variant.babyTexture().texturePath()
            : state.variant.modelAndTexture().asset().texturePath();
    }

    @Override
    public void submit(CowRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        model = models.getModel(state.isBaby);
        super.submit(state, poses, collector, camera);
    }
}
