package com.deconstructedcube.non_identity2_bridge.mixin.client;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.NeedsOfNatureClient;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(NeedsOfNatureClient.class)
public abstract class NeedsOfNatureClientMixin {

    @Inject(
            method = "resolveDestroyedSkinBaseTexture",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_identity2_bridge$bypassMorphedDestroyedSkin(
            LivingEntity entity,
            Identifier currentTexture,
            CallbackInfoReturnable<Identifier> cir
    ) {
        if (Identity2ActorHelper.getMorph(entity) != null) {
            cir.setReturnValue(currentTexture);
        }
    }

    @Inject(
            method = "resolvePlayerSkinPartHiddenCubeIndices",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_identity2_bridge$bypassMorphedSkinPartHiding(
            LivingEntity entity,
            CallbackInfoReturnable<Map<String, Set<Integer>>> cir
    ) {
        if (Identity2ActorHelper.getMorph(entity) != null) {
            cir.setReturnValue(Map.of());
        }
    }
}
