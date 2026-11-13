package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.*;
import java.util.List;

public final class GroundStackBlock extends BaseEntityBlock {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, GroundStack.CAPACITY);
    private static final List<Item> TOOLS = List.of(Items.STONE_PICKAXE, Items.STONE_SWORD, Items.STONE_SHOVEL, Items.STONE_AXE);

    public GroundStackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    public GroundStack newBlockEntity(BlockPos pos, BlockState state) {
        return new GroundStack(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(2, 0, 2, 14, state.getValue(LAYERS), 14);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof GroundStack stack
            ? stack.items() : List.of();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide() && !supported(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    static boolean supported(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    public static boolean place(LevelAccessor level, BlockPos pos, ItemStack item) {
        if (!level.isEmptyBlock(pos) || !supported(level, pos)
                || !level.setBlock(pos, GroundContent.STACK.get().defaultBlockState(), Block.UPDATE_ALL)) {
            return false;
        }
        if (level.getBlockEntity(pos) instanceof GroundStack stack) {
            stack.add(item);
            return true;
        }
        level.removeBlock(pos, false);
        return false;
    }

    static InteractionResult interact(Level level, BlockPos pos, Player player, ItemStack held) {
        if (!(level.getBlockEntity(pos) instanceof GroundStack stack)) {
            return InteractionResult.PASS;
        }
        var items = stack.items();
        if (items.isEmpty()) {
            return InteractionResult.PASS;
        }
        int tool = items.size() == 1 ? TOOLS.indexOf(items.getFirst().getItem()) : -1;
        if (!player.isShiftKeyDown() && !held.isEmpty()) {
            if (tool >= 0 || !GroundContent.isMaterial(held) || items.size() == GroundStack.CAPACITY) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                stack.add(held);
                held.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            if (!player.isShiftKeyDown() && tool >= 0) {
                stack.replaceWith(new ItemStack(TOOLS.get((tool + 1) % TOOLS.size())));
            } else if (!player.isShiftKeyDown() && formsTool(items)) {
                stack.replaceWith(new ItemStack(Items.STONE_PICKAXE));
            } else {
                ItemStack taken = stack.pop();
                if (stack.items().isEmpty()) {
                    level.removeBlock(pos, false);
                }
                if (!player.addItem(taken)) {
                    popResource(level, pos, taken);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean formsTool(List<ItemStack> items) {
        return items.size() == 3 && items.stream().anyMatch(item -> item.is(Items.STRING))
            && items.stream().anyMatch(item -> item.is(Items.STICK))
            && items.stream().anyMatch(item -> item.is(GroundContent.STONE.get()));
    }
}
