package com.deconstructedcube.non_identity2_bridge.mixin.client;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.internal.animation.client.render.AnimationRenderStateAccess;
import com.nonid.internal.animation.client.render.gecko.GeckoReplacedRender;
import com.nonid.internal.animation.util.EntityVariants;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        Identifier entityTypeId = null;
        if (renderState instanceof AnimationRenderStateAccess access) {
            entityTypeId = access.afw$getEntityTypeId();
        }

        boolean isNonPlayerRole = entityTypeId != null && !Identity2ActorHelper.PLAYER_TYPE_ID.equals(entityTypeId);
        Entity morph = Identity2ActorHelper.getMorph(entity);

        // 核心准则：非人类角色绝对禁止赋予玩家皮肤！
        // 自动将变身形态委派给原版渲染管线提取原生贴图（支持变种与材质包），无需任何手动路径硬编码
        if (isNonPlayerRole || morph != null) {
            Identifier nativeTexture = morph != null ? Identity2ActorHelper.resolveMorphNativeTexture(morph) : null;
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
}
