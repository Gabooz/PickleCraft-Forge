package io.github.gabooz.picklecraft.world;

import com.mojang.serialization.MapCodec;
import io.github.gabooz.picklecraft.PickleCraft;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo.Builder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/** Removes vanilla decoration while leaving the terrain, carvers, and biome source in place. */
public final class OverworldFeatures implements BiomeModifier {
    public static final MapCodec<OverworldFeatures> CODEC = MapCodec.unit(new OverworldFeatures());
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, PickleCraft.MOD_ID);

    static {
        SERIALIZERS.register("strip_overworld", () -> CODEC);
    }

    private static final Set<String> TERRAIN_DEPOSITS = Set.of(
        "ore_dirt", "ore_gravel", "ore_granite_upper", "ore_granite_lower",
        "ore_diorite_upper", "ore_diorite_lower", "ore_andesite_upper",
        "ore_andesite_lower", "ore_tuff", "ore_clay"
    );

    @Override
    public void modify(Holder<Biome> biome, Phase phase, Builder builder) {
        if (phase != Phase.REMOVE || !biome.is(BiomeTags.IS_OVERWORLD)) {
            return;
        }
        builder.attributes().set(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettings.EMPTY);
        for (Decoration step : Decoration.values()) {
            builder.generationSettings().getFeatures(step).removeIf(feature ->
                feature.unwrapKey().map(key -> key.identifier().getNamespace().equals("minecraft")
                    && shouldRemove(step, key.identifier().getPath())).orElse(false));
        }
    }

    private static boolean shouldRemove(Decoration step, String path) {
        return step == Decoration.VEGETAL_DECORATION
            || step == Decoration.SURFACE_STRUCTURES
            || step == Decoration.UNDERGROUND_STRUCTURES
            || (path.startsWith("ore_") && !TERRAIN_DEPOSITS.contains(path))
            || path.startsWith("sculk_")
            || path.contains("rooted")
            || path.equals("amethyst_geode");
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
