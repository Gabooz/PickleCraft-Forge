package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;

final class IronFurnaceItem extends BlockItem {
    IronFurnaceItem(IronFurnaceBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        var state = super.getPlacementState(context);
        if (state == null) {
            return null;
        }
        var player = context.getPlayer();
        Direction front = player == null ? Direction.NORTH : player.getDirection().getOpposite();
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, front);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var upper = pos.above();
        var player = context.getPlayer();
        return level.isInWorldBounds(upper) && level.isEmptyBlock(upper)
            && (player == null || level.mayInteract(player, upper))
            && level.isUnobstructed(state.setValue(IronFurnaceBlock.TOP, true), upper, CollisionContext.placementContext(player))
            && IronFurnaceBlock.placePair(level, pos, state);
    }
}
