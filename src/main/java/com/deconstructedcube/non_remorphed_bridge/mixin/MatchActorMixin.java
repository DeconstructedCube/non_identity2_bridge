package com.deconstructedcube.non_remorphed_bridge.mixin;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import com.nonid.internal.animation.util.EntityVariants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.nonid.internal.animation.data.AnimationDefinitions$MatchActor")
public abstract class MatchActorMixin {

    @Redirect(
            method = "from",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private static EntityType<?> non_remorphed_bridge$redirectMatchActorEntityType(Entity entity) {
        return RemorphedActorHelper.getEffectiveEntityType(entity);
    }

    @Redirect(
            method = "from",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isBaby()Z"
            )
    )
    private static boolean non_remorphed_bridge$redirectMatchActorIsBaby(LivingEntity living) {
        return RemorphedActorHelper.isEffectiveBaby(living);
    }

    @Redirect(
            method = "from",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/nonid/internal/animation/util/EntityVariants;resolveVariant(Lnet/minecraft/world/entity/Entity;)Ljava/lang/String;"
            )
    )
    private static String non_remorphed_bridge$redirectMatchActorVariant(Entity entity) {
        Entity morph = RemorphedActorHelper.getMorph(entity);
        return EntityVariants.resolveVariant(morph != null ? morph : entity);
    }
}
