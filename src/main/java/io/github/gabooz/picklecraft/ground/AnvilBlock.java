package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public final class AnvilBlock extends BaseEntityBlock {
    private static final int CAPACITY = 4;
    private static final List<Item> TOOLS = List.of(
        Items.IRON_PICKAXE, Items.IRON_SWORD, Items.IRON_SHOVEL, Items.IRON_AXE);
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(2, 0, 2, 14, 4, 14), Block.box(4, 4, 3, 12, 10, 13),
        Block.box(3, 10, 0, 13, 16, 16));

    public AnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public GroundStack newBlockEntity(BlockPos pos, BlockState state) {
        return new GroundStack(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(GroundContent.ANVIL_ITEM.get()));
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof GroundStack anvil) {
            drops.addAll(anvil.items());
        }
        return drops;
    }

    static InteractionResult use(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var player = event.getEntity();
        var held = event.getItemStack();
        if (!(level.getBlockEntity(pos) instanceof GroundStack anvil)) {
            return InteractionResult.PASS;
        }
        var items = anvil.items();
        boolean shaping = anvil.isShaping();
        if (held.isEmpty() && !items.isEmpty()) {
            if (!level.isClientSide()) {
                if (player.isShiftKeyDown()) {
                    var taken = anvil.pop();
                    if (!player.addItem(taken)) {
                        popResource(level, pos.above(), taken);
                    }
                } else if (shaping) {
                    int next = (TOOLS.indexOf(items.getFirst().getItem()) + 1) % TOOLS.size();
                    anvil.shape(new ItemStack(TOOLS.get(next)));
                } else if (items.size() == 2 && items.stream().anyMatch(item -> item.is(Items.STICK))
                        && items.stream().anyMatch(item -> item.is(Items.IRON_INGOT))) {
                    anvil.shape(new ItemStack(TOOLS.getFirst()));
                } else {
                    var taken = anvil.pop();
                    if (!player.addItem(taken)) {
                        popResource(level, pos.above(), taken);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty() || shaping || items.size() >= CAPACITY || !level.isEmptyBlock(pos.above())) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            anvil.add(held);
            held.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }
}
