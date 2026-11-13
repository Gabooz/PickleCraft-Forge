package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import java.util.ArrayList;
import java.util.List;

public final class GroundStack extends BlockEntity {
    public static final int CAPACITY = 16;
    private final List<ItemStack> items = new ArrayList<>();

    public GroundStack(BlockPos pos, BlockState state) {
        super(GroundContent.STACK_ENTITY.get(), pos, state);
    }

    public List<ItemStack> items() {
        return List.copyOf(items);
    }

    void add(ItemStack item) {
        items.add(item.copyWithCount(1));
        sync();
    }

    ItemStack pop() {
        ItemStack item = items.removeLast();
        sync();
        return item;
    }

    void replaceWith(ItemStack item) {
        items.clear();
        add(item);
    }

    void process(int index, ItemStack output) {
        items.set(index, output.copyWithCount(1));
        int extra = output.getCount() - 1;
        while (extra > 0 && items.size() < CAPACITY) {
            items.add(output.copyWithCount(1));
            extra--;
        }
        sync();
        if (extra > 0) {
            Block.popResource(level, worldPosition.above(), output.copyWithCount(extra));
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            var state = getBlockState();
            var updated = state.hasProperty(GroundStackBlock.LAYERS)
                ? state.setValue(GroundStackBlock.LAYERS, Math.max(1, items.size())) : state;
            if (updated != state) {
                level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
            }
            level.sendBlockUpdated(worldPosition, state, updated, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("items", ItemStack.CODEC.listOf(), items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        input.read("items", ItemStack.CODEC.listOf()).orElse(List.of()).stream()
            .filter(item -> !item.isEmpty()).limit(CAPACITY).map(item -> item.copyWithCount(1)).forEach(items::add);
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
