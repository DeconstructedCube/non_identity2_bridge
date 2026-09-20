package com.deconstructedcube.non_identity2_bridge;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.api.animation.NonActorEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NonIdentity2Bridge implements ModInitializer {

    public static final String MOD_ID = "non_identity2_bridge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        verifyHardDependencies();
        LOGGER.info("[NoN x Identity2 Bridge] Initializing bridge module...");
        NonActorEvents.PROVIDE.register(Identity2ActorHelper::provideMorphActorTags);
        LOGGER.info("[NoN x Identity2 Bridge] Registered actor tag provider.");
    }

    private static void verifyHardDependencies() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded("needsofnature")) {
            throw new IllegalStateException("[NoN x Identity2 Bridge] Missing hard dependency: 'needsofnature' is required!");
        }
        if (!loader.isModLoaded("identity2")) {
            throw new IllegalStateException("[NoN x Identity2 Bridge] Missing hard dependency: 'identity2' is required!");
        }
    }
}
