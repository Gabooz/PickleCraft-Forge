package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Keeps the original timber so dismantling the trim returns the correct log. */
final class StockpilePost extends BlockEntity {
    private ItemStack timber = Items.OAK_LOG.getDefaultInstance();

    StockpilePost(BlockPos pos, BlockState state) {
        super(GroundContent.STOCKPILE_POST_ENTITY.get(), pos, state);
    }

    void setTimber(ItemStack timber) {
        this.timber = timber;
        setChanged();
    }

    ItemStack timber() {
        return timber.copy();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("timber", ItemStack.CODEC, timber);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        timber = input.read("timber", ItemStack.CODEC).orElseGet(Items.OAK_LOG::getDefaultInstance);
    }
}
