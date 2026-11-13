package io.github.gabooz.picklecraft.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;

/** Uses the regular inventory slots, player preview, and effects without the crafting controls. */
public final class SurvivalInventoryScreen extends AbstractContainerScreen<InventoryMenu> {
    private final Player player;
    private final EffectsInInventory effects = new EffectsInInventory(this);

    public SurvivalInventoryScreen(Player player) {
        super(player.inventoryMenu, player.getInventory(), Component.translatable("container.inventory"));
        this.player = player;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // The vanilla inventory texture already lays out the visible slots.
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        effects.extractRenderState(graphics, mouseX, mouseY);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean showsActiveEffects() {
        return effects.canSeeEffects();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_LOCATION, leftPos, topPos,
            0, 0, imageWidth, imageHeight, 256, 256);
        // Fill the old grid and arrow with the panel color from the same texture.
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_LOCATION, leftPos + 96, topPos + 16,
            96, 15, 77, 40, 1, 1, 256, 256);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, leftPos + 26, topPos + 8,
            leftPos + 75, topPos + 78, 30, 0.0625F, mouseX, mouseY, player);
    }
}
