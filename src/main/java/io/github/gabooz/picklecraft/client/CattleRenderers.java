package io.github.gabooz.picklecraft.client;

import io.github.gabooz.picklecraft.cattle.CattleContent;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public final class CattleRenderers {
    private CattleRenderers() {}

    public static void register() {
        EntityRenderers.register(CattleContent.COW.get(), CowRenderer::new);
        EntityRenderers.register(CattleContent.BULL.get(), BullRenderer::new);
    }
}
