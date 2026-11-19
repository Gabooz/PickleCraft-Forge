package io.github.gabooz.picklecraft.cattle;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class CattleContent {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, "picklecraft");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "picklecraft");
    public static final RegistryObject<EntityType<HerdCow>> COW = ENTITIES.register("herd_cow", () ->
        EntityType.Builder.of(HerdCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F)
            .clientTrackingRange(10).build(ENTITIES.key("herd_cow")));
    public static final RegistryObject<EntityType<HerdCow>> BULL = ENTITIES.register("bull", () ->
        EntityType.Builder.of(HerdCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F)
            .clientTrackingRange(10).build(ENTITIES.key("bull")));
    public static final RegistryObject<Item> COW_EGG = ITEMS.register("herd_cow_spawn_egg", () ->
        new SpawnEggItem(new Item.Properties().setId(ITEMS.key("herd_cow_spawn_egg")).spawnEgg(COW.get())));
    public static final RegistryObject<Item> BULL_EGG = ITEMS.register("bull_spawn_egg", () ->
        new SpawnEggItem(new Item.Properties().setId(ITEMS.key("bull_spawn_egg")).spawnEgg(BULL.get())));

    private CattleContent() {}

    public static void register(BusGroup bus) {
        ENTITIES.register(bus);
        ITEMS.register(bus);
        EntityAttributeCreationEvent.BUS.addListener(event -> {
            event.put(COW.get(), AbstractCow.createAttributes().build());
            event.put(BULL.get(), AbstractCow.createAttributes().build());
        });
        CattleHerds.register();
    }
}
