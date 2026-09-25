package com.deconstructedcube.non_woodwalkers_bridge;

import com.deconstructedcube.non_woodwalkers_bridge.util.WoodwalkersActorHelper;
import com.nonid.api.animation.NonActorEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NonWoodwalkersBridge implements ModInitializer {

    public static final String MOD_ID = "non_woodwalkers_bridge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        verifyHardDependencies();
        LOGGER.info("[NoN x Woodwalkers Bridge] Initializing bridge module...");
        NonActorEvents.PROVIDE.register(WoodwalkersActorHelper::provideMorphActorTags);
        LOGGER.info("[NoN x Woodwalkers Bridge] Registered actor tag provider.");
    }

    private static void verifyHardDependencies() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded("needsofnature")) {
            throw new IllegalStateException("[NoN x Woodwalkers Bridge] Missing hard dependency: 'needsofnature' is required!");
        }
        if (!loader.isModLoaded("walkers")) {
            throw new IllegalStateException("[NoN x Woodwalkers Bridge] Missing hard dependency: 'walkers' is required!");
        }
    }
}
