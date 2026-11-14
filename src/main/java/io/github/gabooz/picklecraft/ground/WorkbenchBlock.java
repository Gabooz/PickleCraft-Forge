package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
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
        if (!level.isEmptyBlock(pos.above())) {
            return InteractionResult.FAIL;
        }
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

        if (player.isShiftKeyDown() && held.is(Items.IRON_INGOT)) {
            if (!level.isClientSide()) {
                var contents = bench.items();
                if (level.setBlockAndUpdate(pos, GroundContent.ANVIL.get().defaultBlockState())) {
                    held.consume(1, player);
                    contents.forEach(item -> popResource(level, pos.above(), item));
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_USE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.65F, 1.1F);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (size > 0 && held.isStackable()
                && ItemStack.isSameItemSameComponents(held, bench.items().getLast())) {
            if (!level.isClientSide()) {
                while (!bench.items().isEmpty() && held.getCount() < held.getMaxStackSize()
                        && ItemStack.isSameItemSameComponents(held, bench.items().getLast())) {
                    bench.pop();
                    held.grow(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (isTool(held)) {
            if (size == 0) {
                return InteractionResult.FAIL;
            }
            // New benches have one input. Older saves can still process their newest item.
            int index = size - 1;
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

        if (size != 0) {
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
        if (tool.is(Items.SHEARS)
                && (input.is(GroundContent.BLOSSOMS.get()) || input.is(GroundContent.PLUMARIA_ITEM.get()))) {
            return new ItemStack(Items.STRING, 2);
        }
        if (tool.is(Items.SHEARS) && input.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().is(BlockTags.WOOL)) {
            return new ItemStack(Items.STRING, 4);
        }
        return ItemStack.EMPTY;
    }
}
