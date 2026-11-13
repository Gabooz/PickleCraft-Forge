package io.github.gabooz.picklecraft.ground;

import com.mojang.serialization.MapCodec;
import io.github.gabooz.picklecraft.PickleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.*;
import java.util.Set;

public final class GroundContent {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, PickleCraft.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, PickleCraft.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PickleCraft.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, PickleCraft.MOD_ID);

    public static final RegistryObject<GroundStackBlock> STACK = BLOCKS.register("ground_stack", () ->
        new GroundStackBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("ground_stack"))
            .noCollision().noOcclusion().instabreak().pushReaction(PushReaction.POPPED)));
    public static final RegistryObject<PlumariaBlock> PLUMARIA = BLOCKS.register("plumaria", () ->
        new PlumariaBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("plumaria"))
            .noCollision().noOcclusion().instabreak().noLootTable().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED)));
    public static final RegistryObject<Item> STONE = ITEMS.register("loose_stone", () -> new Item(new Item.Properties().setId(ITEMS.key("loose_stone"))));
    public static final RegistryObject<Item> BLOSSOMS = ITEMS.register("plumaria_blossoms", () -> new Item(new Item.Properties().setId(ITEMS.key("plumaria_blossoms"))));
    public static final RegistryObject<BlockEntityType<GroundStack>> STACK_ENTITY = ENTITIES.register("ground_stack",
        () -> new BlockEntityType<>(GroundStack::new, Set.of(STACK.get())));
    public static final RegistryObject<BlockEntityType<PlumariaGrowth>> PLUMARIA_ENTITY = ENTITIES.register("plumaria",
        () -> new BlockEntityType<>(PlumariaGrowth::new, Set.of(PLUMARIA.get())));

    private GroundContent() {}

    public static void register(BusGroup bus) {
        FEATURES.register("forage", () -> ForageFeature.CODEC);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        FEATURES.register(bus);
    }

    public static boolean isMaterial(ItemStack stack) {
        return stack.is(Items.STICK) || stack.is(Items.STRING) || stack.is(STONE.get());
    }
}
