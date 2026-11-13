package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class PlumariaGrowth extends BlockEntity {
    long regrowAt;

    public PlumariaGrowth(BlockPos pos, BlockState state) {
        super(GroundContent.PLUMARIA_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("regrow_at", regrowAt);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        regrowAt = input.getLongOr("regrow_at", 0);
    }
}
