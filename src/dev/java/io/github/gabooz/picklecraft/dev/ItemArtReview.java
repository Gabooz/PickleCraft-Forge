package io.github.gabooz.picklecraft.dev;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Displays the actual baked item models, including furnace-only model states. */
final class ItemArtReview extends Screen {
    private static final List<String> MODELS = List.of(
        "anvil", "open_barrel", "workbench", "carpenter_table", "iron_furnace", "plumaria",
        "hot_ingot", "white_hot_ingot", "hot_gold", "hot_ore", "heating_gold", "hot_coal",
        "firebox_flame", "iron_saw", "saw_blade", "saw_handle", "wooden_hammer", "stone_hammer",
        "iron_hammer", "iron_tongs", "iron_tongs_loaded", "iron_tongs_loaded_gold", "oar", "fire_starter",
        "loose_stone", "glue", "plumaria_blossoms", "zone_map", "workshop_torch", "unlit_torch",
        "herd_cow_spawn_egg", "bull_spawn_egg"
    );

    private final List<ItemStack> items = new ArrayList<>();

    ItemArtReview() {
        super(Component.literal("PickleCraft item art"));
        for (String name : MODELS) {
            ItemStack item = Items.STICK.getDefaultInstance();
            item.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", name));
            if (name.equals("hot_ore")) {
                item.set(DataComponents.CUSTOM_MODEL_DATA,
                    new net.minecraft.world.item.component.CustomModelData(List.of(), List.of(), List.of(), List.of(0xffb766)));
            }
            items.add(item);
        }
        items.add(Items.IRON_INGOT.getDefaultInstance());
        items.add(Items.GOLD_INGOT.getDefaultInstance());
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xff22262c);
        graphics.centeredText(font, title, width / 2, 12, 0xfff1e8d4);
        int columns = Math.min(6, Math.max(1, width / 40));
        int left = (width - columns * 38) / 2;
        for (int i = 0; i < items.size(); i++) {
            int x = left + (i % columns) * 38;
            int y = 32 + (i / columns) * 27;
            graphics.fill(x, y, x + 34, y + 24, 0xff3d454e);
            graphics.item(items.get(i), x + 9, y + 4);
            if (mouseX >= x && mouseX < x + 34 && mouseY >= y && mouseY < y + 24) {
                String name = i < MODELS.size() ? MODELS.get(i) : i == MODELS.size() ? "vanilla iron ingot" : "vanilla gold ingot";
                graphics.centeredText(font, name.replace('_', ' '), width / 2, height - 14, 0xfff1e8d4);
            }
        }
    }
}
