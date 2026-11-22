package io.github.gabooz.picklecraft.ground;

import io.github.gabooz.picklecraft.PickleCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class SteamTrain extends VehicleEntity {
    private static final DeferredRegister<EntityType<?>> TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, PickleCraft.MOD_ID);
    public static final RegistryObject<EntityType<SteamTrain>> TYPE = TYPES.register("steam_train", () ->
        EntityType.Builder.of(SteamTrain::new, MobCategory.MISC).sized(3, 3).clientTrackingRange(12)
            .updateInterval(1).noLootTable().build(TYPES.key("steam_train")));

    public static final int FUEL_INTERVAL = 5 * 60 * 20;
    public static final int STARTUP_COAL = 3;
    public static final int TENDER_CAPACITY = 64;
    private static final float MAX_SPEED = 0.68F;
    private static final EntityDataAccessor<Integer> HEADING =
        SynchedEntityData.defineId(SteamTrain.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COAL =
        SynchedEntityData.defineId(SteamTrain.class, EntityDataSerializers.INT);

    private boolean heated;
    private int fuelTicks;
    private int throttle;
    private boolean brake = true;
    private boolean jumpHeld;
    private float speed;

    private SteamTrain(EntityType<SteamTrain> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static void register(BusGroup bus) { TYPES.register(bus); }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEADING, Direction.SOUTH.get3DDataValue());
        builder.define(COAL, 0);
    }

    public Direction heading() { return Direction.from3DDataValue(entityData.get(HEADING)); }
    public int coal() { return entityData.get(COAL); }

    private void setCoal(int amount) { entityData.set(COAL, amount); }

    void align(Direction heading, BlockPos track) {
        entityData.set(HEADING, heading.get3DDataValue());
        snapTo(track.getX() + 0.5 - heading.getStepX() * 0.5, track.getY() + 0.2,
            track.getZ() + 0.5 - heading.getStepZ() * 0.5, heading.toYRot(), 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (HEADING.equals(key)) setBoundingBox(makeBoundingBox(position()));
    }

    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        if (entityData == null) return super.makeBoundingBox(pos);
        var aft = along(pos, -8.99);
        var fore = along(pos, 4.99);
        double acrossX = heading().getAxis() == Direction.Axis.Z ? 1.46 : 0;
        double acrossZ = heading().getAxis() == Direction.Axis.X ? 1.46 : 0;
        return new AABB(Math.min(aft.x, fore.x) - acrossX, pos.y, Math.min(aft.z, fore.z) - acrossZ,
            Math.max(aft.x, fore.x) + acrossX, pos.y + 2.95, Math.max(aft.z, fore.z) + acrossZ);
    }

    @Override protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this, 3);
    }

    @Override public boolean isPickable() { return !isRemoved(); }
    @Override public boolean canBeCollidedWith(Entity entity) { return !isRemoved() && !isPassengerOfSameVehicle(entity); }
    @Override protected boolean canAddPassenger(Entity passenger) { return getPassengers().isEmpty(); }
    @Override protected Item getDropItem() { return GroundContent.TRAIN_ITEM.get(); }

    @Override
    protected void positionRider(Entity rider, Entity.MoveFunction move) {
        var seat = along(position(), -3.4);
        move.accept(rider, seat.x, seat.y + 0.75, seat.z);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity rider) {
        var side = heading().getClockWise();
        var seat = along(position(), -3.4);
        for (int sign : new int[]{1, -1}) {
            var exit = seat.add(side.getStepX() * 2 * sign, 0.1, side.getStepZ() * 2 * sign);
            if (level().noCollision(rider, rider.getBoundingBox().move(exit.subtract(rider.position())))) return exit;
        }
        return seat.add(0, 3, 0);
    }

    private Vec3 along(Vec3 start, double blocks) {
        var direction = heading();
        return start.add(direction.getStepX() * blocks, 0, direction.getStepZ() * blocks);
    }

    /** Check the entire fourteen-block train, including the coupled coal tender. */
    boolean routeFits(Vec3 position) {
        var last = BlockPos.containing(along(position, 4.99));
        for (var sample = BlockPos.containing(along(position, -8.99)); ; sample = sample.relative(heading())) {
            if (!RailwayBlock.finished(level(), sample, heading().getAxis())) return false;
            if (sample.equals(last)) break;
        }
        return level().noCollision(this, makeBoundingBox(position));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) {
            getInterpolation().interpolate();
            return;
        }
        if (getFirstPassenger() instanceof ServerPlayer driver) {
            var input = driver.getLastClientInput();
            if (input.forward()) throttle = 1;
            if (input.backward()) throttle = -1;
            if (input.jump() && !jumpHeld) toggleBrake(driver);
            jumpHeld = input.jump();
        } else {
            jumpHeld = false;
        }

        if (!brake && throttle != 0 && !heated && coal() >= STARTUP_COAL) {
            setCoal(coal() - STARTUP_COAL);
            heated = true;
            fuelTicks = FUEL_INTERVAL;
        }
        if (heated && tickCount % 8 == 0) {
            var stack = along(position(), 2.5);
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                stack.x, stack.y + 2.86, stack.z, 0, 0, 0.03, 0, 0.02);
        }
        float target = !brake && heated && (fuelTicks > 0 || coal() > 0) ? throttle * MAX_SPEED : 0;
        speed = Mth.approach(speed, target, brake ? 0.16F : 0.035F);
        if (Math.abs(speed) < 0.0001) return;
        var next = along(position(), speed);
        if (!routeFits(next) || !supportTrain(server, next)) {
            speed = 0;
            brake = true;
            return;
        }
        if (heated && fuelTicks == 0 && coal() > 0) {
            setCoal(coal() - 1);
            fuelTicks = FUEL_INTERVAL;
        }
        if (fuelTicks > 0) fuelTicks--;
        setPos(next);
    }

    private boolean supportTrain(ServerLevel level, Vec3 position) {
        boolean stable = true;
        var last = BlockPos.containing(along(position, 4.99));
        for (var rail = BlockPos.containing(along(position, -8.99)); ; rail = rail.relative(heading())) {
            stable &= RailwayBlock.bearLoad(level, rail, heading().getAxis());
            if (rail.equals(last)) break;
        }
        return stable;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 hit) {
        var held = player.getItemInHand(hand);
        double along = hit.x * heading().getStepX() + hit.z * heading().getStepZ();
        double across = hit.x * heading().getStepZ() - hit.z * heading().getStepX();

        if ((held.is(Items.COAL) || held.is(Items.CHARCOAL)) && along < -2) {
            if (!player.mayBuild()) return InteractionResult.FAIL;
            if (!level().isClientSide()) {
                int moved = Math.min(TENDER_CAPACITY - coal(), player.isShiftKeyDown() ? held.getCount() : 1);
                setCoal(coal() + moved);
                held.consume(moved, player);
                tell(player, "message.picklecraft.train_coal", coal(), TENDER_CAPACITY);
            }
            return InteractionResult.SUCCESS;
        }
        if (!held.isEmpty()) return InteractionResult.PASS;
        if (along >= 2.0) {
            if (!level().isClientSide()) level().playSound(null, blockPosition(), SoundEvents.BELL_BLOCK,
                SoundSource.BLOCKS, 1.1F, 0.85F);
            return InteractionResult.SUCCESS;
        }
        if (along < -5.0) {
            if (!level().isClientSide()) tell(player, "message.picklecraft.train_coal", coal(), TENDER_CAPACITY);
            return InteractionResult.SUCCESS;
        }
        if (along < -2.5 && player.isShiftKeyDown()) {
            if (!level().isClientSide()) {
                if (across >= 0) toggleBrake(player);
                else {
                    throttle = throttle == 1 ? -1 : throttle + 1;
                    tell(player, throttle > 0 ? "message.picklecraft.train_forward"
                        : throttle < 0 ? "message.picklecraft.train_reverse" : "message.picklecraft.train_neutral");
                    clickLever();
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!level().isClientSide()) player.startRiding(this);
        return InteractionResult.SUCCESS;
    }

    private void toggleBrake(Player player) {
        brake = !brake;
        clickLever();
        tell(player, brake ? "message.picklecraft.train_brake" : "message.picklecraft.train_ready");
    }

    private void clickLever() {
        level().playSound(null, blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.8F, brake ? 0.6F : 0.5F);
    }

    private static void tell(Player player, String key, Object... args) {
        if (player instanceof ServerPlayer server) server.sendSystemMessage(Component.translatable(key, args), true);
    }

    @Override protected void addAdditionalSaveData(ValueOutput out) {
        out.putInt("heading", heading().get3DDataValue());
        out.putInt("coal", coal());
        out.putInt("fuel_ticks", fuelTicks);
        out.putInt("throttle", throttle);
        out.putBoolean("brake", brake);
        out.putBoolean("heated", heated);
    }

    @Override protected void readAdditionalSaveData(ValueInput in) {
        var direction = Direction.from3DDataValue(in.getIntOr("heading", Direction.SOUTH.get3DDataValue()));
        entityData.set(HEADING, (direction.getAxis().isHorizontal() ? direction : Direction.SOUTH).get3DDataValue());
        setCoal(Mth.clamp(in.getIntOr("coal", 0), 0, TENDER_CAPACITY));
        fuelTicks = Mth.clamp(in.getIntOr("fuel_ticks", 0), 0, FUEL_INTERVAL);
        throttle = Mth.clamp(in.getIntOr("throttle", 0), -1, 1);
        brake = in.getBooleanOr("brake", true);
        heated = in.getBooleanOr("heated", false);
    }
}
