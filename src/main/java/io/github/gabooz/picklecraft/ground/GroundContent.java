package io.github.gabooz.picklecraft.ground;

import com.mojang.serialization.MapCodec;
import io.github.gabooz.picklecraft.PickleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PickleCraft.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PickleCraft.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, PickleCraft.MOD_ID);

    public static final RegistryObject<GroundStackBlock> STACK = BLOCKS.register("ground_stack", () ->
        new GroundStackBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("ground_stack"))
            .noCollision().noOcclusion().instabreak().pushReaction(PushReaction.POPPED)));
    public static final RegistryObject<PlumariaBlock> PLUMARIA = BLOCKS.register("plumaria", () ->
        new PlumariaBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("plumaria"))
            .noCollision().noOcclusion().instabreak().noLootTable().sound(SoundType.GRASS).pushReaction(PushReaction.POPPED)));
    public static final RegistryObject<WorkbenchBlock> WORKBENCH = BLOCKS.register("workbench", () ->
        new WorkbenchBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("workbench"))
            .strength(2F).noOcclusion().sound(SoundType.WOOD).pushReaction(PushReaction.IMMOVEABLE)));
    public static final RegistryObject<BlockItem> WORKBENCH_ITEM = ITEMS.register("workbench", () ->
        new BlockItem(WORKBENCH.get(), new Item.Properties().setId(ITEMS.key("workbench")).useBlockDescriptionPrefix()));

    public static final RegistryObject<AnvilBlock> ANVIL = BLOCKS.register("anvil", () ->
        new AnvilBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("anvil"))
            .strength(5F).noOcclusion().sound(SoundType.ANVIL).pushReaction(PushReaction.IMMOVEABLE)));
    public static final RegistryObject<BlockItem> ANVIL_ITEM = ITEMS.register("anvil", () ->
        new BlockItem(ANVIL.get(), new Item.Properties().setId(ITEMS.key("anvil")).useBlockDescriptionPrefix()));

    public static final RegistryObject<Item> STONE = ITEMS.register("loose_stone", () -> new Item(new Item.Properties().setId(ITEMS.key("loose_stone"))));
    public static final RegistryObject<Item> BLOSSOMS = ITEMS.register("plumaria_blossoms", () -> new Item(new Item.Properties().setId(ITEMS.key("plumaria_blossoms"))));
    public static final RegistryObject<BlockEntityType<GroundStack>> STACK_ENTITY = ENTITIES.register("ground_stack",
        () -> new BlockEntityType<>(GroundStack::new, Set.of(STACK.get(), WORKBENCH.get(), ANVIL.get())));
    public static final RegistryObject<BlockEntityType<PlumariaGrowth>> PLUMARIA_ENTITY = ENTITIES.register("plumaria",
        () -> new BlockEntityType<>(PlumariaGrowth::new, Set.of(PLUMARIA.get())));

    public static final RegistryObject<BlockItem> PLUMARIA_ITEM = ITEMS.register("plumaria", () ->
        new BlockItem(PLUMARIA.get(), new Item.Properties().setId(ITEMS.key("plumaria")).useBlockDescriptionPrefix()));
    public static final RegistryObject<IronFurnaceBlock> IRON_FURNACE = BLOCKS.register("iron_furnace", () ->
        new IronFurnaceBlock(BlockBehaviour.Properties.of().setId(BLOCKS.key("iron_furnace"))
            .strength(3F).noOcclusion().sound(SoundType.STONE).pushReaction(PushReaction.IMMOVEABLE)
            .lightLevel(state -> state.getValue(IronFurnaceBlock.LIT) ? 13 : 0)));
    public static final RegistryObject<BlockEntityType<IronFurnace>> FURNACE_ENTITY = ENTITIES.register("iron_furnace",
        () -> new BlockEntityType<>(IronFurnace::new, Set.of(IRON_FURNACE.get())));

    public static final RegistryObject<BlockItem> IRON_FURNACE_ITEM = ITEMS.register("iron_furnace", () ->
        new IronFurnaceItem(IRON_FURNACE.get(), new Item.Properties().setId(ITEMS.key("iron_furnace"))
            .useBlockDescriptionPrefix()));

    public static final RegistryObject<Item> IRON_TONGS = ITEMS.register("iron_tongs", () ->
        new IronTongs(new Item.Properties().setId(ITEMS.key("iron_tongs")).stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("picklecraft", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.picklecraft"))
            .icon(() -> BLOSSOMS.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(WORKBENCH_ITEM.get());
                output.accept(ANVIL_ITEM.get());
                output.accept(Items.IRON_PICKAXE);
                output.accept(Items.IRON_SWORD);
                output.accept(Items.IRON_SHOVEL);
                output.accept(Items.IRON_AXE);
                output.accept(IRON_FURNACE_ITEM.get());
                output.accept(IRON_TONGS.get());
                output.accept(Items.COBBLESTONE);
                output.accept(Items.COAL);
                output.accept(Items.CHARCOAL);
                output.accept(Items.RAW_IRON);
                output.accept(Items.IRON_ORE);
                output.accept(Items.DEEPSLATE_IRON_ORE);
                output.accept(Items.IRON_INGOT);
                output.accept(STONE.get());
                output.accept(PLUMARIA_ITEM.get());
                output.accept(BLOSSOMS.get());
                output.accept(Items.STICK);
                output.accept(Items.STRING);
                output.accept(Items.STONE_PICKAXE);
                output.accept(Items.STONE_SWORD);
                output.accept(Items.STONE_SHOVEL);
                output.accept(Items.STONE_AXE);
            }).build());

    private GroundContent() {}

    public static void register(BusGroup bus) {
        FEATURES.register("forage", () -> ForageFeature.CODEC);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TABS.register(bus);
        ENTITIES.register(bus);
        FEATURES.register(bus);
    }

    public static boolean isMaterial(ItemStack stack) {
        return stack.is(Items.STICK) || stack.is(Items.STRING) || stack.is(STONE.get());
    }
}
