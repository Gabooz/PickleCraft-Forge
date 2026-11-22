package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import java.util.ArrayList;
import java.util.List;

public final class CarpenterTable extends BaseEntityBlock {
    static final EnumProperty<Direction> SIDE = BlockStateProperties.HORIZONTAL_FACING;
    static final BooleanProperty END = BooleanProperty.create("end");
    private static final int CAPACITY = 5;
    private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 11, 0, 16, 16, 16),
        Block.box(2, 0, 2, 14, 11, 14));

    CarpenterTable(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SIDE, Direction.EAST).setValue(END, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIDE, END);
    }
    @Override public GroundStack newBlockEntity(BlockPos pos, BlockState state) {
        return new GroundStack(pos, state);
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(END)) return List.of();
        var drops = new ArrayList<ItemStack>();
        drops.add(GroundContent.CARPENTER_KIT.get().getDefaultInstance());
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof GroundStack stack) drops.addAll(stack.items());
        return drops;
    }

    private static BlockPos partner(BlockPos pos, BlockState state) {
        Direction side = state.getValue(SIDE);
        return pos.relative(state.getValue(END) ? side.getOpposite() : side);
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) return;
        var otherPos = partner(pos, state);
        if (!level.hasChunkAt(otherPos)) return;
        var other = level.getBlockState(otherPos);
        if (!other.is(this) || other.getValue(END) == state.getValue(END) || other.getValue(SIDE) != state.getValue(SIDE)) {
            level.destroyBlock(pos, !state.getValue(END));
        }
    }

    private static boolean install(Level level, BlockPos pos, Direction side) {
        var other = pos.relative(side);
        var first = GroundContent.CARPENTER.get().defaultBlockState().setValue(SIDE, side);
        var previous = level.getBlockState(pos);
        if (!level.setBlock(pos, first, Block.UPDATE_CLIENTS)) return false;
        if (!level.setBlock(other, first.setValue(END, true), Block.UPDATE_CLIENTS)) {
            level.setBlockAndUpdate(pos, previous);
            return false;
        }
        level.updateNeighborsAt(pos, first.getBlock());
        level.updateNeighborsAt(other, first.getBlock());
        return true;
    }

    static InteractionResult placeKit(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;
        var level = context.getLevel();
        var pos = context.getClickedPos().relative(context.getClickedFace());
        var side = player.getDirection().getClockWise();
        var other = pos.relative(side);
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(other)
                || !player.mayBuild() || !level.mayInteract(player, pos) || !level.mayInteract(player, other)) return InteractionResult.FAIL;
        if (!level.isClientSide() && install(level, pos, side)) context.getItemInHand().consume(1, player);
        return InteractionResult.SUCCESS;
    }

    static InteractionResult interact(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var held = event.getItemStack();
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        var saw = GroundContent.SAW.get();
        if (player.isShiftKeyDown() && held.is(saw) && state.is(net.minecraft.tags.BlockTags.PLANKS)) {
            var preferred = event.getHitVec().getDirection();
            if (!preferred.getAxis().isHorizontal()) preferred = player.getDirection().getClockWise();
            for (var side : List.of(preferred, preferred.getOpposite(), preferred.getClockWise(), preferred.getCounterClockWise())) {
                var other = pos.relative(side);
                if (!level.hasChunkAt(other) || !level.getBlockState(other).is(net.minecraft.tags.BlockTags.PLANKS)
                        || !level.isEmptyBlock(pos.above()) || !level.isEmptyBlock(other.above())
                        || !level.mayInteract(player, pos) || !level.mayInteract(player, other)) continue;
                if (!level.isClientSide() && install(level, pos, side)) held.hurtAndBreak(1, player, event.getHand());
                return InteractionResult.SUCCESS;
            }
        }
        if (!state.is(GroundContent.CARPENTER.get())) return InteractionResult.PASS;
        var other = partner(pos, state);
        if (!level.hasChunkAt(other) || !level.mayInteract(player, pos) || !level.mayInteract(player, other)) return InteractionResult.FAIL;
        var anchor = state.getValue(END) ? other : pos;
        if (!(level.getBlockEntity(anchor) instanceof GroundStack stack)) return InteractionResult.FAIL;
        var ingredients = stack.items();
        if (held.isEmpty()) {
            if (!level.isClientSide() && !ingredients.isEmpty()) {
                var item = stack.pop();
                if (!player.addItem(item)) popResource(level, anchor.above(), item);
            }
            return InteractionResult.SUCCESS;
        }
        boolean makeOar = ingredients.size() == 2 && held.is(saw)
            && ingredients.stream().anyMatch(item -> item.is(Items.STICK))
            && ingredients.stream().anyMatch(CarpenterTable::isWoodSlab);
        if (makeOar) {
            if (!level.isClientSide()) {
                stack.replaceWith(GroundContent.OAR.get().getDefaultInstance());
                held.hurtAndBreak(1, player, event.getHand());
            }
            return InteractionResult.SUCCESS;
        }
        if (GroundContent.isHammer(held) && ingredients.size() == 5
                && ingredients.stream().filter(item -> item.is(Items.IRON_INGOT)).count() == 3
                && ingredients.stream().filter(item -> item.is(ItemTags.PLANKS)).count() == 2) {
            if (!level.isClientSide()) {
                stack.replaceWith(GroundContent.TRAIN_ITEM.get().getDefaultInstance());
                held.hurtAndBreak(1, player, event.getHand());
            }
            return InteractionResult.SUCCESS;
        }
        if (held.is(saw) || ingredients.size() >= CAPACITY || !ingredient(held)) return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            stack.add(held);
            held.consume(1, player);
            if (boatReady(stack.items())) stack.replaceWith(boatFor(stack.items()));
            if (sailboatReady(stack.items())) stack.replaceWith(GroundContent.SAILBOAT.get().getDefaultInstance());
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isWoodSlab(ItemStack item) { return item.is(ItemTags.WOODEN_SLABS); }
    private static boolean adhesive(ItemStack item) { return item.is(Items.SLIME_BALL) || item.is(GroundContent.GLUE.get()); }
    private static boolean ingredient(ItemStack item) {
        return isWoodSlab(item) || adhesive(item) || item.is(Items.STICK) || item.is(GroundContent.OAR.get())
            || item.is(Items.OAK_BOAT) || item.is(Items.WOOL.white())
            || item.is(Items.IRON_INGOT) || item.is(ItemTags.PLANKS);
    }
    private static boolean sailboatReady(List<ItemStack> items) {
        return items.size() == 3 && items.stream().anyMatch(item -> item.is(Items.OAK_BOAT))
            && items.stream().anyMatch(item -> item.is(Items.WOOL.white()))
            && items.stream().anyMatch(item -> item.is(Items.STICK));
    }
    private static boolean boatReady(List<ItemStack> items) {
        return items.size() == 5 && items.stream().filter(CarpenterTable::adhesive).count() == 1
            && items.stream().filter(CarpenterTable::isWoodSlab).count() == 2
            && items.stream().filter(item -> item.is(GroundContent.OAR.get())).count() == 2;
    }
    private static ItemStack boatFor(List<ItemStack> items) {
        var slab = items.stream().filter(CarpenterTable::isWoodSlab).findFirst().orElseThrow();
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(slab.getItem());
        return net.minecraft.core.registries.BuiltInRegistries.ITEM
            .getOptional(id.withPath(id.getPath().replace("_slab", "_boat")))
            .orElse(Items.OAK_BOAT).getDefaultInstance();
    }
}
