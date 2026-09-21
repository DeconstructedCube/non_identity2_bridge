package com.deconstructedcube.non_identity2_bridge.mixin.client;

import com.deconstructedcube.non_identity2_bridge.client.Identity2ClientActorHelper;
import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.internal.animation.client.render.gecko.GeckoRenderTickets;
import com.nonid.internal.animation.client.render.gecko.GeckoReplacedRender;
import com.nonid.internal.animation.client.render.gecko.GeckoResourceResolver;
import com.nonid.internal.animation.util.EntityVariants;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.UUID;

@Mixin(GeckoReplacedRender.class)
public abstract class GeckoReplacedRenderMixin {

    @Inject(
            method = "resolveVanillaTexture",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_identity2_bridge$guardNonPlayerActorTexture(
            LivingEntity entity,
            LivingEntityRenderState renderState,
            CallbackInfoReturnable<Identifier> cir
    ) {
        Entity morph = Identity2ActorHelper.getMorph(entity);
        // 核心修复：仅当实体确实处于变身形态时才介入接管贴图提取！
        // 普通实体（自然生成的牛、狼、羊等）直接放行给 NoN 自带的原生贴图管线处理。
        if (morph != null) {
            Identifier nativeTexture = Identity2ClientActorHelper.resolveMorphNativeTexture(morph, renderState);
            cir.setReturnValue(nativeTexture);
        }
    }

    @Redirect(
            method = "preferredModelPaths",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/nonid/internal/animation/util/EntityVariants;resolveVariant(Lnet/minecraft/world/entity/Entity;)Ljava/lang/String;"
            )
    )
    private static String non_identity2_bridge$redirectPreferredModelVariant(Entity entity) {
        Entity morph = Identity2ActorHelper.getMorph(entity);
        return EntityVariants.resolveVariant(morph != null ? morph : entity);
    }

    @Inject(
            method = "prepare",
            at = @At("RETURN")
    )
    private static void non_identity2_bridge$ensureAnimationTimelineAndKey(
            LivingEntity entity,
            LivingEntityRenderState state,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (state instanceof GeoRenderState geoState) {
            UUID instanceId = geoState.getGeckolibData(GeckoRenderTickets.ANIMATION_INSTANCE_ID);
            if (instanceId != null) {
                Double timelineSeconds = geoState.getGeckolibData(GeckoRenderTickets.ANIMATION_TIMELINE_SECONDS);
                if (timelineSeconds == null || !Double.isFinite(timelineSeconds)) {
                    double computed = Identity2ClientActorHelper.resolveAuthoredTimelineSeconds(instanceId, tickDelta);
                    geoState.addGeckolibData(GeckoRenderTickets.ANIMATION_TIMELINE_SECONDS, computed);
                }
            }

            Entity morph = Identity2ActorHelper.getMorph(entity);
            if (morph != null) {
                Identifier morphTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(morph.getType());
                Identifier animResource = geoState.getGeckolibData(GeckoRenderTickets.ANIMATION_RESOURCE_ID);
                Identifier animId = geoState.getGeckolibData(GeckoRenderTickets.ANIMATION_ID);
                if (animResource != null && animId != null && morphTypeId != null) {
                    String currentKey = geoState.getGeckolibData(GeckoRenderTickets.ANIMATION_KEY);
                    String stageKey = Identity2ClientActorHelper.conjoinedStageKey(animId.getPath());
                    if (stageKey != null) {
                        String morphClip = stageKey + "_" + morphTypeId.getPath();
                        if (GeckoResourceResolver.hasBakedAnimationKey(animResource, morphClip)
                                && !morphClip.equals(currentKey)) {
                            geoState.addGeckolibData(GeckoRenderTickets.ANIMATION_KEY, morphClip);
                        }
                    }
                }
            }
        }
    }
}
