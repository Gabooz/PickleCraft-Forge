package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class GoldFurnace extends BlockEntity {
    public static final int FUEL_REQUIRED = 10;
    public static final int SMELT_TICKS = 30 * 60 * 20;
    private static final ResourceKey<DamageType> HOT_GOLD = ResourceKey.create(
        Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("picklecraft", "superheated_gold"));

    private ItemStack gold = ItemStack.EMPTY;
    private int coal;
    private int charcoal;
    private int progress = -1;
    private long cooledAt = -1;

    GoldFurnace(BlockPos pos, BlockState state) {
        super(GroundContent.GOLD_FURNACE_ENTITY.get(), pos, state);
    }

    public ItemStack gold() {
        return gold;
    }

    public int fuelCount() {
        return coal + charcoal;
    }

    public float heat() {
        return progress < 0 ? 0 : progress / (float) SMELT_TICKS;
    }

    public boolean hot() {
        return cooledAt >= 0 && level != null && level.getGameTime() < cooledAt;
    }

    private BlockPos center() {
        return worldPosition.relative(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
    }

    private boolean assembled() {
        return GoldFurnaceBlock.hasShell(level, worldPosition,
            getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    void tick(ServerLevel server) {
        // Upgrade old furnaces and shape replacement sandstone after a repair.
        if (server.getGameTime() % 20 == 0 && assembled()) {
            GoldFurnaceBlock.shapeShell(server, worldPosition,
                getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
        if (progress >= 0) {
            // smelting advances only while the shell is intact and loaded
            boolean running = assembled();
            setLit(running);
            if (running && ++progress >= SMELT_TICKS) {
                progress = -1;
                gold = Items.GOLD_INGOT.getDefaultInstance();
                cooledAt = server.getGameTime() + IronFurnace.COOL_TICKS;
                setLit(false);
                sync();
            } else if (running) {
                setChanged();
                if (progress % 200 == 0) {
                    sync();
                }
            }
        } else if (gold.isEmpty() && assembled()) {
            var center = center();
            var chute = new AABB(center).expandTowards(0, 1, 0);
            for (var entity : server.getEntitiesOfClass(ItemEntity.class, chute, item ->
                    item.getItem().is(Items.RAW_GOLD) || item.getItem().is(Items.GOLD_ORE)
                    || item.getItem().is(Items.DEEPSLATE_GOLD_ORE))) {
                var remaining = entity.getItem().copy();
                gold = remaining.split(1);
                if (remaining.isEmpty()) {
                    entity.discard();
                } else {
                    entity.setItem(remaining);
                }
                sync();
                break;
            }
        }
    }

    boolean addFuel(Item item) {
        if (!assembled() || progress >= 0 || gold.is(Items.GOLD_INGOT) || fuelCount() >= FUEL_REQUIRED) {
            return false;
        }
        if (item == Items.COAL) {
            coal++;
        } else {
            charcoal++;
        }
        sync();
        return true;
    }

    boolean ignite() {
        if (!assembled() || progress >= 0 || gold.isEmpty() || gold.is(Items.GOLD_INGOT)
                || fuelCount() != FUEL_REQUIRED) {
            return false;
        }
        coal = 0;
        charcoal = 0;
        progress = 0;
        setLit(true);
        sync();
        return true;
    }

    void retrieve(ServerLevel server, Player player, ItemStack held, InteractionHand hand) {
        boolean tongs = held.is(GroundContent.IRON_TONGS.get());
        if (tongs && IronTongs.loaded(held)) {
            return;
        }
        if (!tongs && player.isShiftKeyDown() && progress < 0 && fuelCount() > 0) {
            var item = charcoal > 0 ? Items.CHARCOAL : Items.COAL;
            if (charcoal > 0) {
                charcoal--;
            } else {
                coal--;
            }
            give(player, item.getDefaultInstance());
            sync();
            return;
        }
        if (!gold.is(Items.GOLD_INGOT)) {
            return;
        }
        boolean hot = hot();
        var output = gold;
        gold = ItemStack.EMPTY;
        long deadline = cooledAt;
        cooledAt = -1;
        sync();
        if (tongs) {
            held.hurtAndBreak(1, player, hand);
        }
        if (hot && tongs && !held.isEmpty()) {
            IronTongs.grip(held, deadline, true);
        } else if (hot && tongs) {
            Block.popResource(server, worldPosition, output);
        } else {
            give(player, output);
        }
        if (hot) {
            var damage = server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(HOT_GOLD);
            player.hurtServer(server, new DamageSource(damage), tongs ? 1F : 22F);
        }
    }

    List<ItemStack> contents() {
        var items = new ArrayList<ItemStack>();
        if (!gold.isEmpty()) items.add(gold.copy());
        if (coal > 0) items.add(new ItemStack(Items.COAL, coal));
        if (charcoal > 0) items.add(new ItemStack(Items.CHARCOAL, charcoal));
        return items;
    }

    private void give(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            Block.popResource(level, worldPosition, stack);
        }
    }

    private void setLit(boolean lit) {
        if (getBlockState().getValue(BlockStateProperties.LIT) != lit) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.LIT, lit));
        }
    }

    private void sync() {
        setChanged();
        var state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!gold.isEmpty()) output.store("gold", ItemStack.CODEC, gold);
        output.putInt("coal", coal);
        output.putInt("charcoal", charcoal);
        output.putInt("progress", progress);
        output.putLong("cooled_at", cooledAt);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        gold = input.read("gold", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        coal = input.getIntOr("coal", 0);
        charcoal = input.getIntOr("charcoal", 0);
        progress = input.getIntOr("progress", -1);
        cooledAt = input.getLongOr("cooled_at", -1);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
