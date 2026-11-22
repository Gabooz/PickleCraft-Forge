package io.github.gabooz.picklecraft.mixin;

import io.github.gabooz.picklecraft.ground.Sailboat;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractBoat.class)
abstract class SailboatThrustMixin {
    @Shadow private AbstractBoat.Status status;

    @ModifyConstant(method = "controlBoat", constant = @Constant(floatValue = 0.04F))
    private float picklecraft$sailThrust(float rowingThrust) {
        return (Object) this instanceof Sailboat && status == AbstractBoat.Status.IN_WATER
            ? rowingThrust * 1.15F : rowingThrust;
    }
}
