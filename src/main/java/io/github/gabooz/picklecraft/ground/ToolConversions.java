package io.github.gabooz.picklecraft.ground;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

/** Material changes preserve tool type, custom components, and proportion of durability already used. */
public final class ToolConversions {
    private ToolConversions() {}
    private static List<List<Item>> families() {
        return List.of(
            List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE),
            List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE),
            List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL),
            List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD),
            List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE),
            List.of(GroundContent.WOODEN_HAMMER.get(), GroundContent.STONE_HAMMER.get(), GroundContent.IRON_HAMMER.get()));
    }
    public static ItemStack convert(ItemStack tool, ItemStack material) {
        int tier = material.is(ItemTags.PLANKS) ? 0 : material.is(Items.COBBLESTONE) ? 1 : material.is(Items.IRON_INGOT) ? 2 : -1;
        if (tier < 0) return ItemStack.EMPTY;
        for (var family : families()) if (family.contains(tool.getItem())) {
            var target = family.get(tier);
            if (tool.is(target)) return ItemStack.EMPTY;
            var result = tool.transmuteCopy(target, 1);
            if (tool.getMaxDamage() > 0 && result.getMaxDamage() > 0) {
                int damage = (int) Math.ceil((double) tool.getDamageValue() / tool.getMaxDamage() * result.getMaxDamage());
                result.setDamageValue(Math.min(result.getMaxDamage() - 1, damage));
            }
            return result;
        }
        return ItemStack.EMPTY;
    }
}
