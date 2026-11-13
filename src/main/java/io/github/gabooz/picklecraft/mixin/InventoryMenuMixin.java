package io.github.gabooz.picklecraft.mixin;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryMenu.class)
abstract class InventoryMenuMixin extends AbstractCraftingMenu {
    protected InventoryMenuMixin(MenuType<?> type, int id, int width, int height) {
        super(type, id, width, height);
    }

    // Preserve the network slot indices used by armor, inventory, hotbar and offhand.
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/inventory/InventoryMenu;addResultSlot(Lnet/minecraft/world/entity/player/Player;II)Lnet/minecraft/world/inventory/Slot;"))
    private Slot picklecraft$disableResult(InventoryMenu menu, Player player, int x, int y) {
        return addSlot(new DisabledSlot());
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/inventory/InventoryMenu;addCraftingGridSlots(II)V"))
    private void picklecraft$disableGrid(InventoryMenu menu, int x, int y) {
        for (int index = InventoryMenu.CRAFT_SLOT_START; index < InventoryMenu.CRAFT_SLOT_END; index++) {
            addSlot(new DisabledSlot());
        }
    }

    private static final class DisabledSlot extends Slot {
        private DisabledSlot() {
            super(new SimpleContainer(1), 0, -1000, -1000);
        }

        @Override public boolean isActive() { return false; }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
        @Override public void set(ItemStack stack) { }
    }
}
