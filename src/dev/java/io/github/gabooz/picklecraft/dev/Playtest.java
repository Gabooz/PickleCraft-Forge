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
                    com.mojang.logging.LogUtils.getLogger().info("PICKLECRAFT PLAYTEST PASSED");
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
        verifyGroundCraft(level, player);
        require(level.dimensionType().defaultClock().map(clock -> level.clockManager().getInstance(clock).rate() == 0.25F).orElse(false),
            "Overworld clock runs at quarter speed");
        require(!biome.shouldSnow(level, new BlockPos(pos.getX(), 250, pos.getZ())), "No snow at high altitude");
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
        player.setShiftKeyDown(true);
        io.github.gabooz.picklecraft.ground.GroundInteractions.use(click);
        player.setShiftKeyDown(false);
        require(level.isEmptyBlock(pos), "Pick up the assembled tool");

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
    private static void require(boolean condition, String check) {
        if (!condition) {
            throw new AssertionError(check);
        }
    }
}
