package com.deconstructedcube.non_identity2_bridge.mixin.client;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.nonid.internal.animation.mixin.client.EntityRenderManagerMixin")
public abstract class NoNEntityRenderManagerMixin {

    @Redirect(
            method = "afw$attachIdentity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private EntityType<?> non_identity2_bridge$redirectNoNRenderStateEntityType(Entity entity) {
        return Identity2ActorHelper.getEffectiveEntityType(entity);
    }
}
