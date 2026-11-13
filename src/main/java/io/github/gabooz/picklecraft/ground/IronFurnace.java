package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class IronFurnace extends BlockEntity {
    public static final int SMELT_TICKS = 60 * 20;
    public static final int INGOT_TICKS = 55 * 20;
    public static final int COOL_TICKS = 5 * 60 * 20;
    private static final ResourceKey<DamageType> HOT_IRON = ResourceKey.create(
        Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("picklecraft", "superheated_iron"));

    private ItemStack fuel = ItemStack.EMPTY;
    private ItemStack iron = ItemStack.EMPTY;
    private long startedAt = -1;

    public IronFurnace(BlockPos pos, BlockState state) {
        super(GroundContent.FURNACE_ENTITY.get(), pos, state);
    }

    public ItemStack fuel() {
        return fuel;
    }

    public ItemStack iron() {
        return iron;
    }

    public int elapsed() {
        return startedAt < 0 || level == null ? -1 : (int) Math.min(SMELT_TICKS + COOL_TICKS,
            Math.max(0L, level.getGameTime() - startedAt));
    }

    static void tick(ServerLevel level, BlockPos pos, BlockState state, IronFurnace furnace) {
        if (furnace.iron.isEmpty() && level.isEmptyBlock(pos.above(2))) {
            // Catch the item as it passes down the center of the open chute.
            var intake = new AABB(pos.getX() + 0.3125, pos.getY() + 1.03, pos.getZ() + 0.3125,
                pos.getX() + 0.6875, pos.getY() + 1.99, pos.getZ() + 0.6875);
            for (var entity : level.getEntitiesOfClass(ItemEntity.class, intake,
                    item -> item.getY() > pos.getY() + 1.03 && acceptsIron(item.getItem()))) {
                var remaining = entity.getItem().copy();
                furnace.iron = remaining.split(1);
                if (remaining.isEmpty()) {
                    entity.discard();
                } else {
                    entity.setItem(remaining);
                }
                furnace.sync();
                break;
            }
        }

        int time = furnace.elapsed();
        if (time < 0 && !furnace.fuel.isEmpty() && !furnace.iron.isEmpty()) {
            furnace.startedAt = level.getGameTime();
            furnace.setLit(true);
            furnace.sync();
        } else if (time >= INGOT_TICKS && !furnace.iron.is(Items.IRON_INGOT)) {
            furnace.iron = new ItemStack(Items.IRON_INGOT);
            furnace.sync();
        }
        if (time >= SMELT_TICKS && furnace.getBlockState().getValue(IronFurnaceBlock.LIT)) {
            furnace.fuel = ItemStack.EMPTY;
            furnace.setLit(false);
            furnace.sync();
        }
    }

    private static boolean acceptsIron(ItemStack item) {
        return item.is(Items.RAW_IRON) || item.is(Items.IRON_ORE) || item.is(Items.DEEPSLATE_IRON_ORE);
    }

    boolean canFuel() {
        return startedAt < 0 && fuel.isEmpty();
    }

    void insertFuel(ItemStack item) {
        fuel = item.copyWithCount(1);
        sync();
    }

    void retrieve(ServerLevel server, Player player, boolean removeFuel) {
        if (removeFuel && startedAt < 0 && !fuel.isEmpty()) {
            give(player, fuel);
            fuel = ItemStack.EMPTY;
            sync();
        } else if (!removeFuel && elapsed() >= INGOT_TICKS) {
            boolean hot = elapsed() < SMELT_TICKS + COOL_TICKS;
            ItemStack output = iron.is(Items.IRON_INGOT) ? iron : new ItemStack(Items.IRON_INGOT);
            iron = ItemStack.EMPTY;
            fuel = ItemStack.EMPTY;
            startedAt = -1;
            setLit(false);
            sync();
            give(player, output);
            if (hot) {
                var damage = server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(HOT_IRON);
                player.hurtServer(server, new DamageSource(damage), 22F);
            }
        }
    }

    List<ItemStack> drops() {
        var drops = new ArrayList<ItemStack>();
        if (startedAt < 0 && !fuel.isEmpty()) {
            drops.add(fuel.copy());
        }
        if (!iron.isEmpty()) {
            drops.add(iron.copy());
        }
        return drops;
    }

    private void give(Player player, ItemStack item) {
        if (!player.addItem(item)) {
            Block.popResource(level, worldPosition.above(2), item);
        }
    }

    private void setLit(boolean lit) {
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(IronFurnaceBlock.LIT, lit));
    }

    private void sync() {
        setChanged();
        var state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!fuel.isEmpty()) output.store("fuel", ItemStack.CODEC, fuel);
        if (!iron.isEmpty()) output.store("iron", ItemStack.CODEC, iron);
        output.putLong("started_at", startedAt);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fuel = input.read("fuel", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        iron = input.read("iron", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        startedAt = input.getLongOr("started_at", -1);
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
