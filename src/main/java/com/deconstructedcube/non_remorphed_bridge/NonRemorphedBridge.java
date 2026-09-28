package com.deconstructedcube.non_remorphed_bridge;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import com.nonid.api.animation.NonActorEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NonRemorphedBridge implements ModInitializer {

    public static final String MOD_ID = "non_remorphed_bridge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        verifyHardDependencies();
        LOGGER.info("[NoN x ReMorphed Bridge] Initializing bridge module...");
        NonActorEvents.PROVIDE.register(RemorphedActorHelper::provideMorphActorTags);
        LOGGER.info("[NoN x ReMorphed Bridge] Registered actor tag provider.");

        // Allow morphed players to initiate action invitations on compatible living mobs
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() || hand != net.minecraft.world.InteractionHand.MAIN_HAND
                    || !player.getMainHandItem().isEmpty() || player.isSpectator()
                    || entity == null || entity.isRemoved() || entity.getUUID().equals(player.getUUID())) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            if (player.isShiftKeyDown() && entity instanceof net.minecraft.world.entity.LivingEntity
                    && !(entity instanceof net.minecraft.world.entity.player.Player)
                    && RemorphedActorHelper.getMorph(player) != null) {
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
            return net.minecraft.world.InteractionResult.PASS;
        });
        LOGGER.info("[NoN x ReMorphed Bridge] Registered morphed mob action invitation handler.");
    }

    private static void verifyHardDependencies() {
        FabricLoader loader = FabricLoader.getInstance();
        if (!loader.isModLoaded("needsofnature")) {
            throw new IllegalStateException("[NoN x ReMorphed Bridge] Missing hard dependency: 'needsofnature' is required!");
        }
        if (!loader.isModLoaded("remorphed") && !loader.isModLoaded("walkers")) {
            throw new IllegalStateException("[NoN x ReMorphed Bridge] Missing hard dependency: 'remorphed' or 'walkers' is required!");
        }
    }
}
