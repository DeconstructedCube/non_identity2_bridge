package com.deconstructedcube.non_woodwalkers_bridge.mixin.client;

import com.deconstructedcube.non_woodwalkers_bridge.client.WoodwalkersClientActorHelper;
import com.deconstructedcube.non_woodwalkers_bridge.util.WoodwalkersActorHelper;
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
    private static void non_woodwalkers_bridge$fixMorphedEntityTypeIdInPrepare(
            LivingEntity entity,
            LivingEntityRenderState state,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (state instanceof AnimationRenderStateAccess access) {
            Entity morph = WoodwalkersActorHelper.getMorph(entity);
            if (morph != null) {
                // 在 NoN 开始寻找动作的前一毫秒，将其底层身份伪装为真正的变身动物
                // 这彻底避免了 1.0.8 中由于二次调用 prepare 导致的 GeckoLib 动画控制器重置冻结问题
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
    private static void non_woodwalkers_bridge$guardNonPlayerActorTexture(
            LivingEntity entity,
            LivingEntityRenderState renderState,
            CallbackInfoReturnable<Identifier> cir
    ) {
        Entity morph = WoodwalkersActorHelper.getMorph(entity);

        // 核心准则：非人类角色绝对禁止赋予玩家皮肤！
        // 自动将变身形态委派给原版渲染管线提取原生贴图（支持变种与材质包），无需任何手动路径硬编码
        if (morph != null) {
            Identifier nativeTexture = WoodwalkersClientActorHelper.resolveMorphNativeTexture(morph, renderState);
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
    private static String non_woodwalkers_bridge$redirectPreferredModelVariant(Entity entity) {
        Entity morph = WoodwalkersActorHelper.getMorph(entity);
        return EntityVariants.resolveVariant(morph != null ? morph : entity);
    }
}
