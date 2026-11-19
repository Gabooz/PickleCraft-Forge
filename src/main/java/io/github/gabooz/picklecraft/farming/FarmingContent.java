package io.github.gabooz.picklecraft.farming;

import io.github.gabooz.picklecraft.PickleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import java.util.Set;

public final class FarmingContent {
    private static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, PickleCraft.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PickleCraft.MOD_ID);
    public static final RegistryObject<SeasonalWheatBlock> WHEAT = BLOCKS.register("wheat", () ->
        new SeasonalWheatBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("wheat"))
            .noCollision().noOcclusion().instabreak().sound(SoundType.CROP)));
    public static final RegistryObject<BlockEntityType<WheatGrowth>> GROWTH = ENTITIES.register("wheat_growth", () ->
        new BlockEntityType<>(WheatGrowth::new, Set.of(WHEAT.get())));
    private FarmingContent() {}
    public static void register(BusGroup bus) { BLOCKS.register(bus); ENTITIES.register(bus); }

    /** Keep the familiar vanilla seeds and route planting to PickleCraft's timed crop. */
    public static boolean plant(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var soil = event.getPos();
        if (!event.getItemStack().is(net.minecraft.world.item.Items.WHEAT_SEEDS)
                || event.getHitVec().getDirection() != net.minecraft.core.Direction.UP
                || !(level.getBlockState(soil).getBlock() instanceof net.minecraft.world.level.block.FarmlandBlock)) return false;
        var pos = soil.above();
        if (!player.mayBuild() || !level.mayInteract(player, pos) || !level.isEmptyBlock(pos)) return false;
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, WHEAT.get().defaultBlockState());
            if (level instanceof net.minecraft.server.level.ServerLevel server
                    && level.getBlockEntity(pos) instanceof WheatGrowth growth) growth.update(server);
            event.getItemStack().consume(1, player);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.CROP_PLANTED, net.minecraft.sounds.SoundSource.BLOCKS, 1, 1);
        }
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        return true;
    }
}
