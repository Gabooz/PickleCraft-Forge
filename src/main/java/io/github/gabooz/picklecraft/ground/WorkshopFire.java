package io.github.gabooz.picklecraft.ground;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

final class WorkshopFire {
    private WorkshopFire() {}

    static InteractionResult interact(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var player = event.getEntity();
        if (!level.mayInteract(player, pos)) return InteractionResult.PASS;
        var state = level.getBlockState(pos);
        var held = event.getItemStack();
        if (state.is(GroundContent.TIMED_TORCH.get()) && held.is(Items.FLINT_AND_STEEL)) {
            if (state.getValue(WorkshopTorch.LIT) || state.getValue(WorkshopTorch.WET)) return InteractionResult.FAIL;
            if (!level.isClientSide()) {
                level.setBlockAndUpdate(pos, state.setValue(WorkshopTorch.LIT, true));
                if (level.getBlockEntity(pos) instanceof TorchClock clock) clock.ignite();
                held.hurtAndBreak(1, player, event.getHand());
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1, 1);
            }
            return InteractionResult.SUCCESS;
        }
        if (!(state.getBlock() instanceof CampfireBlock)) return InteractionResult.PASS;
        if (held.is(Items.STICK) && state.getValue(CampfireBlock.LIT)) {
            if (!level.isClientSide()) {
                held.consume(1, player);
                Block.popResource(level, pos.above(), Items.TORCH.getDefaultInstance());
            }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty() && level.getBlockEntity(pos) instanceof CampfireBlockEntity fire) {
            // The vanilla campfire displays these same four slots on its corners.
            int selected = -1;
            double best = Double.POSITIVE_INFINITY;
            var facing = state.getValue(CampfireBlock.FACING);
            var hit = event.getHitVec().getLocation();
            for (int slot = 0; slot < 4; slot++) {
                if (fire.getItems().get(slot).isEmpty()) continue;
                var corner = net.minecraft.core.Direction.from2DDataValue((slot + facing.get2DDataValue()) % 4);
                var side = corner.getClockWise();
                double dx = hit.x - pos.getX() - 0.5 - (corner.getStepX() + side.getStepX()) * 0.24;
                double dz = hit.z - pos.getZ() - 0.5 - (corner.getStepZ() + side.getStepZ()) * 0.24;
                double distance = dx * dx + dz * dz;
                if (distance < best) { best = distance; selected = slot; }
            }
            if (selected >= 0) {
                if (!level.isClientSide()) {
                    var item = fire.getItems().set(selected, ItemStack.EMPTY);
                    if (!player.addItem(item)) Block.popResource(level, pos.above(), item);
                    fire.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
