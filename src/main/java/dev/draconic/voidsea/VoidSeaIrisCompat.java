package dev.draconic.voidsea;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(VoidSeaIrisCompat.MOD_ID)
public final class VoidSeaIrisCompat {
    public static final String MOD_ID = "void_sea_iris_compat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VoidSeaIrisCompat() {
        LOGGER.info("Void Sea Iris Compat loaded for Minecraft 1.21.1");
    }
}
