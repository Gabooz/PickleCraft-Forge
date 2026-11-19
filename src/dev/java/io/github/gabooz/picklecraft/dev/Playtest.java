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
    private int joinedTicks;
    private boolean awaitingMap;
    private int mapTicks;
    private boolean screenshotRequested;
    private boolean awaitingTeleport;
    private int teleportTicks;
    private double beforeTeleportX;

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
                new WorldOptions(12345L, true, false), registries -> registries.lookupOrThrow(Registries.WORLD_PRESET)
                    .getOrThrow(io.github.gabooz.picklecraft.world.PickleWorldgen.PRESET).value().createWorldDimensions(), game.gui.screen());
        } else if (!checked && game.player != null && game.getSingleplayerServer() != null && ++joinedTicks >= 80) {
            checked = true;
            var server = game.getSingleplayerServer();
            var preset = server.registryAccess().lookupOrThrow(Registries.WORLD_PRESET)
                .getOrThrow(io.github.gabooz.picklecraft.world.PickleWorldgen.PRESET);
            var context = new net.minecraft.client.gui.screens.worldselection.WorldCreationContext(
                new net.minecraft.world.level.levelgen.WorldGenSettings(new WorldOptions(12345L, true, false),
                    preset.value().createWorldDimensions()),
                server.registries(), server.getServerResources().managers(), WorldDataConfiguration.DEFAULT);
            var creation = new net.minecraft.client.gui.screens.worldselection.WorldCreationUiState(
                java.nio.file.Path.of("saves"), context, java.util.Optional.of(WorldPresets.NORMAL),
                java.util.OptionalLong.empty());
            require(creation.getWorldType().preset().is(io.github.gabooz.picklecraft.world.PickleWorldgen.PRESET),
                "World creation defaults to PickleCraft");
            require(creation.getNormalPresetList().stream().anyMatch(entry -> entry.preset() != null
                && entry.preset().is(io.github.gabooz.picklecraft.world.PickleWorldgen.PRESET)),
                "PickleCraft appears in the world type selector");
            var tab = io.github.gabooz.picklecraft.ground.GroundContent.TAB.get();
            tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(
                game.level.enabledFeatures(), true, game.level.registryAccess()));
            require(tab.getDisplayItems().size() == 50, "PickleCraft creative tab contains all fifty entries");
            try (var fogRenderer = new net.minecraft.client.renderer.fog.FogRenderer()) {
                io.github.gabooz.picklecraft.client.ZombieZoneClient.receive(true);
                var fog = fogRenderer.setupFog(game.gameRenderer.mainCamera(), 8, game.getDeltaTracker(), 0, game.level);
                require(fog.renderDistanceEnd == 15 && fog.environmentalEnd <= 15,
                    "Daytime zombie zone fog limits visibility to fifteen blocks");
                var clock = game.level.dimensionType().defaultClock().orElseThrow();
                var clockState = game.level.clockManager().getInstance(clock);
                long priorTime = clockState.totalTicks();
                float priorPartial = clockState.partialTick();
                float priorRate = clockState.rate();
                game.level.clockManager().handleUpdates(game.level.getGameTime(),
                    Map.of(clock, new net.minecraft.world.clock.ClockNetworkState(18000, 0, 0.25F)));
                var dark = fogRenderer.setupFog(game.gameRenderer.mainCamera(), 8, game.getDeltaTracker(), 0, game.level);
                require(dark.renderDistanceEnd == 7, "Nighttime fog stays seven blocks without light");
                var lamp = game.player.blockPosition().above(3);
                var oldLamp = game.level.getBlockState(lamp);
                game.level.setBlock(lamp, Blocks.GLOWSTONE.defaultBlockState(), 3);
                game.level.getChunkSource().getLightEngine().checkBlock(lamp);
                game.level.getChunkSource().getLightEngine().runLightUpdates();
                var lit = fogRenderer.setupFog(game.gameRenderer.mainCamera(), 8, game.getDeltaTracker(), 0, game.level);
                require(lit.renderDistanceEnd == 12, "Nearby light extends nighttime fog to twelve blocks");
                game.level.setBlock(lamp, Blocks.CAMPFIRE.defaultBlockState(), 3);
                io.github.gabooz.picklecraft.client.ZombieZoneClient.receive(true);
                var campfireFog = fogRenderer.setupFog(game.gameRenderer.mainCamera(), 8, game.getDeltaTracker(), 0, game.level);
                require(campfireFog.renderDistanceEnd == 21, "Campfire extends nighttime vision to twenty-one blocks");
                game.level.clockManager().handleUpdates(game.level.getGameTime(),
                    Map.of(clock, new net.minecraft.world.clock.ClockNetworkState(6000, 0, 0.25F)));
                var litDay = fogRenderer.setupFog(game.gameRenderer.mainCamera(), 8, game.getDeltaTracker(), 0, game.level);
                require(litDay.renderDistanceEnd == 15, "Daytime fog stays fifteen blocks near light");
                game.level.setBlock(lamp, oldLamp, 3);
                game.level.getChunkSource().getLightEngine().checkBlock(lamp);
                game.level.getChunkSource().getLightEngine().runLightUpdates();
                game.level.clockManager().handleUpdates(game.level.getGameTime(),
                    Map.of(clock, new net.minecraft.world.clock.ClockNetworkState(priorTime, priorPartial, priorRate)));
                game.player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.NIGHT_VISION, 200));
                var nightVision = fogRenderer.setupFog(game.gameRenderer.mainCamera(), 8, game.getDeltaTracker(), 0, game.level);
                require(nightVision.renderDistanceEnd > 15, "Night Vision removes the zone fog");
                game.player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
                io.github.gabooz.picklecraft.client.ZombieZoneClient.receive(false);
                var clear = fogRenderer.setupFog(game.gameRenderer.mainCamera(), 8, game.getDeltaTracker(), 0, game.level);
                require(clear.renderDistanceEnd > 7, "Cleared zone restores ordinary visibility");
            }
            int springColor = net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(game.level, game.player.blockPosition());
            io.github.gabooz.picklecraft.client.SeasonClient.receive(20);
            int autumnColor = net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(game.level, game.player.blockPosition());
            require(springColor != autumnColor, "Season calendar changes the actual foliage renderer tint");
            io.github.gabooz.picklecraft.client.SeasonClient.receive(0);
            game.gui.setScreen(new InventoryScreen(game.player));
            require(game.gui.screen() instanceof SurvivalInventoryScreen, "Survival inventory screen");
            require(DefaultPlayerSkin.get(game.player.getUUID()).body().id().getPath().equals("entity/player/wide/steve"),
                "Wide Steve skin");
            game.getSingleplayerServer().execute(() -> {
                try {
                    verifyServer(game.getSingleplayerServer().overworld(), game.getSingleplayerServer().getPlayerList().getPlayers().getFirst());
                    verifyTerrainHeights(game.getSingleplayerServer().overworld());
                    var player = game.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
                    var map = io.github.gabooz.picklecraft.ground.GroundContent.ZONE_MAP.get();
                    require(map.use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND)
                        == net.minecraft.world.InteractionResult.FAIL, "Survival players cannot open the creative map");
                    player.setGameMode(GameType.CREATIVE);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, map.getDefaultInstance());
                    require(map.use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND).consumesAction(),
                        "Right-click opens the map for a creative player");
                    game.execute(() -> awaitingMap = true);
                } catch (RuntimeException | AssertionError error) {
                    System.err.println("PICKLECRAFT PLAYTEST FAILED");
                    error.printStackTrace();
                    game.execute(() -> { throw new IllegalStateException("PickleCraft playtest failed", error); });
                }
            });
        }
        if (awaitingMap && !screenshotRequested) {
            mapTicks++;
            if (game.gui.screen() instanceof io.github.gabooz.picklecraft.client.ZoneMapScreen map && mapTicks >= 15) {
                var button = new net.minecraft.client.input.MouseButtonInfo(
                    com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT, 0);
                var click = new net.minecraft.client.input.MouseButtonEvent(80, 80, button);
                require(map.mouseClicked(click, false), "Map accepts a pan drag");
                require(map.mouseDragged(new net.minecraft.client.input.MouseButtonEvent(120, 95, button), 40, 15),
                    "Map pans through its mouse entry point");
                require(map.mouseReleased(new net.minecraft.client.input.MouseButtonEvent(120, 95, button)),
                    "Map releases its pan drag");
                require(map.mouseScrolled(100, 100, 0, 1), "Map zooms through its mouse entry point");
                require(map.mouseScrolled(100, 100, 0, -1), "Map zooms back out");
                require(map.keyPressed(new net.minecraft.client.input.KeyEvent(
                    com.mojang.blaze3d.platform.InputConstants.KEY_R, 0, 0)), "Map centers on the live player");
                screenshotRequested = true;
                net.minecraft.client.Screenshot.grab(game.gameDirectory, "zone-map.png", game.gameRenderer.mainRenderTarget(), 1,
                    message -> game.execute(() -> {
                        beforeTeleportX = game.player.getX();
                        var right = new net.minecraft.client.input.MouseButtonInfo(
                            com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT, 0);
                        require(map.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(
                            map.width / 2.0 + 35, (34 + map.height - 86) / 2.0, right), false),
                            "Right-click requests map teleport");
                        awaitingTeleport = true;
                    }));
            } else if (mapTicks > 200) {
                throw new AssertionError("Creative map packet did not open its screen");
            }
        }
        if (awaitingTeleport) {
            if (Math.abs(game.player.getX() - beforeTeleportX) > 500) {
                require(!(game.gui.screen() instanceof io.github.gabooz.picklecraft.client.ZoneMapScreen),
                    "Map closes after teleporting");
                com.mojang.logging.LogUtils.getLogger().info("PICKLECRAFT PLAYTEST PASSED");
                awaitingTeleport = false;
                game.stop();
            } else if (++teleportTicks > 400) throw new AssertionError("Map teleport packet did not move the player");
        }
    }

    private static void verifyTerrainHeights(net.minecraft.server.level.ServerLevel level) {
        var sampler = level.getChunkSource().randomState().createClimateSampler(
            net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED);
        long seed = io.github.gabooz.picklecraft.world.HexRegions.seed(sampler);
        var kinds = java.util.EnumSet.noneOf(io.github.gabooz.picklecraft.world.HexRegions.Kind.class);
        for (int q = -6; q <= 6 && kinds.size() < 3; q++) {
            for (int r = -6; r <= 6 && kinds.size() < 3; r++) {
                var cell = io.github.gabooz.picklecraft.world.HexRegions.cell(q, r, seed);
                if (kinds.contains(cell.kind()) || cell.kind().ordinal() > 2) continue;
                int x = (int) Math.round(io.github.gabooz.picklecraft.world.HexRegions.SIDE * Math.sqrt(3) * (q + r / 2.0));
                int z = (int) Math.round(io.github.gabooz.picklecraft.world.HexRegions.SIDE * 1.5 * r);
                if (io.github.gabooz.picklecraft.world.OceanTerrain.continent(x, z, seed) < 0.14) continue;
                int height = level.getChunkSource().getGenerator().getBaseHeight(x, z,
                    net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG, level, level.getChunkSource().randomState());
                require(height >= 55 && height <= (cell.kind() ==
                    io.github.gabooz.picklecraft.world.HexRegions.Kind.MOUNTAINS ? 320 : 110),
                    "Biome surface height stays in the intended lowland or mountain range");
                kinds.add(cell.kind());
                com.mojang.logging.LogUtils.getLogger().info("ZONE HEIGHT biome={}, y={}", cell.kind(), height);
            }
        }
        require(kinds.size() == 3, "Checked the heights of all three biome types");
    }

    private static void verifyServer(net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player) {
        verifyHexWorld(level);
        if (System.getenv("PICKLECRAFT_TEST_FOCUS") == null) verifyZoneMines(level);
        verifyZombieRosters(level, player);
        verifyCattle(level, player);
        require(!io.github.gabooz.picklecraft.map.ZoneMapNetwork.teleport(player, 0, 0),
            "Survival players cannot use map teleport packets");
        verifyBones(level, player);
        verifySeasonsAndFarming(level, player);
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
        verifyGroundCraft(level, player);
        verifyWorkbench(level, player);
        verifyIronFurnace(level, player);
        verifyGoldFurnace(level, player);
        verifyOpenBarrel(level, player);
        verifyStockpile(level, player);
        require(level.dimensionType().defaultClock().map(clock -> level.clockManager().getInstance(clock).rate() == 0.25F).orElse(false),
            "Overworld clock runs at quarter speed");
        require(!biome.shouldSnow(level, new BlockPos(pos.getX(), 250, pos.getZ())), "No snow at high altitude");
    }

    private static void verifyHexWorld(net.minecraft.server.level.ServerLevel level) {
        var source = level.getChunkSource().getGenerator().getBiomeSource();
        require(source instanceof io.github.gabooz.picklecraft.world.HexBiomeSource, "PickleCraft world preset uses hex regions");
        var sampler = level.getChunkSource().randomState().createClimateSampler(
            net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED);
        long seed = io.github.gabooz.picklecraft.world.HexRegions.seed(sampler);
        var seen = java.util.EnumSet.noneOf(io.github.gabooz.picklecraft.world.HexRegions.Kind.class);
        var counts = new int[io.github.gabooz.picklecraft.world.HexRegions.Kind.values().length];
        int mountainLinks = 0;
        for (int q = -18; q <= 18; q++) {
            for (int r = -18; r <= 18; r++) {
                var cell = io.github.gabooz.picklecraft.world.HexRegions.cell(q, r, seed);
                seen.add(cell.kind());
                counts[cell.kind().ordinal()]++;
                if (cell.kind() == io.github.gabooz.picklecraft.world.HexRegions.Kind.MOUNTAINS) {
                    for (int[] delta : new int[][]{{1,0},{0,1},{1,-1}}) {
                        if (io.github.gabooz.picklecraft.world.HexRegions.cell(q+delta[0],r+delta[1],seed).kind() == cell.kind()) {
                            mountainLinks++;
                        }
                    }
                }
                require(io.github.gabooz.picklecraft.world.BiomeChallenges.select(cell).isPresent()
                    == io.github.gabooz.picklecraft.zombies.ZombieZones.assigned(cell),
                    "Zombie challenge selection follows grassland eligibility");
            }
        }
        require(seen.size() == counts.length && counts[0] > counts[2] && mountainLinks > 60,
            "Grasslands, mountain chains and slightly scarcer forests occur across hex regions");
        verifyOcean(level, seed);
        var resolver = source.createResolver(sampler);
        var ops = net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, level.registryAccess());
        var encoded = net.minecraft.world.level.biome.BiomeSource.CODEC.encodeStart(ops, source).getOrThrow();
        var decoded = net.minecraft.world.level.biome.BiomeSource.CODEC.parse(ops, encoded).getOrThrow().createResolver(sampler);
        var vanilla = net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(
            level.registryAccess().lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getOrThrow(
                ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, Identifier.withDefaultNamespace("overworld"))))
            .createResolver(sampler);
        int riverSamples = 0;
        for (int x = -4000; x <= 4000; x += 100) {
            for (int z = -4000; z <= 4000; z += 100) {
                var original = vanilla.getNoiseBiome(x, 20, z);
                var actual = resolver.getNoiseBiome(x, 20, z);
                boolean river = original.is(net.minecraft.world.level.biome.Biomes.RIVER)
                    || original.is(net.minecraft.world.level.biome.Biomes.FROZEN_RIVER);
                boolean inland = io.github.gabooz.picklecraft.world.OceanTerrain.continent(x * 4.0, z * 4.0, seed) > 0.14;
                if (river && inland) riverSamples++;
                require(!inland || actual.is(net.minecraft.world.level.biome.Biomes.RIVER) == river,
                    "River selection follows vanilla climate independently of hex borders");
                require(decoded.getNoiseBiome(x, 20, z).equals(actual), "Saved biome source reproduces the same map");
            }
        }
        require(riverSamples > 0, "Winding river biomes occur across the world");

        var biomes = source.possibleBiomes();
        require(biomes.size() == 7, "Ocean, beach and stony shore are declared alongside inland biomes");
        for (int x = -16000; x <= 16000; x += 1000) {
            require(biomes.contains(resolver.getNoiseBiome(x, 20, -x)), "Hex resolver returns a declared biome");
        }
        com.mojang.logging.LogUtils.getLogger().info("HEX REGION COUNTS grasslands={}, mountains={}, forests={}",
            counts[0], counts[1], counts[2]);
    }

    private static void verifyOcean(net.minecraft.server.level.ServerLevel level, long seed) {
        for (int q = -6; q <= 6; q++) for (int r = -6; r <= 6; r++) {
            var region = io.github.gabooz.picklecraft.world.HexRegions.cell(q, r, seed);
            if (region.kind() != io.github.gabooz.picklecraft.world.HexRegions.Kind.OCEAN) continue;
            double side = io.github.gabooz.picklecraft.world.HexRegions.SIDE;
            int x = (int) Math.round(side * Math.sqrt(3) * (q + r / 2.0));
            int z = (int) Math.round(side * 1.5 * r);
            if (io.github.gabooz.picklecraft.world.OceanTerrain.continent(x, z, seed) > -0.2) continue;
            level.getChunk(x >> 4, z >> 4);
            require(level.getBlockState(new BlockPos(x, 62, z)).is(Blocks.WATER),
                "Deep ocean columns contain water at sea level");
            require(level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR, x, z) < 58,
                "Deep ocean has a submerged floor");
            return;
        }
        throw new AssertionError("No deep ocean sample found");
    }

    private static void verifyZoneMines(net.minecraft.server.level.ServerLevel level) {
        var sampler = level.getChunkSource().randomState().createClimateSampler(
            net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED);
        long seed = io.github.gabooz.picklecraft.world.HexRegions.seed(sampler);
        int[] counts = new int[3];
        var examples = new java.util.EnumMap<io.github.gabooz.picklecraft.world.ZoneMines.Ore, BlockPos>(
            io.github.gabooz.picklecraft.world.ZoneMines.Ore.class);
        for (int q = -80; q <= 80; q++) {
            for (int r = -80; r <= 80; r++) {
                var zone = io.github.gabooz.picklecraft.world.HexRegions.cell(q, r, seed);
                var mine = io.github.gabooz.picklecraft.world.ZoneMines.ore(zone);
                counts[mine.ordinal()]++;
                if (Math.abs(q) <= 3 && Math.abs(r) <= 3) {
                    examples.putIfAbsent(mine, new BlockPos(
                        (int) Math.round(io.github.gabooz.picklecraft.world.HexRegions.SIDE * Math.sqrt(3) * (q + r / 2.0)), -24, (int) Math.round(io.github.gabooz.picklecraft.world.HexRegions.SIDE * 1.5 * r)));
                }
            }
        }
        double frequency = (counts[1] + counts[2]) / (double) (161 * 161);
        double ratio = counts[1] / (double) counts[2];
        require(frequency > 0.32 && frequency < 0.35 && ratio > 4.7 && ratio < 5.3,
            "One third of zones are mines, with five iron mines for each gold mine");
        require(examples.size() == 3, "Test world contains both mine types and ore-free zones");
        var deposit = level.registryAccess().lookupOrThrow(Registries.FEATURE).getOrThrow(
            ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath("picklecraft", "mine_ore_iron"))).value();
        for (var entry : examples.entrySet()) {
            var origin = entry.getValue();
            var mine = entry.getKey();
            var chunk = level.getChunk(origin.getX() >> 4, origin.getZ() >> 4);
            int ores = 0;
            for (int y = -60; y < 128; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        var pos = new BlockPos((origin.getX() & ~15) + x, y, (origin.getZ() & ~15) + z);
                        var state = chunk.getBlockState(pos);
                        String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
                        if (!name.endsWith("_ore") && !name.equals("raw_iron_block") && !name.equals("raw_copper_block")) continue;
                        ores++;
                        require(mine != io.github.gabooz.picklecraft.world.ZoneMines.Ore.NONE
                            && (mine == io.github.gabooz.picklecraft.world.ZoneMines.Ore.IRON
                                ? state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)
                                : state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE)),
                            "A generated zone contains only its selected ore");
                    }
                }
            }
            require((ores > 0) == (mine != io.github.gabooz.picklecraft.world.ZoneMines.Ore.NONE),
                "Mine chunks contain ore and ore-free chunks stay clear");
            // Exercise the reusable deposit directly in a controlled host block volume.
            for (var pos : BlockPos.betweenClosed(origin.offset(-8, -5, -8), origin.offset(8, 5, 8))) {
                level.setBlock(pos, Blocks.DEEPSLATE.defaultBlockState(), 2);
            }
            int placed = 0;
            for (int attempt = 0; attempt < 12; attempt++) {
                if (deposit.place(level, level.getChunkSource().getGenerator(),
                        net.minecraft.util.RandomSource.create(1234 + attempt), origin)) placed++;
            }
            require((placed > 0) == (mine != io.github.gabooz.picklecraft.world.ZoneMines.Ore.NONE),
                "Vanilla deposit wrapper respects zone eligibility");
            com.mojang.logging.LogUtils.getLogger().info("MINE SAMPLE type={}, generated_ore_blocks={}", mine, ores);
        }
        com.mojang.logging.LogUtils.getLogger().info("MINE ZONE COUNTS none={}, iron={}, gold={}",
            counts[0], counts[1], counts[2]);
    }

    private static void verifyZombieRosters(net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player) {
        var sampler = level.getChunkSource().randomState().createClimateSampler(
            net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED);
        long seed = io.github.gabooz.picklecraft.world.HexRegions.seed(sampler);
        int grasslands = 0;
        int assigned = 0;
        io.github.gabooz.picklecraft.world.HexRegions.Region example = null;
        for (int q = -70; q <= 70; q++) {
            for (int r = -70; r <= 70; r++) {
                var region = io.github.gabooz.picklecraft.world.HexRegions.cell(q, r, seed);
                if (region.kind() == io.github.gabooz.picklecraft.world.HexRegions.Kind.GRASSLANDS) grasslands++;
                if (io.github.gabooz.picklecraft.zombies.ZombieZones.assigned(region)) {
                    require(region.kind() == io.github.gabooz.picklecraft.world.HexRegions.Kind.GRASSLANDS,
                        "Zombie challenges are exclusive to grasslands");
                    assigned++;
                    if (example == null) example = region;
                }
            }
        }
        require(assigned / (float) grasslands > 0.14 && assigned / (float) grasslands < 0.16,
            "About fifteen percent of grasslands receive zombies");
        var ledger = io.github.gabooz.picklecraft.zombies.ZombieLedger.get(level);
        var zone = ledger.zone(example);
        require(zone.souls.size() >= 18 && zone.souls.size() <= 25, "Each roster has eighteen to twenty-five members");
        var actors = new java.util.ArrayList<io.github.gabooz.picklecraft.zombies.ZoneZombie>();
        for (int type = 0; type < 3; type++) {
            zone.souls.set(type, new io.github.gabooz.picklecraft.zombies.ZombieLedger.Soul(type, 1,
                io.github.gabooz.picklecraft.zombies.ZoneZombie.healthFor(type)));
            var actor = io.github.gabooz.picklecraft.zombies.ZombieContent.ZOMBIE.get().create(level,
                net.minecraft.world.entity.EntitySpawnReason.NATURAL);
            actor.assign(zone, type, 1);
            actor.setPos(player.getX() + 3 + type, player.getY() + 1, player.getZ());
            require(!actor.hasCustomName(), "Zone zombies have no name tags");
            require(actor.getMaxHealth() == (type == 0 ? 90 : type == 1 ? 60 : 70),
                "Roster variant has the specified maximum health");
            require(actor.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                == (type == 0 ? 8 : type == 1 ? 12 : 14), "Roster variant has the specified attack strength");
            require(io.github.gabooz.picklecraft.zombies.ZombieZones.allows(actor), "A current roster incarnation can join");
            require(level.addFreshEntity(actor), "Managed zombies pass mob suppression");
            actors.add(actor);
        }
        var first = actors.getFirst();
        var arena = first.blockPosition();
        for (var floor : BlockPos.betweenClosed(arena.offset(-17, -1, -17), arena.offset(17, -1, 17))) {
            level.setBlock(floor, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
            level.setBlock(floor.above(), Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(floor.above(2), Blocks.AIR.defaultBlockState(), 2);
        }
        var lamp = first.blockPosition().above(3);
        level.setBlockAndUpdate(lamp, Blocks.GLOWSTONE.defaultBlockState());
        // Process the light queue before checking the zombie's movement preference.
        var lightEngine = level.getChunkSource().getLightEngine();
        var pendingLight = lightEngine.waitForPendingTasks(lamp.getX() >> 4, lamp.getZ() >> 4);
        lightEngine.tryScheduleUpdate();
        level.getServer().managedBlock(pendingLight::isDone);
        require(first.getWalkTargetValue(first.blockPosition(), level) < 0,
            "Zombie navigation treats light as forbidden");
        require(io.github.gabooz.picklecraft.zombies.ZoneLights.pressure(level, arena.offset(8, 0, 0), java.util.List.of()) == 0,
            "Zombies may approach the dim edge of ordinary light");
        var firePos = arena.offset(6, 0, 0);
        level.setBlockAndUpdate(firePos, Blocks.CAMPFIRE.defaultBlockState());
        var fires = io.github.gabooz.picklecraft.zombies.ZoneLights.campfires(level, arena, 18);
        require(io.github.gabooz.picklecraft.zombies.ZoneLights.pressure(level, arena, fires)
                > io.github.gabooz.picklecraft.zombies.ZoneLights.pressure(level, arena, java.util.List.of()),
            "Campfires repel zombies more strongly than ordinary light at this distance");
        level.setBlockAndUpdate(firePos, Blocks.AIR.defaultBlockState());
        first.setOnGround(true);
        first.tickCount = 20;
        first.tick();
        require(!first.getNavigation().isDone(), "A zombie chooses an escape path from a light source");
        first.tickCount = 21;
        first.tick();
        require(!first.getNavigation().isDone(), "A zombie keeps its escape path on subsequent ticks");
        level.setBlockAndUpdate(lamp, Blocks.AIR.defaultBlockState());
        first.setHealth(5);
        var worldData = (net.minecraft.world.level.storage.ServerLevelData) level.getLevelData();
        long startTime = level.getGameTime();
        var clock = level.dimensionType().defaultClock().orElseThrow();
        long clockTime = level.getDefaultClockTime();
        level.clockManager().setTotalTicks(clock, 23000);
        first.tick();
        require(first.retreating() && first.getHealth() == 90, "Dawn triggers retreat and restores health");
        int before = zone.remaining();
        first.hurtServer(level, first.damageSources().generic(), 200);
        require(zone.remaining() == before - 1 && !zone.alive(0), "A killed roster member stays permanently dead");
        var ops = net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE, level.registryAccess());
        var saved = io.github.gabooz.picklecraft.zombies.ZombieLedger.CODEC.encodeStart(ops, ledger).getOrThrow();
        var restored = io.github.gabooz.picklecraft.zombies.ZombieLedger.CODEC.parse(ops, saved).getOrThrow();
        require(!restored.find(zone.q, zone.r).alive(0), "Saving and loading preserves permanent deaths");
        ledger.dawn(zone, 2);
        require(!zone.alive(0) && zone.souls.get(1).health() == 60, "Night reset heals survivors without resurrection");
        var second = actors.get(1);
        second.discard();
        require(!io.github.gabooz.picklecraft.zombies.ZombieZones.allows(second),
            "A detached incarnation cannot rejoin after unload");
        actors.get(2).discard();
        for (int slot = 0; slot < zone.souls.size(); slot++) ledger.kill(zone, slot);
        require(zone.remaining() == 0, "All roster members can be permanently cleared");
        var cleared = io.github.gabooz.picklecraft.zombies.ZombieLedger.CODEC.encodeStart(ops, ledger).getOrThrow();
        require(io.github.gabooz.picklecraft.zombies.ZombieLedger.CODEC.parse(ops, cleared).getOrThrow()
            .find(zone.q, zone.r).remaining() == 0, "A cleared zone remains cleared after save and load");
        level.clockManager().setTotalTicks(clock, clockTime);
        worldData.setGameTime(startTime);
        com.mojang.logging.LogUtils.getLogger().info("ZOMBIE ZONES grasslands={}, challenged={}, roster={}",
            grasslands, assigned, zone.souls.size());
    }

    private static void verifySeasonsAndFarming(net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player) {
        var clock = level.dimensionType().defaultClock().orElseThrow();
        long original = level.getDefaultClockTime();
        var calendar = io.github.gabooz.picklecraft.seasons.Seasons.get(level);
        require(io.github.gabooz.picklecraft.seasons.Seasons.at(0, 0).season() == io.github.gabooz.picklecraft.seasons.Seasons.Season.SPRING,
            "The calendar starts in spring");
        require(io.github.gabooz.picklecraft.seasons.Seasons.at(10 * 24000L, 0).season() == io.github.gabooz.picklecraft.seasons.Seasons.Season.SUMMER,
            "Each season lasts ten days");
        require(!io.github.gabooz.picklecraft.map.ZoneMapNetwork.changeTime(player, 6000)
            && !io.github.gabooz.picklecraft.map.ZoneMapNetwork.changeSeason(player, 3), "Calendar controls reject survival packets");
        level.clockManager().setTotalTicks(clock, 6000);
        calendar.select(level, io.github.gabooz.picklecraft.seasons.Seasons.Season.SPRING);
        var pos = player.blockPosition().offset(0, 3, 4);
        level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WHEAT_SEEDS, 2));
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos.below()), net.minecraft.core.Direction.UP, pos.below(), false);
        require(io.github.gabooz.picklecraft.ground.GroundInteractions.use(new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(
            player, net.minecraft.world.InteractionHand.MAIN_HAND, pos.below(), hit)), "Vanilla seeds plant the timed wheat crop");
        require(player.getMainHandItem().getCount() == 1, "Planting consumes one seed");
        var crop = (io.github.gabooz.picklecraft.farming.WheatGrowth) level.getBlockEntity(pos);
        crop.update(level);
        level.clockManager().setTotalTicks(clock, 6000 + 14 * 24000L);
        crop.update(level);
        require(level.getBlockState(pos).getValue(net.minecraft.world.level.block.CropBlock.AGE) < 7, "Wheat is immature before fifteen days");
        level.clockManager().setTotalTicks(clock, 6000 + 15 * 24000L);
        crop.update(level);
        require(level.getBlockState(pos).getValue(net.minecraft.world.level.block.CropBlock.AGE) == 7, "Wheat matures after fifteen days in summer");
        var drops = Block.getDrops(level.getBlockState(pos), level, pos, crop);
        require(drops.stream().anyMatch(item -> item.is(net.minecraft.world.item.Items.WHEAT) && item.getCount() >= 17 && item.getCount() <= 25)
            && drops.stream().anyMatch(item -> item.is(net.minecraft.world.item.Items.WHEAT_SEEDS) && item.getCount() >= 3 && item.getCount() <= 5), "Mature wheat yields requested grain and seeds");
        calendar.select(level, io.github.gabooz.picklecraft.seasons.Seasons.Season.AUTUMN);
        crop.update(level);
        require(!level.getBlockState(pos).getValue(io.github.gabooz.picklecraft.farming.SeasonalWheatBlock.DEAD), "Early autumn does not kill wheat");
        level.clockManager().setTotalTicks(clock, 6000 + 20 * 24000L);
        crop.update(level);
        require(level.getBlockState(pos).getValue(io.github.gabooz.picklecraft.farming.SeasonalWheatBlock.DEAD), "Autumn day six kills wheat");
        require(Block.getDrops(level.getBlockState(pos), level, pos, crop).isEmpty(), "Dead wheat yields no harvest");
        level.removeBlock(pos, false);
        var woodenHoe = net.minecraft.world.item.Items.WOODEN_HOE.getDefaultInstance();
        woodenHoe.setDamageValue(30);
        var stoneHoe = io.github.gabooz.picklecraft.ground.ToolConversions.convert(woodenHoe, net.minecraft.world.item.Items.COBBLESTONE.getDefaultInstance());
        require(stoneHoe.is(net.minecraft.world.item.Items.STONE_HOE) && stoneHoe.getDamageValue() > 60, "Hoe conversion preserves type and proportional wear");
        var ironHoe = io.github.gabooz.picklecraft.ground.ToolConversions.convert(stoneHoe, net.minecraft.world.item.Items.IRON_INGOT.getDefaultInstance());
        require(ironHoe.is(net.minecraft.world.item.Items.IRON_HOE), "An iron ingot upgrades a hoe to iron");
        player.setGameMode(GameType.CREATIVE);
        require(io.github.gabooz.picklecraft.map.ZoneMapNetwork.changeSeason(player, 3), "Creative map can select winter");
        require(io.github.gabooz.picklecraft.seasons.Seasons.date(level).season() == io.github.gabooz.picklecraft.seasons.Seasons.Season.WINTER, "Selected season reaches the server calendar");
        long day = Math.floorDiv(level.getDefaultClockTime(), 24000);
        require(io.github.gabooz.picklecraft.map.ZoneMapNetwork.changeTime(player, 18000)
            && Math.floorMod(level.getDefaultClockTime(), 24000) == 18000
            && Math.floorDiv(level.getDefaultClockTime(), 24000) == day, "Time control preserves the day number");
        player.setGameMode(GameType.SURVIVAL);
        level.clockManager().setTotalTicks(clock, original);
        calendar.select(level, io.github.gabooz.picklecraft.seasons.Seasons.Season.SPRING);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
    }

    private static void verifyBones(net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player) {
        var chunk = player.chunkPosition();
        boolean placed = false;
        for (long seed = 0; seed < 2000 && !placed; seed++) {
            placed = io.github.gabooz.picklecraft.zombies.ZoneBones.decorate(level, seed * 0x9E3779B97F4A7C15L, chunk);
        }
        require(placed, "Zombie-zone surface decoration places a real bone stack");
        boolean visibleBone = false;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            var top = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                chunk.getBlockAt(x, 0, z));
            for (var pos : java.util.List.of(top, top.below())) {
                if (level.getBlockEntity(pos) instanceof io.github.gabooz.picklecraft.ground.GroundStack stack
                        && stack.items().stream().anyMatch(item -> item.is(net.minecraft.world.item.Items.BONE))) visibleBone = true;
            }
        }
        require(visibleBone, "Generated bones retain item data on the visible surface");
    }

    private static void verifyCattle(net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player) {
        long seed = io.github.gabooz.picklecraft.zombies.ZombieZones.seed(level);
        int grass = 0;
        int herds = 0;
        for (int q = -80; q <= 80; q++) {
            for (int r = -80; r <= 80; r++) {
                var zone = io.github.gabooz.picklecraft.world.HexRegions.cell(q, r, seed);
                if (zone.kind() == io.github.gabooz.picklecraft.world.HexRegions.Kind.GRASSLANDS) grass++;
                if (io.github.gabooz.picklecraft.cattle.CattleHerds.assigned(zone)) {
                    herds++;
                    require(zone.kind() == io.github.gabooz.picklecraft.world.HexRegions.Kind.GRASSLANDS,
                        "Cattle are assigned only to grasslands");
                    require(io.github.gabooz.picklecraft.cattle.CattleHerds.size(zone) >= 2
                        && io.github.gabooz.picklecraft.cattle.CattleHerds.size(zone) <= 3, "Founding herds contain two or three animals");
                }
            }
        }
        require(herds / (float) grass > 0.04 && herds / (float) grass < 0.06, "Five percent of grasslands receive cattle");
        var mother = io.github.gabooz.picklecraft.cattle.CattleContent.COW.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        var father = io.github.gabooz.picklecraft.cattle.CattleContent.BULL.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        mother.setPos(player.getX() + 6, player.getY() + 1, player.getZ());
        father.setPos(player.getX() + 8, player.getY() + 1, player.getZ());
        require(level.addFreshEntity(mother) && level.addFreshEntity(father), "Cattle pass peaceful-mob suppression");
        require(father.isBull() && father.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SCALE) == 1.1,
            "Bull has its larger scale");
        require(father.getVariant().is(net.minecraft.world.entity.animal.cow.CowVariants.WARM), "Bull uses the tan warm-cow appearance");
        for (var stat : io.github.gabooz.picklecraft.cattle.CattleStats.Stat.values()) {
            require(mother.stats().get(stat) == 25 && father.stats().get(stat) == 25, "Founding cattle stats start at twenty-five");
        }
        mother.setInLove(player);
        father.setInLove(player);
        require(mother.canMate(father) && !mother.canMate(mother), "Only opposite cattle sexes can breed");
        mother.spawnChildFromBreeding(level, father);
        var babies = level.getEntitiesOfClass(io.github.gabooz.picklecraft.cattle.HerdCow.class,
            mother.getBoundingBox().inflate(8), cow -> cow.isBaby() && mother.getUUID().equals(cow.mother()));
        require(babies.size() == 1, "Breeding creates exactly one mother-linked calf");
        var calf = babies.getFirst();
        require(!mother.canFallInLove(), "A mother cannot breed while caring for a calf");
        var clock = level.dimensionType().defaultClock().orElseThrow();
        long original = level.getDefaultClockTime();
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        player.setItemInHand(hand, net.minecraft.world.item.Items.BUCKET.getDefaultInstance());
        require(mother.mobInteract(player, hand).consumesAction()
            && player.getItemInHand(hand).is(net.minecraft.world.item.Items.MILK_BUCKET), "An adult cow can be milked");
        player.setItemInHand(hand, net.minecraft.world.item.Items.BUCKET.getDefaultInstance());
        require(!mother.mobInteract(player, hand).consumesAction(), "A cow cannot be milked twice in one day");
        require(!father.mobInteract(player, hand).consumesAction(), "Bulls cannot be milked");

        var twin = io.github.gabooz.picklecraft.cattle.CattleContent.COW.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        twin.assignMother(mother, original);
        twin.setPos(mother.getX() + 2, mother.getY(), mother.getZ());
        level.addFreshEntity(twin);
        float twinHealth = twin.getHealth();
        level.clockManager().setTotalTicks(clock, original + 24001);
        twin.tickCount = 20;
        twin.tick();
        require(twin.getHealth() < twinHealth, "The second calf is not nurtured and starves after a day");
        calf.tickCount = 20;
        calf.tick();
        float calfHealth = calf.getHealth();
        calf.setPos(mother.getX() + 40, mother.getY(), mother.getZ());
        level.clockManager().setTotalTicks(clock, original + 48002);
        calf.tickCount = 20;
        calf.tick();
        require(calf.getHealth() < calfHealth, "A separated calf starts taking damage after a day");
        player.setItemInHand(hand, net.minecraft.world.item.Items.BUCKET.getDefaultInstance());
        require(mother.mobInteract(player, hand).consumesAction(), "Cow milk becomes available on a later day");
        level.clockManager().setTotalTicks(clock, original + 15 * 24000L);
        calf.tickCount = 20;
        calf.tick();
        require(!calf.isBaby() && mother.canFallInLove(), "Calves mature in fifteen clock days and release the mother");

        var random = net.minecraft.util.RandomSource.create(123456);
        int mutations = 0;
        int trials = 10000;
        for (int attempt = 0; attempt < trials; attempt++) {
            var childStats = io.github.gabooz.picklecraft.cattle.CattleStats.inherit(mother.stats(), father.stats(), random);
            for (var stat : io.github.gabooz.picklecraft.cattle.CattleStats.Stat.values()) {
                int value = childStats.get(stat);
                require(value >= 24 && value <= 26, "A newborn's mutation changes a stat by at most one");
                if (value != 25) mutations++;
            }
        }
        require(mutations > trials * 7 * 0.04 && mutations < trials * 7 * 0.06, "Each stat mutates about five percent of the time");
        var ops = net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE, level.registryAccess());
        var data = io.github.gabooz.picklecraft.cattle.CattleData.get(level);
        data.markSpawned(123456, 0);
        var encoded = io.github.gabooz.picklecraft.cattle.CattleData.CODEC.encodeStart(ops, data).getOrThrow();
        var restored = io.github.gabooz.picklecraft.cattle.CattleData.CODEC.parse(ops, encoded).getOrThrow();
        require(restored.spawned(123456) == 1, "Completed herd spawns survive saving");
        var dropArea = father.getBoundingBox().inflate(3);
        father.hurtServer(level, father.damageSources().generic(), 200);
        int beef = 0;
        int leather = 0;
        for (var item : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, dropArea)) {
            if (item.getItem().is(net.minecraft.world.item.Items.BEEF)) beef += item.getItem().getCount();
            if (item.getItem().is(net.minecraft.world.item.Items.LEATHER)) leather += item.getItem().getCount();
        }
        require(beef >= 25 && beef <= 35 && leather >= 7 && leather <= 12, "Adult cattle have the requested raw-beef and leather yields");
        calf.discard();
        twin.discard();
        mother.discard();
        father.discard();
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        level.clockManager().setTotalTicks(clock, original);
        com.mojang.logging.LogUtils.getLogger().info("CATTLE CHECK grasslands={}, herds={}, mutations={}", grass, herds, mutations);
    }

    private static void verifyGroundCraft(net.minecraft.server.level.ServerLevel level, net.minecraft.server.level.ServerPlayer player) {
        var pos = player.blockPosition().offset(2, 1, 0);
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        var content = io.github.gabooz.picklecraft.ground.GroundContent.STACK.get();
        var items = net.minecraft.world.item.Items.STICK;
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
            net.minecraft.core.Direction.UP, pos, false);
        var placeHit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos.below()),
            net.minecraft.core.Direction.UP, pos.below(), false);
        var stick = new net.minecraft.world.item.ItemStack(items);
        player.setItemInHand(hand, stick);
        require(io.github.gabooz.picklecraft.ground.GroundInteractions.use(
            new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, pos.below(), placeHit))
            && level.getBlockState(pos).is(content) && stick.isEmpty(), "Place a stick on the ground");
        for (var item : java.util.List.of(net.minecraft.world.item.Items.STRING, io.github.gabooz.picklecraft.ground.GroundContent.STONE.get())) {
            player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(item));
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(
                new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, pos, hit));
        }
        var stack = (io.github.gabooz.picklecraft.ground.GroundStack) level.getBlockEntity(pos);
        require(stack.items().size() == 3, "Stack three distinct materials");
        var click = new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, pos, hit);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(stack.items().getFirst().is(net.minecraft.world.item.Items.STONE_PICKAXE), "Assemble a pickaxe");
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(stack.items().getFirst().is(net.minecraft.world.item.Items.STONE_SWORD), "Cycle to a sword");
        for (var tool : java.util.List.of(net.minecraft.world.item.Items.STONE_SHOVEL,
                net.minecraft.world.item.Items.STONE_AXE, io.github.gabooz.picklecraft.ground.GroundContent.STONE_HAMMER.get())) {
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
            require(stack.items().getFirst().is(tool), "Cycle ground recipe to " + tool);
        }
        player.setShiftKeyDown(true);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setShiftKeyDown(false);
        require(level.isEmptyBlock(pos), "Pick up the assembled stone hammer");

        for (int layer = 0; layer < 17; layer++) {
            if (layer == 0) {
                require(io.github.gabooz.picklecraft.ground.GroundStackBlock.place(level, pos,
                    new net.minecraft.world.item.ItemStack(items)), "Start a stack for the layer limit test");
            } else {
                player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(items));
                io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
            }
        }
        var full = (io.github.gabooz.picklecraft.ground.GroundStack) level.getBlockEntity(pos);
        require(full.items().size() == 16 && !player.getItemInHand(hand).isEmpty(), "Reject the seventeenth layer");
        player.setShiftKeyDown(true);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setShiftKeyDown(false);
        require(full.items().size() == 15, "Shift-click removes only the top layer");
        var attack = new net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock(player, pos,
            net.minecraft.core.Direction.UP, net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START);
        require(io.github.gabooz.picklecraft.ground.GroundInteractions.hit(attack) && level.isEmptyBlock(pos),
            "A hit drops the ground stack");

        var flower = io.github.gabooz.picklecraft.ground.GroundContent.PLUMARIA.get();
        level.setBlockAndUpdate(pos, flower.defaultBlockState());
        flower.defaultBlockState().useWithoutItem(level, player, hit);
        var blooming = io.github.gabooz.picklecraft.ground.PlumariaBlock.BLOOMING;
        require(!level.getBlockState(pos).getValue(blooming), "Harvest flowers and leave foliage");
        var clock = level.dimensionType().defaultClock().orElseThrow();
        var time = level.getDefaultClockTime();
        level.clockManager().setTotalTicks(clock, time + io.github.gabooz.picklecraft.ground.PlumariaBlock.WEEK - 1);
        level.getBlockState(pos).tick(level, pos, level.getRandom());
        require(!level.getBlockState(pos).getValue(blooming), "Plumaria stays bare for a full week");
        level.clockManager().setTotalTicks(clock, time + io.github.gabooz.picklecraft.ground.PlumariaBlock.WEEK);
        level.getBlockState(pos).tick(level, pos, level.getRandom());
        require(level.getBlockState(pos).getValue(blooming), "Plumaria blooms after one week");
    }
    private static void verifyWorkbench(net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player) {
        var pos = player.blockPosition().offset(2, 2, 0);
        level.setBlockAndUpdate(pos, Blocks.OAK_LOG.defaultBlockState());
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
            net.minecraft.core.Direction.EAST, pos, false);
        var click = new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, pos, hit);
        var axe = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE_AXE);
        player.setItemInHand(hand, axe);
        player.setShiftKeyDown(true);
        require(io.github.gabooz.picklecraft.ground.GroundInteractions.use(click), "Carve the workbench");
        player.setShiftKeyDown(false);
        require(level.getBlockState(pos).is(io.github.gabooz.picklecraft.ground.GroundContent.WORKBENCH.get()), "Workbench replaced log");
        var bench = (io.github.gabooz.picklecraft.ground.GroundStack) level.getBlockEntity(pos);
        var items = net.minecraft.world.item.Items.OAK_LOG;
        level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
        player.setItemInHand(hand, items.getDefaultInstance());
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(bench.items().isEmpty() && player.getItemInHand(hand).is(items), "Covered workbench cannot be used");
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(items, 2));
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(bench.items().size() == 1 && player.getItemInHand(hand).getCount() == 1,
            "Workbench accepts only one input");
        player.setItemInHand(hand, axe);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(bench.items().getLast().is(net.minecraft.world.item.Items.OAK_PLANKS), "Axe processes the last log");
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(bench.items().size() == 4 && bench.items().getLast().is(net.minecraft.world.item.Items.STICK),
            "Workbench stacks all four output items");
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(items));
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(bench.items().size() == 4 && player.getItemInHand(hand).is(items), "Output blocks additional inputs");
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(bench.items().size() == 3, "Empty hand removes last item");
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setShiftKeyDown(false);
        require(bench.items().isEmpty(), "Shift-click clears workbench");
        var blossoms = io.github.gabooz.picklecraft.ground.GroundContent.BLOSSOMS.get();
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(blossoms));
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SHEARS));
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(bench.items().size() == 2 && bench.items().stream().allMatch(item -> item.is(net.minecraft.world.item.Items.STRING)),
            "Shearing Plumaria blossoms produces two string");

        var string = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STRING, 63);
        player.setItemInHand(hand, string);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(string.getCount() == 64 && bench.items().size() == 1,
            "Workbench merges matching output up to the held stack limit");

        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setItemInHand(hand, net.minecraft.world.item.Items.COBBLESTONE.getDefaultInstance());
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        var hammer = io.github.gabooz.picklecraft.ground.GroundContent.STONE_HAMMER.get().getDefaultInstance();
        player.setItemInHand(hand, hammer);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(bench.items().size() >= 3 && bench.items().size() <= 4
            && bench.items().stream().allMatch(item -> item.is(io.github.gabooz.picklecraft.ground.GroundContent.STONE.get())),
            "Hammer crushes cobblestone into three or four loose stones");

        var ingot = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 2);
        player.setItemInHand(hand, ingot);
        player.setShiftKeyDown(true);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setShiftKeyDown(false);
        require(level.getBlockState(pos).is(io.github.gabooz.picklecraft.ground.GroundContent.ANVIL.get()),
            "Shift-clicking a workbench with an ingot makes an anvil");
        var anvil = (io.github.gabooz.picklecraft.ground.GroundStack) level.getBlockEntity(pos);
        require(anvil.items().isEmpty(), "Anvil starts clear after conversion");
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 5));
        for (int i = 0; i < 5; i++) {
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        }
        require(anvil.items().size() == 4, "Anvil holds at most four loose items");
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        for (int i = 0; i < 4; i++) {
            player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        }
        for (var ingredient : java.util.List.of(net.minecraft.world.item.Items.STICK, net.minecraft.world.item.Items.IRON_INGOT)) {
            player.setItemInHand(hand, ingredient.getDefaultInstance());
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        }
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(anvil.items().size() == 1 && anvil.items().getFirst().is(net.minecraft.world.item.Items.STICK),
            "Empty hand removes the top item from a ready anvil recipe");
        player.setItemInHand(hand, net.minecraft.world.item.Items.IRON_INGOT.getDefaultInstance());
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setItemInHand(hand, hammer);
        for (var tool : java.util.List.of(net.minecraft.world.item.Items.IRON_PICKAXE, net.minecraft.world.item.Items.IRON_SWORD,
                net.minecraft.world.item.Items.IRON_SHOVEL, net.minecraft.world.item.Items.IRON_AXE,
                io.github.gabooz.picklecraft.ground.GroundContent.IRON_HAMMER.get(),
                net.minecraft.world.item.Items.IRON_PICKAXE)) {
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
            require(anvil.items().size() == 1 && anvil.items().getFirst().is(tool), "Anvil shapes " + tool);
        }
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(anvil.items().isEmpty(), "Empty hand removes the finished iron tool");

        var groundPos = pos.offset(1, 0, 0);
        level.setBlockAndUpdate(groundPos.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(groundPos, Blocks.AIR.defaultBlockState());
        require(io.github.gabooz.picklecraft.ground.GroundStackBlock.place(level, groundPos,
            net.minecraft.world.item.Items.OAK_PLANKS.getDefaultInstance()), "Start the wooden hoe recipe with planks");
        var hoeHit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(groundPos),
            net.minecraft.core.Direction.UP, groundPos, false);
        var hoeClick = new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, groundPos, hoeHit);
        for (var ingredient : java.util.List.of(net.minecraft.world.item.Items.STRING, net.minecraft.world.item.Items.STICK)) {
            player.setItemInHand(hand, ingredient.getDefaultInstance());
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(hoeClick);
        }
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(hoeClick);
        var hoeStack = (io.github.gabooz.picklecraft.ground.GroundStack) level.getBlockEntity(groundPos);
        require(hoeStack.items().size() == 1 && hoeStack.items().getFirst().is(net.minecraft.world.item.Items.WOODEN_HOE),
            "Planks, string and stick form a wooden hoe instead of the stone cycle");
        level.removeBlock(groundPos, false);
        player.setItemInHand(hand, net.minecraft.world.item.Items.WOODEN_HOE.getDefaultInstance());
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 2));
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(anvil.items().getFirst().is(net.minecraft.world.item.Items.STONE_HOE) && player.getMainHandItem().getCount() == 1,
            "Anvil consumes one cobble to convert a placed hoe to stone");
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 2));
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(anvil.items().getFirst().is(net.minecraft.world.item.Items.IRON_HOE) && player.getMainHandItem().getCount() == 1,
            "Anvil consumes one ingot to convert a placed hoe to iron");
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        var ground = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(groundPos.below()),
            net.minecraft.core.Direction.UP, groundPos.below(), false);
        player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(blossoms));
        require(io.github.gabooz.picklecraft.ground.GroundInteractions.use(
            new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, groundPos.below(), ground)),
            "Place blossoms on grass");
        var stack = (io.github.gabooz.picklecraft.ground.GroundStack) level.getBlockEntity(groundPos);
        require(stack != null && stack.items().getFirst().is(blossoms), "Ground stack contains blossoms");
        player.setShiftKeyDown(true);
        var blossomHit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(groundPos),
            net.minecraft.core.Direction.UP, groundPos, false);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(
            new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, groundPos, blossomHit));
        player.setShiftKeyDown(false);
        require(stack.items().size() == 1 && stack.items().getFirst().is(net.minecraft.world.item.Items.STRING),
            "Shift-click converts ground blossoms into one string");

    }
    private static void verifyIronFurnace(net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player) {
        var itemPos = player.blockPosition().offset(-3, 3, 0);
        var support = itemPos.below();
        level.setBlockAndUpdate(support, Blocks.COBBLESTONE.defaultBlockState());
        level.setBlockAndUpdate(itemPos, Blocks.AIR.defaultBlockState());
        var furnaceItem = io.github.gabooz.picklecraft.ground.GroundContent.IRON_FURNACE_ITEM.get();
        var itemStack = furnaceItem.getDefaultInstance();
        var itemHit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(support),
            net.minecraft.core.Direction.UP, support, false);
        var placement = new net.minecraft.world.item.context.BlockPlaceContext(
            player, net.minecraft.world.InteractionHand.MAIN_HAND, itemStack, itemHit);
        level.setBlockAndUpdate(itemPos.above(), Blocks.COBBLESTONE.defaultBlockState());
        require(!furnaceItem.place(placement).consumesAction() && itemStack.getCount() == 1
            && level.isEmptyBlock(itemPos), "Creative tab furnace item requires room for both halves");
        level.setBlockAndUpdate(itemPos.above(), Blocks.AIR.defaultBlockState());
        require(furnaceItem.place(placement).consumesAction() && level.getBlockEntity(itemPos)
            instanceof io.github.gabooz.picklecraft.ground.IronFurnace
            && level.getBlockState(itemPos.above()).getValue(io.github.gabooz.picklecraft.ground.IronFurnaceBlock.TOP),
            "Creative tab furnace item places both halves");
        var pos = player.blockPosition().offset(4, 3, 0);
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
            net.minecraft.core.Direction.NORTH, pos, false);
        var click = new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, pos, hit);
        level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.COBBLESTONE.defaultBlockState());
        level.setBlockAndUpdate(pos.above(2), Blocks.AIR.defaultBlockState());
        player.setItemInHand(hand, net.minecraft.world.item.Items.STONE_PICKAXE.getDefaultInstance());
        player.setShiftKeyDown(true);
        require(io.github.gabooz.picklecraft.ground.GroundInteractions.use(click), "Carve iron furnace");
        player.setShiftKeyDown(false);
        var state = level.getBlockState(pos);
        var furnace = (io.github.gabooz.picklecraft.ground.IronFurnace) level.getBlockEntity(pos);
        require(furnace != null && level.getBlockState(pos.above()).getValue(
            io.github.gabooz.picklecraft.ground.IronFurnaceBlock.TOP), "Furnace has both halves");
        var coal = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, 2);
        player.setItemInHand(hand, coal);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(coal.getCount() == 1 && furnace.fuel().getCount() == 1, "Accept only one coal");
        var raw = new net.minecraft.world.entity.item.ItemEntity(level, pos.getX() + .5, pos.getY() + 1.3,
            pos.getZ() + .5, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.RAW_IRON, 2));
        level.addFreshEntity(raw);
        var ticker = state.getTicker(level, io.github.gabooz.picklecraft.ground.GroundContent.FURNACE_ENTITY.get());
        ticker.tick(level, pos, state, furnace);
        require(raw.getItem().getCount() == 1 && furnace.iron().getCount() == 1
            && level.getBlockState(pos).getValue(io.github.gabooz.picklecraft.ground.IronFurnaceBlock.LIT),
            "Smelt one dropped iron with one fuel");
        var worldData = (net.minecraft.world.level.storage.ServerLevelData) level.getLevelData();
        long start = level.getGameTime();
        worldData.setGameTime(start + 1100);
        ticker.tick(level, pos, state, furnace);
        require(furnace.iron().is(net.minecraft.world.item.Items.IRON_INGOT), "Ingot appears after 55 seconds");
        worldData.setGameTime(start + 1200);
        ticker.tick(level, pos, state, furnace);
        require(furnace.fuel().isEmpty() && !level.getBlockState(pos).getValue(
            io.github.gabooz.picklecraft.ground.IronFurnaceBlock.LIT), "Fuel consumed and fire out at 60 seconds");
        worldData.setGameTime(start + 7200);
        var tongs = io.github.gabooz.picklecraft.ground.GroundContent.IRON_TONGS.get().getDefaultInstance();
        player.setItemInHand(hand, tongs);
        float health = player.getHealth();
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(furnace.iron().isEmpty() && player.getHealth() == health && tongs.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0) == 1,
            "Retrieving cooled iron with tongs costs one durability");
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(tongs.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0) == 1, "An empty furnace does not wear down tongs");
        player.setItemInHand(hand, net.minecraft.world.item.Items.CHARCOAL.getDefaultInstance());
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        ticker.tick(level, pos, state, furnace);
        require(furnace.elapsed() == 0, "Charcoal starts the second batch");
        worldData.setGameTime(start + 8400);
        ticker.tick(level, pos, state, furnace);
        player.setItemInHand(hand, tongs);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(furnace.iron().isEmpty() && player.getHealth() == health - 1F && tongs.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0) == 2
            && tongs.get(net.minecraft.core.component.DataComponents.ITEM_MODEL).getPath().equals("iron_tongs_loaded"),
            "Tongs grip hot iron and deal half a heart");
        var waterPos = pos.offset(1, 0, 0);
        level.setBlockAndUpdate(waterPos, Blocks.WATER_CAULDRON.defaultBlockState());
        var waterHit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(waterPos),
            net.minecraft.core.Direction.UP, waterPos, false);
        require(tongs.useOn(new net.minecraft.world.item.context.UseOnContext(player, hand, waterHit)).consumesAction()
            && tongs.get(net.minecraft.core.component.DataComponents.ITEM_MODEL).getPath().equals("iron_tongs"),
            "Cauldron quenches iron and empties tongs");
        require(tongs.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0) == 3, "Quenching hot iron costs one durability");
        level.setBlockAndUpdate(waterPos, Blocks.AIR.defaultBlockState());


    }

    private static void verifyGoldFurnace(net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player) {
        var center = player.blockPosition().offset(-3, 4, 0);
        var front = net.minecraft.core.Direction.EAST;
        var grate = center.relative(front);
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        for (var pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            level.setBlockAndUpdate(pos, pos.equals(center) || pos.equals(center.above())
                ? Blocks.AIR.defaultBlockState() : Blocks.RED_SANDSTONE.defaultBlockState());
        }
        level.setBlockAndUpdate(grate, Blocks.IRON_BARS.defaultBlockState());
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(grate),
            front, grate, false);
        var click = new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, grate, hit);
        var hammer = io.github.gabooz.picklecraft.ground.GroundContent.STONE_HAMMER.get().getDefaultInstance();
        player.setItemInHand(hand, hammer);
        player.setShiftKeyDown(true);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setShiftKeyDown(false);
        var state = level.getBlockState(grate);
        var lit = net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;
        require(level.getBlockEntity(grate) instanceof io.github.gabooz.picklecraft.ground.GoldFurnace,
            "Hammer forms a gold furnace in a complete shell");
        var shell = io.github.gabooz.picklecraft.ground.GroundContent.GOLD_FURNACE_SHELL.get();
        int shapedBlocks = 0;
        for (var pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (level.getBlockState(pos).is(shell)) {
                shapedBlocks++;
            }
        }
        require(shapedBlocks == 24 && level.isEmptyBlock(center) && level.isEmptyBlock(center.above()),
            "All sandstone forms the modeled furnace and the shaft stays open");
        var furnace = (io.github.gabooz.picklecraft.ground.GoldFurnace) level.getBlockEntity(grate);
        var ticker = state.getTicker(level, io.github.gabooz.picklecraft.ground.GroundContent.GOLD_FURNACE_ENTITY.get());
        var items = net.minecraft.world.item.Items.RAW_GOLD;
        var ore = new net.minecraft.world.entity.item.ItemEntity(level, center.getX() + 0.5, center.getY() + 1.5,
            center.getZ() + 0.5, new net.minecraft.world.item.ItemStack(items, 2));
        level.addFreshEntity(ore);
        ticker.tick(level, grate, state, furnace);
        require(furnace.gold().is(items) && ore.getItem().getCount() == 1, "Gold chute accepts exactly one ore");
        for (int i = 0; i < 10; i++) {
            player.setItemInHand(hand, (i % 2 == 0 ? net.minecraft.world.item.Items.COAL
                : net.minecraft.world.item.Items.CHARCOAL).getDefaultInstance());
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
            if (i == 8) {
                player.setItemInHand(hand, net.minecraft.world.item.Items.FLINT_AND_STEEL.getDefaultInstance());
                io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
                require(!level.getBlockState(grate).getValue(lit), "Nine fuel cannot light the furnace");
            }
        }
        require(furnace.fuelCount() == 10 && !level.getBlockState(grate).getValue(lit),
            "Ten mixed fuel wait for manual ignition");
        player.setItemInHand(hand, net.minecraft.world.item.Items.FLINT_AND_STEEL.getDefaultInstance());
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(furnace.fuelCount() == 0 && level.getBlockState(grate).getValue(lit), "Manual ignition starts the batch");
        for (int i = 1; i < io.github.gabooz.picklecraft.ground.GoldFurnace.SMELT_TICKS; i++) {
            ticker.tick(level, grate, state, furnace);
        }
        require(furnace.gold().is(items), "Gold remains ore until the full thirty minutes");
        ticker.tick(level, grate, state, furnace);
        require(furnace.gold().is(net.minecraft.world.item.Items.GOLD_INGOT) && furnace.hot()
            && !level.getBlockState(grate).getValue(lit), "Gold finishes hot after thirty minutes");
        var tongs = io.github.gabooz.picklecraft.ground.GroundContent.IRON_TONGS.get().getDefaultInstance();
        player.setItemInHand(hand, tongs);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(furnace.gold().isEmpty() && tongs.get(net.minecraft.core.component.DataComponents.ITEM_MODEL)
            .getPath().equals("iron_tongs_loaded_gold"), "Tongs pick up hot gold");
    }

    private static void verifyOpenBarrel(net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player) {
        var pos = player.blockPosition().offset(0, 1, 2);
        var direction = net.minecraft.core.Direction.UP;
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
            direction, pos, false);
        var click = new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, pos, hit);
        var block = io.github.gabooz.picklecraft.ground.GroundContent.OPEN_BARREL.get();
        level.setBlockAndUpdate(pos, block.defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        var barrel = (io.github.gabooz.picklecraft.ground.OpenBarrel) level.getBlockEntity(pos);
        var stone = net.minecraft.world.item.Items.COBBLESTONE;
        var axe = net.minecraft.world.item.Items.IRON_AXE;
        var stack = new net.minecraft.world.item.ItemStack(stone, 60);
        player.setItemInHand(hand, stack);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        var tool = axe.getDefaultInstance();
        tool.set(net.minecraft.core.component.DataComponents.DAMAGE, 7);
        player.setItemInHand(hand, tool);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        var top = new net.minecraft.world.item.ItemStack(stone, 5);
        player.setItemInHand(hand, top);
        player.setShiftKeyDown(true);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(top.getCount() == 4 && barrel.contents().getLast().getCount() == 1,
            "Sneaking inserts one item into the barrel");
        player.setShiftKeyDown(false);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(top.getCount() == 2 && barrel.contents().getLast().getCount() == 3,
            "Barrel accepts only as many items as fit");
        player.setShiftKeyDown(true);
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(barrel.contents().getLast().getCount() == 2, "Sneaking withdraws one item");
        player.setShiftKeyDown(false);
        player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        require(barrel.contents().size() == 2 && barrel.contents().getFirst().getCount() == 60
            && barrel.contents().getLast().is(axe)
            && barrel.contents().getLast().getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0) == 7,
            "Barrel withdraws a contiguous run and preserves the tool underneath");
        player.setShiftKeyDown(true);
        var hitBarrel = new net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock(player, pos, direction,
            net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START);
        require(io.github.gabooz.picklecraft.ground.GroundInteractions.hit(hitBarrel)
            && barrel.contents().isEmpty() && level.getBlockState(pos).is(block),
            "Sneak hit empties the barrel in place");
        player.setShiftKeyDown(false);
        var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
            new net.minecraft.world.phys.AABB(pos.above()), entity -> entity.getItem().is(axe) || entity.getItem().is(stone));
        require(drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum() == 61,
            "All remaining items spill into the world");
    }

    private static void verifyStockpile(net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player) {
        var hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        var coal = net.minecraft.world.item.Items.COAL;
        var stone = net.minecraft.world.item.Items.STONE;
        for (int size : new int[]{3, 9}) {
            var corner = player.blockPosition().offset(1, 5, size == 3 ? 0 : 4);
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    var cell = corner.offset(x, 0, z);
                    boolean xEdge = x == 0 || x == size - 1;
                    boolean zEdge = z == 0 || z == size - 1;
                    if (xEdge || zEdge) {
                        level.setBlockAndUpdate(cell, (xEdge && zEdge ? Blocks.OAK_LOG : Blocks.STONE_BRICKS).defaultBlockState());
                    } else {
                        level.setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(cell.below(), Blocks.DIRT_PATH.defaultBlockState());
                    }
                }
            }
            var controller = corner.offset(1, 0, 0);
            var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(controller),
                net.minecraft.core.Direction.UP, controller, false);
            var click = new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, controller, hit);
            var hammer = io.github.gabooz.picklecraft.ground.GroundContent.STONE_HAMMER.get().getDefaultInstance();
            player.setItemInHand(hand, hammer);
            player.setShiftKeyDown(true);
            require(io.github.gabooz.picklecraft.ground.GroundInteractions.use(click), "Hammer forms a " + size + "x" + size + " stockpile");
            player.setShiftKeyDown(false);
            var pile = (io.github.gabooz.picklecraft.ground.Stockpile) level.getBlockEntity(controller);
            require(pile != null && pile.capacity() == size * size * size, "Stockpile capacity is width cubed");
            var trim = io.github.gabooz.picklecraft.ground.GroundContent.STOCKPILE_TRIM.get();
            require(level.getBlockState(corner).is(trim)
                && level.getBlockState(corner.offset(0, 0, 1)).is(trim)
                && level.getBlockState(corner.offset(1, -1, 1)).is(trim)
                && level.isEmptyBlock(corner.offset(1, 0, 1)),
                "Assembled stockpile has modeled corners, rim and recessed floor");
            var coalStack = new net.minecraft.world.item.ItemStack(coal, 64);
            player.setItemInHand(hand, coalStack);
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
            require(coalStack.isEmpty() && pile.sample().is(coal), "First deposit sets the stockpile type");
            player.setItemInHand(hand, stone.getDefaultInstance());
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
            require(player.getItemInHand(hand).is(stone) && pile.sample().is(coal), "Stockpile rejects a different item type");
            var otherRim = corner.offset(0, 0, 1);
            var otherHit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(otherRim),
                net.minecraft.core.Direction.UP, otherRim, false);
            player.setItemInHand(hand, net.minecraft.world.item.ItemStack.EMPTY);
            io.github.gabooz.picklecraft.ground.GroundInteractions.use(
                new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, hand, otherRim, otherHit));
            require(pile.sample().isEmpty(), "Another rim block withdraws the last stack");
        }
    }

    private static void require(boolean condition, String check) {
        if (!condition) {
            throw new AssertionError(check);
        }
    }
}
