package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public final class OpenBarrelBlock extends BaseEntityBlock {
    private static final VoxelShape SHELL = Shapes.or(
        box(0, 0, 0, 16, 1, 16), box(0, 1, 0, 1, 16, 16), box(15, 1, 0, 16, 16, 16),
        box(1, 1, 0, 15, 16, 1), box(1, 1, 15, 15, 16, 16));

    public OpenBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public OpenBarrel newBlockEntity(BlockPos pos, BlockState state) {
        return new OpenBarrel(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHELL;
    }

    @Override
    public <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel server
            ? createTickerHelper(type, GroundContent.BARREL_ENTITY.get(), (world, pos, block, barrel) -> barrel.coolTongs(server))
            : null;
    }

    static InteractionResult use(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof OpenBarrel barrel)) {
            return InteractionResult.PASS;
        }
        if (!event.getLevel().isClientSide()) {
            var player = event.getEntity();
            var held = event.getItemStack();
            if (held.isEmpty()) {
                barrel.take(player, player.isShiftKeyDown());
            } else {
                barrel.insert(player, held, player.isShiftKeyDown());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var drops = new ArrayList<ItemStack>();
        drops.add(GroundContent.OPEN_BARREL_ITEM.get().getDefaultInstance());
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof OpenBarrel barrel) {
            drops.addAll(barrel.contents());
        }
        return drops;
    }
}
