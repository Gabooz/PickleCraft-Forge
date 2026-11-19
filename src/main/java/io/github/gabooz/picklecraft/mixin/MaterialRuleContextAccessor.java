package io.github.gabooz.picklecraft.mixin;

import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MaterialRuleContext.class)
public interface MaterialRuleContextAccessor {
    @Accessor("randomState")
    RandomState picklecraft$randomState();
}
