package io.github.gabooz.picklecraft.cattle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.EnumSet;
import java.util.UUID;

public final class HerdCow extends Cow {
    public static final int GROWTH_DAYS = 15;
    private CattleStats stats = new CattleStats();
    private UUID mother;
    private long born = Long.MIN_VALUE;
    private long lastNursed = Long.MIN_VALUE;
    private long milkedDay = Long.MIN_VALUE;

    public HerdCow(EntityType<? extends Cow> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        configure();
    }

    public boolean isBull() { return getType() == CattleContent.BULL.get(); }
    public CattleStats stats() { return stats; }
    public UUID mother() { return mother; }

    public void configure() {
        setVariant(registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.COW_VARIANT)
            .getOrThrow(isBull() ? CowVariants.WARM : CowVariants.TEMPERATE));
        getAttribute(Attributes.SCALE).setBaseValue(isBull() ? 1.1 : 1);
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.EntitySpawnReason reason,
            net.minecraft.world.entity.SpawnGroupData data) {
        var result = super.finalizeSpawn(level, difficulty, reason, data);
        configure();
        return result;
    }

    public void assignMother(HerdCow parent, long time) {
        mother = parent.getUUID();
        born = time;
        lastNursed = time;
        setAge(-GROWTH_DAYS * 24000 * 4);
        if (level() instanceof ServerLevel server) CattleData.get(server).addCalf(mother, getUUID(), born);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof FollowParentGoal);
        goalSelector.addGoal(2, new Goal() {
            private HerdCow parent;
            { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
            @Override
            public boolean canUse() {
                if (!isBaby() || mother == null || !(level() instanceof ServerLevel server)) return false;
                parent = server.getEntityInAnyDimension(mother) instanceof HerdCow cow ? cow : null;
                return parent != null && parent.isAlive() && parent.level() == level()
                    && distanceToSqr(parent) > 9 && distanceToSqr(parent) < 4096;
            }
            @Override
            public boolean canContinueToUse() {
                return parent != null && parent.isAlive() && isBaby() && distanceToSqr(parent) > 4
                    && distanceToSqr(parent) < 4096;
            }
            @Override
            public void tick() {
                getLookControl().setLookAt(parent, 30, 30);
                if (tickCount % 10 == 0) getNavigation().moveTo(parent, 1.2);
            }
            @Override
            public void stop() { getNavigation().stop(); parent = null; }
        });
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !isBaby() && (!(level() instanceof ServerLevel server)
            || isBull() || !CattleData.get(server).hasCalves(getUUID(), server.getDefaultClockTime()));
    }

    @Override
    public boolean canMate(Animal other) {
        return other instanceof HerdCow partner && partner != this && partner.isBull() != isBull()
            && !isBaby() && !partner.isBaby() && isInLove() && partner.isInLove()
            && (!(level() instanceof ServerLevel server) || !CattleData.get(server).hasCalves(
                isBull() ? partner.getUUID() : getUUID(), server.getDefaultClockTime()));
    }

    @Override
    public HerdCow getBreedOffspring(ServerLevel level, AgeableMob other) {
        if (!(other instanceof HerdCow partner) || !canMate(partner)) return null;
        var child = (random.nextBoolean() ? CattleContent.BULL : CattleContent.COW).get()
            .create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
        if (child != null) {
            child.configure();
            child.stats = CattleStats.inherit(stats, partner.stats, random);
        }
        return child;
    }

    @Override
    public void finalizeSpawnChildFromBreeding(ServerLevel level, Animal partner, AgeableMob child) {
        if (child instanceof HerdCow calf && partner instanceof HerdCow cattle) {
            calf.assignMother(isBull() ? cattle : this, level.getDefaultClockTime());
        }
        super.finalizeSpawnChildFromBreeding(level, partner, child);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal partner) {
        if (canMate(partner)) super.spawnChildFromBreeding(level, partner);
    }

    @Override
    public void ageUp(int seconds, boolean forced) {
        if (born == Long.MIN_VALUE) super.ageUp(seconds, forced);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        var held = player.getItemInHand(hand);
        if (held.is(Items.BUCKET)) {
            long day = Math.floorDiv(level().getDefaultClockTime(), 24000);
            if (isBull() || isBaby() || day <= milkedDay) return InteractionResult.FAIL;
            if (!level().isClientSide()) {
                milkedDay = day;
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.MILK_BUCKET)));
                playSound(net.minecraft.sounds.SoundEvents.COW_MILK, 1, 1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (born == Long.MIN_VALUE || tickCount % 20 != 0) return;
        long now = level.getDefaultClockTime();
        var data = CattleData.get(level);
        long remaining = GROWTH_DAYS * 24000L - (now - born);
        if (remaining <= 0) {
            setAge(0);
            data.finishCalf(getUUID());
            born = Long.MIN_VALUE;
            return;
        }
        setAge((int) -Math.min(Integer.MAX_VALUE, remaining * 4));
        if (mother == null) return;
        data.addCalf(mother, getUUID(), born);
        var parent = level.getEntityInAnyDimension(mother);
        if (data.nurtured(mother, getUUID(), now) && parent instanceof HerdCow cow
                && cow.isAlive() && cow.level() == level && distanceToSqr(cow) <= 144) {
            lastNursed = now;
        } else if (now - lastNursed > 24000) {
            hurtServer(level, damageSources().generic(), 1);
        }
    }

    @Override
    public void die(DamageSource damage) {
        if (level() instanceof ServerLevel server) CattleData.get(server).finishCalf(getUUID());
        super.die(damage);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("cattle_stats", CattleStats.CODEC, stats);
        if (mother != null) output.putString("mother", mother.toString());
        output.putLong("born", born);
        output.putLong("last_nursed", lastNursed);
        output.putLong("milked_day", milkedDay);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        stats = input.read("cattle_stats", CattleStats.CODEC).orElseGet(CattleStats::new);
        mother = input.getString("mother").map(UUID::fromString).orElse(null);
        born = input.getLongOr("born", Long.MIN_VALUE);
        lastNursed = input.getLongOr("last_nursed", born);
        milkedDay = input.getLongOr("milked_day", Long.MIN_VALUE);
        configure();
    }
}
