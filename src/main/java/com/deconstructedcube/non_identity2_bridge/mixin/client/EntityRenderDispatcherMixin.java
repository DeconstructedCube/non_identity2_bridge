package com.deconstructedcube.non_identity2_bridge.mixin.client;

import com.nonid.internal.animation.client.runtime.ClientAnimationRuntime;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityRenderDispatcher.class, priority = 1500)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
            method = "extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN")
    )
    private void non_identity2_bridge$lockMorphedAnimationOrientation(
            Entity entity,
            float tickDelta,
            CallbackInfoReturnable<EntityRenderState> cir
    ) {
        EntityRenderState state = cir.getReturnValue();
        if (state instanceof LivingEntityRenderState livingState && entity instanceof LivingEntity) {
            if (ClientAnimationRuntime.hasActiveInstances()
                    && ClientAnimationRuntime.isActorActive(entity.getUUID())
                    && !ClientAnimationRuntime.isActorUsingSimplifiedPresentation(entity.getUUID())) {

                // 锁定身体和头部朝向，彻底杜绝转动鼠标时动物模型随视角旋转
                ClientAnimationRuntime.LockedOrientation locked = ClientAnimationRuntime.getLockedOrientation(entity.getUUID());
                if (locked != null) {
                    livingState.bodyRot = locked.bodyYaw();
                    livingState.yRot = Mth.wrapDegrees(locked.headYaw() - locked.bodyYaw());
                    livingState.xRot = locked.pitch();
                }
            }
        }
    }
}
