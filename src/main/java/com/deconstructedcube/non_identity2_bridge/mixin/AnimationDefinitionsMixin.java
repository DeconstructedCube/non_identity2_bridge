package com.deconstructedcube.non_identity2_bridge.mixin;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.internal.animation.data.AnimationDefinitions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnimationDefinitions.class)
public abstract class AnimationDefinitionsMixin {

    @Redirect(
            method = "fallbackActorKey",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private static EntityType<?> non_identity2_bridge$redirectFallbackActorKeyEntityType(Entity entity) {
        return Identity2ActorHelper.getEffectiveEntityType(entity);
    }
}
