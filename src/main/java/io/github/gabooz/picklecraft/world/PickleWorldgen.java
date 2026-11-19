package io.github.gabooz.picklecraft.world;

import com.mojang.serialization.MapCodec;
import io.github.gabooz.picklecraft.PickleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;

public final class PickleWorldgen {
    public static final ResourceKey<WorldPreset> PRESET = ResourceKey.create(Registries.WORLD_PRESET,
        Identifier.fromNamespaceAndPath(PickleCraft.MOD_ID, "picklecraft"));
    private static final DeferredRegister<MapCodec<? extends BiomeSource>> SOURCES =
        DeferredRegister.create(Registries.BIOME_SOURCE, PickleCraft.MOD_ID);
    private static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITIES =
        DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, PickleCraft.MOD_ID);

    private PickleWorldgen() {}

    public static void register(BusGroup bus) {
        SOURCES.register("hex_regions", () -> HexBiomeSource.CODEC);
        DENSITIES.register("hex_terrain", () -> HexTerrain.CODEC);
        SOURCES.register(bus);
        DENSITIES.register(bus);
    }
}
