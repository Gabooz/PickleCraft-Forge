package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import io.github.gabooz.picklecraft.ground.StockpileTrimBlock.Part;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public final class StockpileBlock extends BaseEntityBlock {
    public StockpileBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Stockpile newBlockEntity(BlockPos pos, BlockState state) {
        return new Stockpile(pos, state);
    }

    static InteractionResult interact(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        var player = event.getEntity();
        var held = event.getItemStack();
        if (!(state.is(Blocks.STONE_BRICKS) || state.is(BlockTags.LOGS) || state.is(Blocks.DIRT_PATH)
                || state.is(GroundContent.STOCKPILE.get()) || state.is(GroundContent.STOCKPILE_TRIM.get())) || !level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }
        var existing = find(level, pos, state.is(Blocks.DIRT_PATH) || isPart(state, Part.FLOOR));
        if (existing != null) {
            if (!level.mayInteract(player, existing.getBlockPos())
                    || !matches(level, existing.corner(), existing.size(), existing.getBlockPos())) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) {
                if (canShape(level, existing.corner(), existing.size(), player)) {
                    shape(level, existing.corner(), existing.size(), existing.getBlockPos());
                }
                if (held.isEmpty()) {
                    existing.take(player, player.isShiftKeyDown());
                } else {
                    existing.insert(player, held, player.isShiftKeyDown());
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!state.is(Blocks.STONE_BRICKS) || !player.isShiftKeyDown() || !GroundContent.isHammer(held)) {
            return InteractionResult.PASS;
        }
        for (int size = 3; size <= 9; size++) {
            // The hammer strikes a side, so only test corners consistent with that edge cell.
            for (int along = 1; along < size - 1; along++) {
                for (var corner : List.of(pos.offset(-along, 0, 0), pos.offset(-along, 0, 1 - size),
                        pos.offset(0, 0, -along), pos.offset(1 - size, 0, -along))) {
                    if ((!level.getBlockState(corner).is(BlockTags.LOGS) && !isPart(level.getBlockState(corner), Part.POST)) || !matches(level, corner, size, null)) {
                        continue;
                    }
                    if (!canShape(level, corner, size, player)) {
                        return InteractionResult.FAIL;
                    }
                    if (!level.isClientSide() && level.setBlockAndUpdate(pos, GroundContent.STOCKPILE.get().defaultBlockState())
                            && level.getBlockEntity(pos) instanceof Stockpile pile) {
                        pile.initialize(corner, size);
                        shape(level, corner, size, pos);
                        held.hurtAndBreak(1, player, event.getHand());
                        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1F, 0.65F);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private static Stockpile find(Level level, BlockPos pos, boolean floor) {
        if (level.getBlockEntity(pos) instanceof Stockpile pile) {
            return pile;
        }
        var rim = floor ? pos.above() : pos;
        for (var candidate : BlockPos.betweenClosed(rim.offset(-8, 0, -8), rim.offset(8, 0, 8))) {
            if (level.hasChunkAt(candidate) && level.getBlockEntity(candidate) instanceof Stockpile pile) {
                int x = pos.getX() - pile.corner().getX();
                int z = pos.getZ() - pile.corner().getZ();
                int edge = pile.size() - 1;
                if (x >= 0 && x <= edge && z >= 0 && z <= edge
                        && floor == (x > 0 && x < edge && z > 0 && z < edge)) {
                    return pile;
                }
            }
        }
        return null;
    }

    private static boolean isPart(BlockState state, Part part) {
        return state.is(GroundContent.STOCKPILE_TRIM.get()) && state.getValue(StockpileTrimBlock.PART) == part;
    }

    private static boolean matches(Level level, BlockPos corner, int size, BlockPos controller) {
        int edge = size - 1;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                var point = corner.offset(x, 0, z);
                if (!level.hasChunkAt(point)) return false;
                if (point.equals(controller)) continue;
                boolean xEdge = x == 0 || x == edge;
                boolean zEdge = z == 0 || z == edge;
                var state = level.getBlockState(point);
                if (xEdge && zEdge) {
                    if (!state.is(BlockTags.LOGS) && !isPart(state, Part.POST)) return false;
                } else if (xEdge || zEdge) {
                    if (!state.is(Blocks.STONE_BRICKS) && !isPart(state, Part.RIM)) return false;
                } else {
                    var floor = level.getBlockState(point.below());
                    if (!state.isAir() || (!floor.is(Blocks.DIRT_PATH) && !isPart(floor, Part.FLOOR))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean canShape(Level level, BlockPos corner, int size, net.minecraft.world.entity.player.Player player) {
        int edge = size - 1;
        for (var point : BlockPos.betweenClosed(corner, corner.offset(edge, 0, edge))) {
            boolean inside = point.getX() > corner.getX() && point.getX() < corner.getX() + edge
                && point.getZ() > corner.getZ() && point.getZ() < corner.getZ() + edge;
            if (!level.mayInteract(player, inside ? point.below() : point)) return false;
        }
        return true;
    }

    static void shape(Level level, BlockPos corner, int size, BlockPos controller) {
        var trim = GroundContent.STOCKPILE_TRIM.get().defaultBlockState();
        int edge = size - 1;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                var point = corner.offset(x, 0, z);
                if (point.equals(controller)) continue;
                boolean xEdge = x == 0 || x == edge;
                boolean zEdge = z == 0 || z == edge;
                var part = xEdge && zEdge ? Part.POST : xEdge || zEdge ? Part.RIM : Part.FLOOR;
                if (part == Part.FLOOR) point = point.below();
                var front = z == 0 ? Direction.NORTH : z == edge ? Direction.SOUTH
                    : x == 0 ? Direction.WEST : Direction.EAST;
                var shaped = trim.setValue(StockpileTrimBlock.PART, part)
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, part == Part.RIM ? front : Direction.NORTH);
                var before = level.getBlockState(point);
                if (before != shaped && level.setBlockAndUpdate(point, shaped) && part == Part.POST
                        && level.getBlockEntity(point) instanceof StockpilePost post) {
                    post.setTimber(before.getBlock().asItem().getDefaultInstance());
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof Stockpile pile) || !pile.displayItem().is(Items.REDSTONE)) {
            return;
        }
        double center = pile.size() / 2.0;
        double spread = (pile.size() - 2) * 0.35 * Math.sqrt(pile.displayFill());
        var corner = pile.corner();
        level.addParticle(new DustParticleOptions(0xff3c38, 0.45F),
            corner.getX() + center + (random.nextDouble() * 2 - 1) * spread,
            pos.getY() + 0.08 + pile.displayFill() * 0.4,
            corner.getZ() + center + (random.nextDouble() * 2 - 1) * spread, 0, 0.02, 0);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var items = new ArrayList<ItemStack>();
        items.add(Items.STONE_BRICKS.getDefaultInstance());
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof Stockpile pile) {
            items.addAll(pile.drops());
        }
        return items;
    }
}
