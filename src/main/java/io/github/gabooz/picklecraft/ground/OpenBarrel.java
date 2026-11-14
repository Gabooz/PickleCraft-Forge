package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public final class OpenBarrel extends BlockEntity {
    public static final int CAPACITY = 64;
    private final List<ItemStack> contents = new ArrayList<>();
    private int count;

    OpenBarrel(BlockPos pos, BlockState state) {
        super(GroundContent.BARREL_ENTITY.get(), pos, state);
    }

    public List<ItemStack> contents() {
        return List.copyOf(contents);
    }

    void insert(Player player, ItemStack held, boolean single) {
        int amount = Math.min(CAPACITY - count, single ? 1 : held.getCount());
        if (amount == 0) {
            return;
        }
        append(held.copyWithCount(amount));
        held.consume(amount, player);
        sync();
    }

    private void append(ItemStack stack) {
        if (!contents.isEmpty()) {
            var last = contents.getLast();
            if (ItemStack.isSameItemSameComponents(last, stack)
                    && last.getCount() + stack.getCount() <= last.getMaxStackSize()) {
                last.grow(stack.getCount());
                count += stack.getCount();
                return;
            }
        }
        contents.add(stack);
        count += stack.getCount();
    }

    void take(Player player, boolean single) {
        if (contents.isEmpty()) {
            return;
        }
        var type = contents.getLast().getItem();
        // Group by item type while keeping each stack's components and durability.
        do {
            var last = contents.getLast();
            var output = single ? last.split(1) : contents.removeLast();
            if (single && last.isEmpty()) {
                contents.removeLast();
            }
            count -= output.getCount();
            if (!player.addItem(output)) {
                Block.popResource(level, worldPosition.above(), output);
            }
        } while (!single && !contents.isEmpty() && contents.getLast().is(type));
        sync();
    }

    void spill() {
        if (contents.isEmpty()) {
            return;
        }
        contents.forEach(stack -> Block.popResource(level, worldPosition.above(), stack));
        contents.clear();
        count = 0;
        sync();
    }

    void coolTongs(ServerLevel server) {
        if (server.getGameTime() % 20 != 0) {
            return;
        }
        var cooled = new ArrayList<ItemStack>();
        for (var stack : contents) {
            if (IronTongs.cool(stack, server)) {
                cooled.add(IronTongs.release(stack));
            }
        }
        if (cooled.isEmpty()) {
            return;
        }
        for (var ingot : cooled) {
            if (count < CAPACITY) {
                append(ingot);
            } else {
                Block.popResource(server, worldPosition.above(), ingot);
            }
        }
        sync();
    }

    private void sync() {
        setChanged();
        var state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("contents", ItemStack.CODEC.listOf(), contents);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        contents.clear();
        count = 0;
        for (var stack : input.read("contents", ItemStack.CODEC.listOf()).orElse(List.of())) {
            if (count == CAPACITY) {
                break;
            }
            if (!stack.isEmpty()) {
                append(stack.copyWithCount(Math.min(stack.getCount(), CAPACITY - count)));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
