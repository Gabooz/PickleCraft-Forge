package io.github.gabooz.picklecraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.client.event.ScreenEvent;

public final class InventoryScreens {
    private InventoryScreens() {}

    public static void register() {
        ScreenEvent.Opening.BUS.addListener(InventoryScreens::replaceInventory);
        if (Boolean.getBoolean("picklecraft.smokeTest")) {
            try {
                Class.forName("io.github.gabooz.picklecraft.dev.Playtest").getMethod("register").invoke(null);
            } catch (ReflectiveOperationException error) {
                throw new IllegalStateException("Could not start the PickleCraft playtest", error);
            }
        }
    }

    private static void replaceInventory(ScreenEvent.Opening event) {
        var player = Minecraft.getInstance().player;
        Screen screen = event.getNewScreen();
        if (screen != null && screen.getClass() == InventoryScreen.class && player != null && !player.hasInfiniteMaterials()) {
            event.setNewScreen(new SurvivalInventoryScreen(player));
        }
    }
}
