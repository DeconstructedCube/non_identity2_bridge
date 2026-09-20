package com.deconstructedcube.non_identity2_bridge.mixin.client;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.internal.animation.client.render.AnimationRenderStateAccess;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
            method = "extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN")
    )
    private void non_identity2_bridge$attachMorphIdentity(Entity entity, float tickDelta, CallbackInfoReturnable<EntityRenderState> cir) {
        if (cir.getReturnValue() instanceof AnimationRenderStateAccess access && Identity2ActorHelper.isMorphed(entity)) {
            Identifier morphId = Identity2ActorHelper.getMorphId(entity);
            if (morphId != null) {
                access.afw$setEntityTypeId(morphId);
            }
        }
    }
}
