package com.deconstructedcube.non_identity2_bridge.mixin;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.internal.animation.util.EntityVariants;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityVariants.class)
public abstract class EntityVariantsMixin {

    @Inject(
            method = "resolveVariant(Lnet/minecraft/world/entity/Entity;)Ljava/lang/String;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void non_identity2_bridge$resolveMorphVariant(Entity entity, CallbackInfoReturnable<String> cir) {
        if (Identity2ActorHelper.isMorphed(entity)) {
            String variant = Identity2ActorHelper.getMorphVariant(entity);
            if (variant != null && !variant.isBlank()) {
                cir.setReturnValue(variant);
            }
        }
    }
}
