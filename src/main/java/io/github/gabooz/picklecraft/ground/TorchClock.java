package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class TorchClock extends BlockEntity {
    // Elapsed server ticks preserve the lifetime when a creative player changes the time of day.
    public static final long BURN_TICKS = 40L * 24000 * 4;
    private long expires = -1;
    TorchClock(BlockPos pos, BlockState state) { super(GroundContent.TORCH_CLOCK.get(), pos, state); }
    void ignite() {
        expires = level.getGameTime() + BURN_TICKS;
        setChanged();
    }
    public void update(ServerLevel world) {
        if (expires < 0) ignite();
        if (world.getGameTime() >= expires) {
            world.setBlockAndUpdate(worldPosition, getBlockState().setValue(WorkshopTorch.LIT, false));
        }
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("expires", expires);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        expires = input.getLongOr("expires", -1);
    }
}
