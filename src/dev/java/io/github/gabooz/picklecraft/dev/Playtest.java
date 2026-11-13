package io.github.gabooz.picklecraft.dev;

import io.github.gabooz.picklecraft.client.SurvivalInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.event.TickEvent;

import java.util.Map;

/** Opt-in development check using a fresh world and a real integrated server and client. */
public final class Playtest {
    private boolean started;
    private boolean checked;

    public static void register() {
        TickEvent.ClientTickEvent.Post.BUS.addListener(new Playtest()::tick);
    }

    private void tick(TickEvent.ClientTickEvent.Post event) {
        var game = Minecraft.getInstance();
        if (!started && game.isGameLoadFinished()) {
            started = true;
            String name = "PickleCraft playtest " + System.currentTimeMillis();
            game.createWorldOpenFlows().createFreshLevel(name,
                new LevelSettings(name, GameType.SURVIVAL, LevelSettings.DifficultySettings.DEFAULT,
                    true, WorldDataConfiguration.DEFAULT),
                new WorldOptions(12345L, true, false), WorldPresets::createNormalWorldDimensions, game.gui.screen());
        } else if (!checked && game.player != null && game.getSingleplayerServer() != null) {
            checked = true;
            game.gui.setScreen(new InventoryScreen(game.player));
            require(game.gui.screen() instanceof SurvivalInventoryScreen, "Survival inventory screen");
            require(DefaultPlayerSkin.get(game.player.getUUID()).body().id().getPath().equals("entity/player/wide/steve"),
                "Wide Steve skin");
            game.getSingleplayerServer().execute(() -> {
                try {
                    verifyServer(game.getSingleplayerServer().overworld(), game.getSingleplayerServer().getPlayerList().getPlayers().getFirst());
                    System.out.println("PICKLECRAFT PLAYTEST PASSED");
                    game.execute(game::stop);
                } catch (RuntimeException | AssertionError error) {
                    System.err.println("PICKLECRAFT PLAYTEST FAILED");
                    error.printStackTrace();
                    game.execute(() -> { throw new IllegalStateException("PickleCraft playtest failed", error); });
                }
            });
        }
    }

    private static void verifyServer(net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player) {
        var pos = player.blockPosition().below();
        for (var entry : Map.<Block, Integer>of(Blocks.OAK_LOG, 120, Blocks.DIRT, 30, Blocks.STONE, 300,
                Blocks.SAND, 15, Blocks.COBBLESTONE, 60).entrySet()) {
            float progress = entry.getKey().defaultBlockState().getDestroyProgress(player, level, pos);
            require(Math.abs(progress * 20 * entry.getValue() - 1) < 0.00001F, "Mining time for " + entry.getKey());
        }
        require(Blocks.OAK_LEAVES.defaultBlockState().getDestroyProgress(player, level, pos) > 1F / 1200, "Leaves break quickly");
        require(!player.inventoryMenu.getSlot(0).isActive() && player.inventoryMenu.getSlot(5).isActive(), "Inventory slot indices");
        require(level.recipeAccess().getRecipes().stream().noneMatch(recipe ->
            recipe.id().identifier().getNamespace().equals("minecraft") && recipe.value().getType() == RecipeType.CRAFTING),
            "Vanilla crafting recipes");

        var oak = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)
            .getOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath("picklecraft", "sparse_oak")));
        var biome = level.getBiome(pos).value();
        require(biome.getGenerationSettings().features().get(Decoration.VEGETAL_DECORATION.ordinal()).contains(oak),
            "Sparse oak feature enabled in the Overworld");
        require(!biome.shouldSnow(level, new BlockPos(pos.getX(), 250, pos.getZ())), "No snow at high altitude");
    }

    private static void require(boolean condition, String check) {
        if (!condition) {
            throw new AssertionError(check);
        }
    }
}
