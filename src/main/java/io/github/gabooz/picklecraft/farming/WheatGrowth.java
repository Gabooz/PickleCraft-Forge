package io.github.gabooz.picklecraft.farming;

import io.github.gabooz.picklecraft.seasons.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Clock timestamps survive chunk unloading; a crop cannot skip winter by being unloaded. */
public final class WheatGrowth extends BlockEntity {
    public static final long GROWTH_TIME = CropSchedule.WHEAT.growthDays() * Seasons.DAY;
    private long planted = Long.MIN_VALUE;
    private long checkedDay;
    public WheatGrowth(BlockPos pos, BlockState state) { super(FarmingContent.GROWTH.get(), pos, state); }
    public void update(ServerLevel world) {
        var state = getBlockState();
        if (state.getValue(SeasonalWheatBlock.DEAD)) return;
        long now = world.getDefaultClockTime();
        long day = Math.floorDiv(now, Seasons.DAY) + Seasons.get(world).offset();
        if (planted == Long.MIN_VALUE) {
            planted = now - state.getValue(net.minecraft.world.level.block.CropBlock.AGE) * GROWTH_TIME / 7;
            checkedDay = day;
        }
        boolean dead = !CropSchedule.WHEAT.survives(Seasons.date(world))
            || !CropSchedule.WHEAT.survivedInterval(checkedDay, day);
        checkedDay = day;
        setChanged();
        if (dead) world.setBlockAndUpdate(worldPosition, state.setValue(SeasonalWheatBlock.DEAD, true));
        else {
            int age = (int) Math.clamp(Math.max(0, now - planted) * 7 / GROWTH_TIME, 0, 7);
            if (state.getValue(net.minecraft.world.level.block.CropBlock.AGE) != age)
                world.setBlockAndUpdate(worldPosition, state.setValue(net.minecraft.world.level.block.CropBlock.AGE, age));
        }
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); output.putLong("planted", planted); output.putLong("checked_calendar_day", checkedDay);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); planted = input.getLongOr("planted", Long.MIN_VALUE); checkedDay = input.getLongOr("checked_calendar_day", 0);
    }
}
