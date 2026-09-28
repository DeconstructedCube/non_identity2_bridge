package com.deconstructedcube.non_remorphed_bridge.mixin;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import com.nonid.NeedsOfNature;
import com.nonid.NonPlayerInteractionSystem;
import com.nonid.internal.animation.api.AnimationService;
import com.nonid.network.PlayerAnimationSelectionS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(NonPlayerInteractionSystem.class)
public abstract class NonPlayerInteractionSystemMixin {

    @Shadow
    @Final
    private static Map<UUID, Object> SELECTIONS;

    @Shadow
    @Final
    private static Map<UUID, UUID> SESSION_BY_PLAYER;

    @Shadow
    private static boolean validInteractionPair(ServerPlayer requester, Entity target) {
        throw new AssertionError();
    }

    @Shadow
    private static boolean hasRequestEnergy(ServerPlayer requester) {
        throw new AssertionError();
    }

    @Shadow
    private static void sendStandaloneFailure(ServerPlayer requester, String messageKey) {
        throw new AssertionError();
    }

    @Shadow
    private static List<Entity> sortedActors(Entity first, Entity second) {
        throw new AssertionError();
    }

    @Inject(
            method = "handleInteraction",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_remorphed_bridge$handleMorphedMobInteraction(
            ServerLevel world,
            ServerPlayer requester,
            Entity clickedEntity,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (world == null || requester == null || clickedEntity == null || clickedEntity.isRemoved()
                || requester.level() != world || clickedEntity.level() != world
                || requester.getUUID().equals(clickedEntity.getUUID())) {
            return;
        }

        if (!(clickedEntity instanceof ServerPlayer) && clickedEntity instanceof LivingEntity mobTarget) {
            if (requester.isShiftKeyDown() && RemorphedActorHelper.getMorph(requester) != null) {
                cir.setReturnValue(non_remorphed_bridge$beginMorphedMobDirectSelection(world, requester, mobTarget));
            }
        }
    }

    @Inject(
            method = "isBusyForDirectRequest",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_remorphed_bridge$allowMobBusyCheck(
            ServerLevel world,
            ServerPlayer requester,
            Entity target,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (requester != null && target instanceof LivingEntity && !(target instanceof ServerPlayer)) {
            boolean busy = AnimationService.isActorPendingOrActive(world, requester.getUUID())
                    || AnimationService.isActorPendingOrActive(world, target.getUUID())
                    || NeedsOfNature.hasActivePlayerAnimation(requester);
            cir.setReturnValue(busy);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Unique
    private static boolean non_remorphed_bridge$beginMorphedMobDirectSelection(
            ServerLevel world,
            ServerPlayer requester,
            LivingEntity target
    ) {
        if (!validInteractionPair(requester, target)) {
            return false;
        }
        if (!hasRequestEnergy(requester)) {
            sendStandaloneFailure(requester, "gui.needsofnature.player_request.energy_low");
            return true;
        }
        if (SESSION_BY_PLAYER.containsKey(requester.getUUID())) {
            sendStandaloneFailure(requester, "gui.needsofnature.player_request.already_pending");
            return true;
        }
        if (NeedsOfNature.hasActivePlayerAnimation(requester) || AnimationService.isActorPendingOrActive(world, target.getUUID())) {
            sendStandaloneFailure(requester, "gui.needsofnature.player_request.player_busy");
            return true;
        }
        List<Entity> actors = sortedActors(requester, target);
        List<UUID> actorUuids = actors.stream().map(Entity::getUUID).toList();

        try {
            Class<?> systemClass = NonPlayerInteractionSystem.class;
            Class<?> sessionClass = Class.forName("com.nonid.NonPlayerInteractionSystem$SelectionSession");
            Class<?> requestKindClass = Class.forName("com.nonid.NonPlayerInteractionSystem$RequestKind");

            Method enumMethod = systemClass.getDeclaredMethod("enumerateCandidates", ServerLevel.class, List.class, Entity.class, Identifier.class);
            enumMethod.setAccessible(true);
            List<?> candidates = (List<?>) enumMethod.invoke(null, world, actors, target, null);

            if (candidates == null || candidates.isEmpty()) {
                sendStandaloneFailure(requester, "gui.needsofnature.player_request.no_match");
                return true;
            }

            UUID requestId = UUID.randomUUID();
            Object directKind = Enum.valueOf((Class<Enum>) requestKindClass, "DIRECT");
            Constructor<?> ctor = sessionClass.getDeclaredConstructor(
                    UUID.class, requestKindClass, net.minecraft.resources.ResourceKey.class,
                    UUID.class, UUID.class, UUID.class, long.class, List.class, List.class
            );
            ctor.setAccessible(true);
            Object session = ctor.newInstance(
                    requestId, directKind, world.dimension(), requester.getUUID(), target.getUUID(),
                    null, world.getGameTime() + 300, List.copyOf(actorUuids), List.copyOf(candidates)
            );

            SELECTIONS.put(requestId, session);
            SESSION_BY_PLAYER.put(requester.getUUID(), requestId);

            Method jsonMethod = systemClass.getDeclaredMethod("candidatesJson", sessionClass, UUID.class);
            jsonMethod.setAccessible(true);
            String json = (String) jsonMethod.invoke(null, session, requester.getUUID());

            ServerPlayNetworking.send(requester, new PlayerAnimationSelectionS2CPayload(
                    requestId, target.getUUID(), target.getName().getString(), json, false
            ));
            return true;
        } catch (Exception e) {
            NeedsOfNature.LOGGER.error("[NoN-Bridge] Failed to initiate morphed mob selection", e);
            sendStandaloneFailure(requester, "gui.needsofnature.player_request.invalid");
            return true;
        }
    }
}
