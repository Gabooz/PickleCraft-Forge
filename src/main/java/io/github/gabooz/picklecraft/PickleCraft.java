package io.github.gabooz.picklecraft;

import com.mojang.logging.LogUtils;
import io.github.gabooz.picklecraft.client.InventoryScreens;
import io.github.gabooz.picklecraft.world.OverworldFeatures;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/** Forge constructs this entry point when it loads PickleCraft on either side. */
@Mod(PickleCraft.MOD_ID)
public final class PickleCraft {
    public static final String MOD_ID = "picklecraft";

    private static final Logger LOGGER = LogUtils.getLogger();

    public PickleCraft(FMLJavaModLoadingContext context) {
        var modBus = context.getModBusGroup();
        OverworldFeatures.SERIALIZERS.register(modBus);
        EntityJoinLevelEvent.BUS.addListener(PickleCraft::preventOverworldMobs);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FMLClientSetupEvent.getBus(modBus).addListener(event -> InventoryScreens.register());
        }
        LOGGER.info("PickleCraft loaded");
    }

    private static boolean preventOverworldMobs(EntityJoinLevelEvent event) {
        return !event.getLevel().isClientSide()
            && event.getLevel().dimension().equals(Level.OVERWORLD)
            && event.getEntity() instanceof Mob;
    }
}
