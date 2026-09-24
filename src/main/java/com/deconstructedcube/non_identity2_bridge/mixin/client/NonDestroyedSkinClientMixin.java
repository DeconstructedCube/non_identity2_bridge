package com.deconstructedcube.non_identity2_bridge.mixin.client;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.client.NonDestroyedSkinClient;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NonDestroyedSkinClient.class)
public abstract class NonDestroyedSkinClientMixin {

    @Inject(
            method = "resolveBaseTexture",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_identity2_bridge$bypassMorphBaseTexture(
            LivingEntity entity,
            Identifier currentBaseTexture,
            CallbackInfoReturnable<Identifier> cir
    ) {
        if (Identity2ActorHelper.getMorph(entity) != null) {
            cir.setReturnValue(null);
        }
    }

    @Inject(
            method = "resolveOverlayTexture",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_identity2_bridge$bypassMorphOverlayTexture(
            LivingEntity entity,
            CallbackInfoReturnable<Identifier> cir
    ) {
        if (Identity2ActorHelper.getMorph(entity) != null) {
            cir.setReturnValue(null);
        }
    }

    @Inject(
            method = "resolveRenderedTexture",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_identity2_bridge$bypassMorphRenderedTexture(
            LivingEntity entity,
            Identifier renderedTexture,
            CallbackInfoReturnable<Identifier> cir
    ) {
        if (Identity2ActorHelper.getMorph(entity) != null) {
            cir.setReturnValue(renderedTexture);
        }
    }

    @Inject(
            method = "resolvePlayerModelTexture",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_identity2_bridge$bypassMorphPlayerModelTexture(
            LivingEntity entity,
            Identifier renderedTexture,
            NonDestroyedSkinClient.CpmTextureSource cpmTextureSource,
            CallbackInfoReturnable<Identifier> cir
    ) {
        if (Identity2ActorHelper.getMorph(entity) != null) {
            cir.setReturnValue(renderedTexture);
        }
    }
}
