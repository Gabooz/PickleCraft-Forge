package io.github.gabooz.picklecraft.zombies;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class ZoneZombie extends Zombie {
    private int zoneQ;
    private int zoneR;
    private int slot = -1;
    private int incarnation;
    private int variant;
    private boolean retreating;
    private long lastDawn = Long.MIN_VALUE;
    private int attackCooldown;
    private double blindOffset;
    private boolean escapingLight;

    public ZoneZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setCanPickUpLoot(false);
    }

    /** Saved variant IDs are 0 for tank, 1 for runner, and 2 for blind. */
    public static float healthFor(int variant) {
        return switch (variant) {
            case 0 -> 90;
            case 1 -> 60;
            default -> 70;
        };
    }

    public int zoneQ() { return zoneQ; }
    public int zoneR() { return zoneR; }
    public int slot() { return slot; }
    public int incarnation() { return incarnation; }
    public boolean retreating() { return retreating; }
    public String identity() { return zoneQ + "/" + zoneR + "/" + slot; }

    public void assign(ZombieLedger.Zone zone, int slot, int incarnation) {
        this.zoneQ = zone.q;
        this.zoneR = zone.r;
        this.slot = slot;
        this.incarnation = incarnation;
        this.variant = zone.souls.get(slot).variant();
        this.lastDawn = zone.dawn;
        configure(zone.seed);
        setHealth(zone.souls.get(slot).health());
    }

    private void configure(long seed) {
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(healthFor(variant));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(variant == 0 ? 8 : variant == 1 ? 12 : 14);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(variant == 0 ? 0.23 : variant == 1 ? 0.345 : 0.20);
        getAttribute(Attributes.SCALE).setBaseValue(variant == 0 ? 1.1 : 1.0);
        var traits = net.minecraft.util.RandomSource.create(seed ^ slot * 0x9E3779B97F4A7C15L);
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(36 + traits.nextInt(21));
        blindOffset = Math.toRadians(traits.nextDouble() * 120 - 60);
        setCustomName(Component.translatable("entity.picklecraft.zone_zombie." + variant));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    protected boolean isSunSensitive() { return false; }

    @Override
    protected boolean convertsInWater() { return false; }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }

    @Override
    public void checkDespawn() {
        // The roster and player chunk tracking control despawning.
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, net.minecraft.world.level.LevelReader level) {
        return level.getBrightness(LightLayer.BLOCK, pos) > 0 ? -1000 : 10;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        var ledger = ZombieLedger.get(level);
        var zone = ledger.find(zoneQ, zoneR);
        if (zone == null || !zone.alive(slot) || zone.souls.get(slot).incarnation() != incarnation) {
            discard();
            return;
        }
        if (!ZombieZones.night(level)) {
            ledger.dawn(zone, ZombieZones.dawn(level));
            retreating = true;
        }
        if (lastDawn != zone.dawn) {
            lastDawn = zone.dawn;
            setHealth(healthFor(variant));
        }
        if (attackCooldown > 0) attackCooldown--;
        if (tickCount % 20 == 0) remember(level);
        if (retreating && level.players().stream().noneMatch(player ->
                !player.isSpectator() && player.getChunkTrackingView().contains(chunkPosition()))) {
            discard();
            return;
        }
        double range = retreating ? 512 : getAttributeValue(Attributes.FOLLOW_RANGE);
        var nearest = level.players().stream().filter(player -> !player.isSpectator()
                && (retreating || !player.isCreative()) && distanceToSqr(player) <= range * range)
            .min(java.util.Comparator.comparingDouble(player -> distanceToSqr(player))).orElse(null);
        int light = Math.max(level.getBrightness(LightLayer.BLOCK, blockPosition()),
            level.getBrightness(LightLayer.BLOCK, blockPosition().above()));
        if (light > 0) {
            if (!escapingLight) getNavigation().stop();
            if (!escapingLight || getNavigation().isDone() || tickCount % 10 == 0) escapeLight(level, light);
            escapingLight = true;
            return;
        }
        escapingLight = false;
        var walking = getNavigation().getPath();
        if (walking != null && !walking.isDone()
                && level.getBrightness(LightLayer.BLOCK, walking.getNodePos(walking.getNextNodeIndex())) > 0) {
            getNavigation().stop();
        }
        if (nearest == null || nearest.isSpectator()) {
            if (tickCount % 40 == 0) safeMove(level, position().add(random.nextInt(17) - 8, 0, random.nextInt(17) - 8), 0, false);
            return;
        }
        if (retreating) {
            if (tickCount % 10 == 0) {
                var away = position().subtract(nearest.position()).multiply(1, 0, 1).normalize();
                if (away.lengthSqr() < 0.01) away = new Vec3(1, 0, 0);
                safeMove(level, position().add(away.scale(16)), 0, false);
            }
            return;
        }
        if (level.getBrightness(LightLayer.BLOCK, nearest.blockPosition()) > 0) {
            if (tickCount % 10 == 0) {
                var away = position().subtract(nearest.position()).multiply(1, 0, 1).normalize();
                safeMove(level, position().add(away.scale(12)), 0, true);
            }
            return;
        }
        if (distanceToSqr(nearest) < 4.0 && getSensing().hasLineOfSight(nearest) && attackCooldown == 0) {
            doHurtTarget(level, nearest);
            attackCooldown = 20;
        }
        if (tickCount % 20 == 0) {
            Vec3 target = nearest.position();
            if (variant == 2) {
                if (tickCount % 80 == 0) blindOffset = Math.toRadians(random.nextDouble() * 120 - 60);
                var direction = target.subtract(position()).multiply(1, 0, 1).normalize().yRot((float) blindOffset);
                target = position().add(direction.scale(Math.min(12, distanceTo(nearest))));
            }
            safeMove(level, target, 0, false);
        }
    }

    private void escapeLight(ServerLevel level, int currentLight) {
        record Escape(BlockPos pos, double score) {}
        var candidates = new java.util.ArrayList<Escape>();
        for (int i = 0; i < 48; i++) {
            var candidate = blockPosition().offset(random.nextInt(33) - 16, 0, random.nextInt(33) - 16);
            if (!level.hasChunkAt(candidate)) continue;
            candidate = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
            int light = Math.max(level.getBrightness(LightLayer.BLOCK, candidate),
                level.getBrightness(LightLayer.BLOCK, candidate.above()));
            if (light < currentLight) {
                candidates.add(new Escape(candidate, (currentLight - light) * 100 - candidate.distSqr(blockPosition())));
            }
        }
        candidates.sort(java.util.Comparator.comparingDouble(Escape::score).reversed());
        for (var candidate : candidates) {
            if (safeMove(level, Vec3.atBottomCenterOf(candidate.pos()), currentLight, true)) return;
        }
    }

    private boolean safeMove(ServerLevel level, Vec3 target, int maxLight, boolean escaping) {
        var pos = BlockPos.containing(target);
        if (!level.hasChunkAt(pos)) return false;
        Path path = getNavigation().createPath(pos, 1);
        if (path == null) return false;
        for (int i = 0; i < path.getNodeCount(); i++) {
            var node = path.getNodePos(i);
            int light = Math.max(level.getBrightness(LightLayer.BLOCK, node),
                level.getBrightness(LightLayer.BLOCK, node.above()));
            if (light > maxLight) return false;
        }
        return getNavigation().moveTo(path, escaping ? 1.35 : 1.0);
    }

    private void remember(ServerLevel level) {
        var ledger = ZombieLedger.get(level);
        var zone = ledger.find(zoneQ, zoneR);
        if (zone != null && zone.alive(slot) && zone.souls.get(slot).incarnation() == incarnation) {
            var old = zone.souls.get(slot);
            if (old.health() != getHealth()) {
                zone.souls.set(slot, new ZombieLedger.Soul(variant, incarnation, getHealth()));
                ledger.setDirty();
            }
        }
    }

    public void detached(ServerLevel level) {
        if (!ZombieZones.night(level)) {
            var zone = ZombieLedger.get(level).find(zoneQ, zoneR);
            if (zone != null) {
                ZombieLedger.get(level).dawn(zone, ZombieZones.dawn(level));
                if (lastDawn != zone.dawn) {
                    lastDawn = zone.dawn;
                    setHealth(healthFor(variant));
                }
            }
        }
        remember(level);
        var ledger = ZombieLedger.get(level);
        var zone = ledger.find(zoneQ, zoneR);
        if (zone != null && zone.alive(slot) && zone.souls.get(slot).incarnation() == incarnation) {
            zone.souls.set(slot, new ZombieLedger.Soul(variant, incarnation + 1, getHealth()));
            ledger.setDirty();
        }
    }

    @Override
    public void die(DamageSource damage) {
        if (level() instanceof ServerLevel level) {
            var ledger = ZombieLedger.get(level);
            var zone = ledger.find(zoneQ, zoneR);
            if (zone != null && zone.alive(slot) && zone.souls.get(slot).incarnation() == incarnation) ledger.kill(zone, slot);
        }
        super.die(damage);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("zone_q", zoneQ);
        output.putInt("zone_r", zoneR);
        output.putInt("roster_slot", slot);
        output.putInt("incarnation", incarnation);
        output.putInt("variant", variant);
        output.putBoolean("retreating", retreating);
        output.putDouble("blind_offset", blindOffset);
        output.putLong("last_dawn", lastDawn);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        zoneQ = input.getIntOr("zone_q", 0);
        zoneR = input.getIntOr("zone_r", 0);
        slot = input.getIntOr("roster_slot", -1);
        incarnation = input.getIntOr("incarnation", 0);
        variant = input.getIntOr("variant", 0);
        retreating = input.getBooleanOr("retreating", false);
        blindOffset = input.getDoubleOr("blind_offset", 0);
        lastDawn = input.getLongOr("last_dawn", Long.MIN_VALUE);
    }
}
