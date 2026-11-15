package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public final class Stockpile extends BlockEntity {
    private final List<ItemStack> stacks = new ArrayList<>();
    private int size = 3;
    private int offsetX;
    private int offsetZ;

    Stockpile(BlockPos pos, BlockState state) {
        super(GroundContent.STOCKPILE_ENTITY.get(), pos, state);
    }

    void initialize(BlockPos corner, int size) {
        this.size = size;
        offsetX = corner.getX() - worldPosition.getX();
        offsetZ = corner.getZ() - worldPosition.getZ();
        sync();
    }

    public BlockPos corner() {
        return worldPosition.offset(offsetX, 0, offsetZ);
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return size * size * size;
    }

    public ItemStack sample() {
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.getFirst();
    }

    public float fill() {
        if (stacks.isEmpty()) {
            return 0;
        }
        float occupied = 0;
        for (var stack : stacks) {
            occupied += stack.getCount() / (float) stack.getMaxStackSize();
        }
        return occupied / capacity();
    }

    List<ItemStack> drops() {
        return List.copyOf(stacks);
    }

    void insert(Player player, ItemStack held, boolean single) {
        if (!stacks.isEmpty() && !sample().is(held.getItem())) {
            return;
        }
        int remaining = single ? 1 : held.getCount();
        int inserted = 0;
        // Fill compatible partial stacks first. Item data stays intact for tools and other variants.
        for (var stack : stacks) {
            if (remaining == 0) break;
            if (ItemStack.isSameItemSameComponents(stack, held)) {
                int moved = Math.min(remaining, stack.getMaxStackSize() - stack.getCount());
                stack.grow(moved);
                inserted += moved;
                remaining -= moved;
            }
        }
        while (remaining > 0 && stacks.size() < capacity()) {
            int moved = Math.min(remaining, held.getMaxStackSize());
            stacks.add(held.copyWithCount(moved));
            inserted += moved;
            remaining -= moved;
        }
        if (inserted > 0) {
            held.consume(inserted, player);
            sync();
        }
    }

    void take(Player player, boolean single) {
        if (stacks.isEmpty()) {
            return;
        }
        var last = stacks.getLast();
        var output = single ? last.split(1) : stacks.removeLast();
        if (single && last.isEmpty()) {
            stacks.removeLast();
        }
        if (!player.addItem(output)) {
            Block.popResource(level, worldPosition.above(), output);
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
        writeLayout(output);
        output.store("stacks", ItemStack.CODEC.listOf(), stacks);
    }

    private void writeLayout(ValueOutput output) {
        output.putInt("size", size);
        output.putInt("offset_x", offsetX);
        output.putInt("offset_z", offsetZ);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        size = Math.clamp(input.getIntOr("size", 3), 3, 9);
        offsetX = Math.clamp(input.getIntOr("offset_x", 0), 1 - size, 0);
        offsetZ = Math.clamp(input.getIntOr("offset_z", 0), 1 - size, 0);
        stacks.clear();
        var saved = input.read("stacks", ItemStack.CODEC.listOf()).orElse(List.of());
        if (saved.isEmpty()) {
            // Client updates carry only a sample and fullness, never the full inventory.
            sampleFromUpdate = input.read("sample", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            fillFromUpdate = input.getFloatOr("fill", 0);
        } else {
            saved.stream().filter(stack -> !stack.isEmpty()).limit(capacity()).forEach(stacks::add);
            sampleFromUpdate = ItemStack.EMPTY;
            fillFromUpdate = 0;
        }
    }

    private ItemStack sampleFromUpdate = ItemStack.EMPTY;
    private float fillFromUpdate;

    public ItemStack displayItem() {
        return stacks.isEmpty() ? sampleFromUpdate : sample();
    }

    public float displayFill() {
        return stacks.isEmpty() ? fillFromUpdate : fill();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
            net.minecraft.util.ProblemReporter.DISCARDING, registries);
        writeLayout(output);
        if (!stacks.isEmpty()) output.store("sample", ItemStack.CODEC, sample().copyWithCount(1));
        output.putFloat("fill", fill());
        return output.buildResult();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
