package com.deconstructedcube.non_remorphed_bridge.mixin.client;

import com.deconstructedcube.non_remorphed_bridge.client.RemorphedClientActorHelper;
import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import com.nonid.internal.animation.client.render.AnimationRenderStateAccess;
import com.nonid.internal.animation.client.render.gecko.GeckoReplacedRender;
import com.nonid.internal.animation.util.EntityVariants;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GeckoReplacedRender.class)
public abstract class GeckoReplacedRenderMixin {

    @Inject(
            method = "prepare",
            at = @At("HEAD")
    )
    private static void non_remorphed_bridge$fixMorphedEntityTypeIdInPrepare(
            LivingEntity entity,
            LivingEntityRenderState state,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (state instanceof AnimationRenderStateAccess access) {
            Entity morph = RemorphedActorHelper.getMorph(entity);
            if (morph != null) {
                EntityType<?> morphType = morph.getType();
                Identifier morphId = BuiltInRegistries.ENTITY_TYPE.getKey(morphType);
                access.afw$setEntityTypeId(morphId);
            }
        }
    }

    @Inject(
            method = "resolveVanillaTexture",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_remorphed_bridge$guardNonPlayerActorTexture(
            LivingEntity entity,
            LivingEntityRenderState renderState,
            CallbackInfoReturnable<Identifier> cir
    ) {
        Entity morph = RemorphedActorHelper.getMorph(entity);
        if (morph != null) {
            Identifier nativeTexture = RemorphedClientActorHelper.resolveMorphNativeTexture(morph, renderState);
            if (nativeTexture != null) {
                cir.setReturnValue(nativeTexture);
            }
        }
    }

    @Redirect(
            method = "preferredModelPaths",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/nonid/internal/animation/util/EntityVariants;resolveVariant(Lnet/minecraft/world/entity/Entity;)Ljava/lang/String;"
            )
    )
    private static String non_remorphed_bridge$redirectPreferredModelVariant(Entity entity) {
        Entity morph = RemorphedActorHelper.getMorph(entity);
        return EntityVariants.resolveVariant(morph != null ? morph : entity);
    }
}
