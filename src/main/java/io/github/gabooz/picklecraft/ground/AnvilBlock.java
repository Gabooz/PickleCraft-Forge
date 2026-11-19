package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
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
        if (!player.isShiftKeyDown() && level.isEmptyBlock(pos.above())) {
            ItemStack result = ItemStack.EMPTY;
            boolean hotIron = items.isEmpty() && IronTongs.holdsIron(held);
            if (hotIron) {
                if (level instanceof net.minecraft.server.level.ServerLevel server && IronTongs.cool(held, server)) {
                    return InteractionResult.FAIL;
                }
                if (!level.isClientSide()) {
                    long deadline = IronTongs.deadline(held);
                    var ingot = IronTongs.release(held);
                    net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        ingot, data -> data.putLong("picklecraft_forge_until", deadline));
                    ingot.set(net.minecraft.core.component.DataComponents.ITEM_MODEL,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("picklecraft", "hot_ingot"));
                    anvil.add(ingot);
                    held.hurtAndBreak(1, player, event.getHand());
                }
                return InteractionResult.SUCCESS;
            } else if (items.size() == 1 && (held.isEmpty() || GroundContent.isHammer(held))) {
                var workpiece = items.getFirst();
                long deadline = workpiece.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getLongOr("picklecraft_forge_until", -1);
                if (deadline >= 0) {
                    if (!level.isClientSide()) {
                        if (level.getGameTime() >= deadline) {
                            anvil.convertTool(Items.IRON_INGOT.getDefaultInstance());
                        } else {
                            anvil.convertTool(GroundContent.SAW_BLADE.get().getDefaultInstance());
                            if (!held.isEmpty()) held.hurtAndBreak(1, player, event.getHand());
                            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_USE,
                                net.minecraft.sounds.SoundSource.BLOCKS, 0.55F, 1.2F);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
                if (GroundContent.isHammer(held) && workpiece.is(net.minecraft.tags.ItemTags.PLANKS)) {
                    result = GroundContent.SAW_HANDLE.get().getDefaultInstance();
                }
            } else if (GroundContent.isHammer(held)) {
                if (items.size() == 2 && items.stream().anyMatch(item -> item.is(Items.FLINT))
                        && items.stream().anyMatch(item -> item.is(Items.IRON_INGOT))) {
                    result = Items.FLINT_AND_STEEL.getDefaultInstance();
                } else if (items.size() == 2 && items.stream().anyMatch(item -> item.is(GroundContent.SAW_HANDLE.get()))
                        && items.stream().anyMatch(item -> item.is(GroundContent.SAW_BLADE.get()))) {
                    result = GroundContent.SAW.get().getDefaultInstance();
                }
            }
            if (!result.isEmpty()) {
                if (!level.isClientSide()) {
                    anvil.convertTool(result);
                    held.hurtAndBreak(1, player, event.getHand());
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_USE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.55F, 1.2F);
                }
                return InteractionResult.SUCCESS;
            }
        }
        // Re-materializing one placed tool is distinct from hammering loose ingredients.
        var converted = items.size() == 1 ? ToolConversions.convert(items.getFirst(), held) : ItemStack.EMPTY;
        if (!converted.isEmpty() && !player.isShiftKeyDown() && level.isEmptyBlock(pos.above())) {
            if (!level.isClientSide()) {
                anvil.convertTool(converted);
                held.consume(1, player);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_USE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.55F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        boolean shaping = anvil.isShaping();
        boolean ready = items.size() == 2 && items.stream().anyMatch(item -> item.is(Items.STICK))
            && items.stream().anyMatch(item -> item.is(Items.IRON_INGOT));
        // An empty anvil accepts the hammer as the workpiece for a material upgrade.
        if (GroundContent.isHammer(held) && !items.isEmpty() && !player.isShiftKeyDown()) {
            if ((!ready && !shaping) || !level.isEmptyBlock(pos.above())) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                var tools = List.of(Items.IRON_PICKAXE, Items.IRON_SWORD, Items.IRON_SHOVEL, Items.IRON_AXE,
                    GroundContent.IRON_HAMMER.get());
                int next = shaping ? (tools.indexOf(items.getFirst().getItem()) + 1) % tools.size() : 0;
                anvil.shape(new ItemStack(tools.get(next)));
                held.hurtAndBreak(1, player, event.getHand());
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_USE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.55F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        // Sneaking retrieves the result or last ingredient, even with the hammer still in hand.
        if (!items.isEmpty() && (held.isEmpty() || (GroundContent.isHammer(held) && player.isShiftKeyDown()))) {
            if (!level.isClientSide()) {
                var taken = anvil.pop();
                if (!player.addItem(taken)) {
                    popResource(level, pos.above(), taken);
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
