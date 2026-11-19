package io.github.gabooz.picklecraft.zombies;

import io.github.gabooz.picklecraft.PickleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ZombieContent {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, PickleCraft.MOD_ID);
    public static final RegistryObject<EntityType<ZoneZombie>> ZOMBIE = ENTITIES.register("zone_zombie", () ->
        EntityType.Builder.of(ZoneZombie::new, MobCategory.MONSTER).sized(0.6F, 1.95F)
            .clientTrackingRange(12).build(ENTITIES.key("zone_zombie")));

    private ZombieContent() {}

    public static void register(BusGroup bus) {
        ENTITIES.register(bus);
        EntityAttributeCreationEvent.BUS.addListener(event -> event.put(ZOMBIE.get(), Zombie.createAttributes().build()));
        ZombieZoneNetwork.register();
        ZombieZones.register();
    }
}
