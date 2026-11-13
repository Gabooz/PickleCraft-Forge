package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class GroundInteractions {
    private GroundInteractions() {}

    public static boolean use(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().mayBuild()) {
            return false;
        }
        var level = event.getLevel();
        var pos = event.getPos();
        var held = event.getItemStack();
        InteractionResult result;
        if (level.getBlockState(pos).is(GroundContent.STACK.get()) && level.mayInteract(event.getEntity(), pos)) {
            result = GroundStackBlock.interact(level, pos, event.getEntity(), held);
        } else if (event.getHitVec().getDirection() == Direction.UP && GroundContent.isMaterial(held)
                && level.mayInteract(event.getEntity(), pos.above()) && level.isEmptyBlock(pos.above())
                && GroundStackBlock.supported(level, pos.above())) {
            if (!level.isClientSide() && GroundStackBlock.place(level, pos.above(), held)) {
                held.consume(1, event.getEntity());
            }
            result = InteractionResult.SUCCESS;
        } else {
            return false;
        }
        event.setCancellationResult(result);
        return true;
    }

    public static boolean hit(PlayerInteractEvent.LeftClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || !event.getEntity().mayBuild() || !event.getEntity().isWithinBlockInteractionRange(pos, 1.0)
                || !level.mayInteract(event.getEntity(), pos)
                || !level.getBlockState(pos).is(GroundContent.STACK.get())) {
            return false;
        }
        if (!level.isClientSide()) {
            level.destroyBlock(pos, true);
        }
        return true;
    }
}
