package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public final class WorkbenchBlock extends BaseEntityBlock {
    public static final double SURFACE_HEIGHT = 14.0 / 16.0;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

    public WorkbenchBlock(Properties properties) {
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
        var drops = new java.util.ArrayList<ItemStack>();
        drops.add(new ItemStack(GroundContent.WORKBENCH_ITEM.get()));
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof GroundStack stack) {
            drops.addAll(stack.items());
        }
        return drops;
    }

    static InteractionResult use(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var player = event.getEntity();
        var pos = event.getPos();
        var held = event.getItemStack();
        if (!(level.getBlockEntity(pos) instanceof GroundStack bench)) {
            return InteractionResult.PASS;
        }
        int size = bench.items().size();
        if (held.isEmpty()) {
            if (!level.isClientSide() && size > 0) {
                int count = player.isShiftKeyDown() ? size : 1;
                for (int i = 0; i < count; i++) {
                    var taken = bench.pop();
                    if (!player.addItem(taken)) {
                        popResource(level, pos.above(), taken);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (isTool(held)) {
            if (size == 0) {
                return InteractionResult.FAIL;
            }
            // A hit on an occupied cell targets that item. A side or empty cell targets the newest item.
            var hit = event.getHitVec();
            int index = size - 1;
            if (hit.getDirection() == Direction.UP) {
                int column = Math.clamp((int) ((hit.getLocation().x - pos.getX()) * 4), 0, 3);
                int row = Math.clamp((int) ((hit.getLocation().z - pos.getZ()) * 4), 0, 3);
                int cell = row * 4 + column;
                if (cell < size) {
                    index = cell;
                }
            }
            ItemStack output = transform(bench.items().get(index), held);
            if (output.isEmpty()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                bench.process(index, output);
                held.hurtAndBreak(1, player, event.getHand());
            }
            return InteractionResult.SUCCESS;
        }

        if (size == GroundStack.CAPACITY) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            bench.add(held);
            held.consume(1, player);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isTool(ItemStack item) {
        return item.has(DataComponents.TOOL) || item.is(ItemTags.SWORDS) || item.is(ItemTags.AXES)
            || item.is(ItemTags.SHOVELS) || item.is(ItemTags.PICKAXES) || item.is(ItemTags.HOES) || item.is(Items.SHEARS);
    }

    private static ItemStack transform(ItemStack input, ItemStack tool) {
        if (tool.is(ItemTags.AXES) && input.getItem() instanceof BlockItem blockItem) {
            var state = blockItem.getBlock().defaultBlockState();
            if (state.is(BlockTags.PLANKS)) {
                return new ItemStack(Items.STICK, 4);
            }
            if (state.is(BlockTags.LOGS)) {
                var blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
                String wood = blockId.getPath().replaceFirst("^stripped_", "")
                    .replaceFirst("_(log|wood|stem|hyphae)$", "");
                var plank = BuiltInRegistries.ITEM.getOptional(
                    blockId.withPath(wood + "_planks"));
                return new ItemStack(plank.orElse(Items.OAK_PLANKS));
            }
        }
        if (tool.is(ItemTags.PICKAXES) && input.is(Items.COBBLESTONE)) {
            return new ItemStack(Items.GRAVEL);
        }
        if (tool.is(ItemTags.SHOVELS) && input.is(Items.GRAVEL)) {
            return new ItemStack(Items.FLINT);
        }
        if (tool.is(Items.SHEARS) && input.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().is(BlockTags.WOOL)) {
            return new ItemStack(Items.STRING, 4);
        }
        return ItemStack.EMPTY;
    }
}
