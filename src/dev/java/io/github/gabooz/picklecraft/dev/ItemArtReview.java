package io.github.gabooz.picklecraft.dev;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Displays the actual baked item models, including furnace-only model states. */
final class ItemArtReview extends Screen {
    private record Reference(String model, Supplier<ItemStack> comparison) {
        Reference(String model, Item vanilla) {
            this(model, vanilla::getDefaultInstance);
        }
    }

    private static final List<Reference> REFERENCES = List.of(
        new Reference("anvil", Items.ANVIL), new Reference("open_barrel", Items.BARREL),
        new Reference("workbench", Items.OAK_LOG), new Reference("carpenter_table", Items.CRAFTING_TABLE),
        new Reference("iron_furnace", Items.FURNACE), new Reference("plumaria", Items.AZURE_BLUET),
        new Reference("hot_ingot", Items.IRON_INGOT), new Reference("white_hot_ingot", Items.IRON_INGOT),
        new Reference("hot_gold", Items.GOLD_INGOT), new Reference("hot_ore", Items.RAW_IRON),
        new Reference("heating_gold", Items.RAW_GOLD), new Reference("hot_coal", Items.CHARCOAL),
        new Reference("firebox_flame", Items.FIRE_CHARGE), new Reference("iron_saw", Items.SHEARS),
        new Reference("saw_blade", () -> modelStack("iron_saw")),
        new Reference("saw_handle", () -> modelStack("iron_saw")),
        new Reference("wooden_hammer", Items.WOODEN_AXE), new Reference("stone_hammer", Items.STONE_AXE),
        new Reference("iron_hammer", Items.IRON_AXE), new Reference("iron_tongs", Items.SHEARS),
        new Reference("iron_tongs_loaded", Items.IRON_INGOT), new Reference("iron_tongs_loaded_gold", Items.GOLD_INGOT),
        new Reference("oar", Items.OAK_BOAT), new Reference("fire_starter", Items.FLINT_AND_STEEL),
        new Reference("loose_stone", Items.FLINT), new Reference("glue", Items.SLIME_BALL),
        new Reference("plumaria_blossoms", Items.PEONY), new Reference("zone_map", Items.MAP),
        new Reference("workshop_torch", Items.TORCH), new Reference("unlit_torch", Items.TORCH),
        new Reference("herd_cow_spawn_egg", Items.COW_SPAWN_EGG), new Reference("bull_spawn_egg", Items.COW_SPAWN_EGG)
    );

    private final List<ItemStack> items = new ArrayList<>();
    private final List<ItemStack> comparisonItems = new ArrayList<>();

    ItemArtReview() {
        super(Component.literal("Item art: PickleCraft / reference"));
        for (Reference reference : REFERENCES) {
            ItemStack item = modelStack(reference.model());
            if (reference.model().equals("hot_ore")) {
                item.set(DataComponents.CUSTOM_MODEL_DATA,
                    new net.minecraft.world.item.component.CustomModelData(List.of(), List.of(), List.of(), List.of(0xffb766)));
            }
            items.add(item);
            comparisonItems.add(reference.comparison().get());
        }
    }

    private static ItemStack modelStack(String model) {
        ItemStack item = Items.STICK.getDefaultInstance();
        item.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", model));
        return item;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xff22262c);
        graphics.centeredText(font, title, width / 2, 12, 0xfff1e8d4);
        int columns = Math.min(8, Math.max(1, width / 48));
        int left = (width - columns * 48) / 2;
        for (int i = 0; i < items.size(); i++) {
            int x = left + (i % columns) * 48;
            int y = 32 + (i / columns) * 29;
            graphics.fill(x, y, x + 44, y + 24, 0xff3d454e);
            graphics.item(items.get(i), x + 3, y + 4);
            graphics.item(comparisonItems.get(i), x + 25, y + 4);
            if (mouseX >= x && mouseX < x + 44 && mouseY >= y && mouseY < y + 24) {
                String name = REFERENCES.get(i).model();
                graphics.centeredText(font, name.replace('_', ' '), width / 2, height - 14, 0xfff1e8d4);
            }
        }
    }
}
