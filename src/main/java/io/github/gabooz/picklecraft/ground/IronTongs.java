package io.github.gabooz.picklecraft.ground;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;

public final class IronTongs extends Item {
    private static final String COOL_AT = "picklecraft_cool_at";
    private static final Identifier LOADED_MODEL = Identifier.fromNamespaceAndPath("picklecraft", "iron_tongs_loaded");

    public IronTongs(Properties properties) {
        super(properties);
    }

    static boolean loaded(ItemStack stack) {
        return stack.is(GroundContent.IRON_TONGS.get()) && deadline(stack) >= 0;
    }

    private static long deadline(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getLongOr(COOL_AT, -1);
    }

    static boolean cool(ItemStack stack, ServerLevel level) {
        return loaded(stack) && level.getGameTime() >= deadline(stack);
    }

    static void grip(ItemStack stack, long coolAt) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putLong(COOL_AT, coolAt));
        stack.set(DataComponents.ITEM_MODEL, LOADED_MODEL);
    }

    static ItemStack release(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.remove(COOL_AT));
        stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("picklecraft", "iron_tongs"));
        return Items.IRON_INGOT.getDefaultInstance();
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (cool(stack, level)) {
            var ingot = release(stack);
            if (!(owner instanceof Player player) || !player.addItem(ingot)) {
                Block.popResource(level, owner.blockPosition(), ingot);
            }
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level() instanceof ServerLevel level && cool(stack, level)) {
            // Replace the tracked stack so clients see the empty tongs immediately.
            var emptyTongs = stack.copy();
            var ingot = release(emptyTongs);
            entity.setItem(emptyTongs);
            Block.popResource(level, entity.blockPosition(), ingot);
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return quench(context.getLevel(), context.getClickedPos(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        return hit.getType() == HitResult.Type.BLOCK
            ? quench(level, hit.getBlockPos(), player, hand) : InteractionResult.PASS;
    }

    private static InteractionResult quench(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (player == null) {
            return InteractionResult.PASS;
        }
        var stack = player.getItemInHand(hand);
        if (!loaded(stack) || !level.mayInteract(player, pos)
                || !(level.getFluidState(pos).is(FluidTags.WATER) || level.getBlockState(pos).is(Blocks.WATER_CAULDRON))) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            boolean hot = !cool(stack, server);
            var ingot = release(stack);
            if (!player.addItem(ingot)) {
                Block.popResource(server, player.blockPosition(), ingot);
            }
            if (hot) {
                stack.hurtAndBreak(1, player, hand);
                server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.15F);
                server.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.95,
                    pos.getZ() + 0.5, 14, 0.2, 0.15, 0.2, 0.035);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
