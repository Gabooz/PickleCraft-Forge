package io.github.gabooz.picklecraft;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/** Forge constructs this entry point when it loads PickleCraft on either side. */
@Mod(PickleCraft.MOD_ID)
public final class PickleCraft {
    public static final String MOD_ID = "picklecraft";

    private static final Logger LOGGER = LogUtils.getLogger();

    public PickleCraft() {
        // Wire up future gameplay registries here. Keep their behavior in feature classes.
        LOGGER.info("PickleCraft loaded");
    }
}
