package io.github.gabooz.picklecraft.ground;

import io.github.gabooz.picklecraft.PickleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class Sailboat extends Boat {
    private static final DeferredRegister<EntityType<?>> TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, PickleCraft.MOD_ID);
    public static final RegistryObject<EntityType<Sailboat>> TYPE = TYPES.register("sailboat", () ->
        EntityType.Builder.of(Sailboat::new, MobCategory.MISC).sized(1.375F, 0.5625F)
            .eyeHeight(0.5625F).clientTrackingRange(10).noLootTable().build(TYPES.key("sailboat")));

    private Sailboat(EntityType<Sailboat> type, Level level) {
        super(type, level, GroundContent.SAILBOAT::get);
    }

    public static void register(BusGroup bus) {
        TYPES.register(bus);
    }
}
