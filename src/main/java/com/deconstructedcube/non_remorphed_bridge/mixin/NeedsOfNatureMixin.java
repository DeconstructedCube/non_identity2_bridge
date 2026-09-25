package com.deconstructedcube.non_remorphed_bridge.mixin;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import com.nonid.NeedsOfNature;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NeedsOfNature.class)
public abstract class NeedsOfNatureMixin {

    @Redirect(
            method = "resolveLiquidGainProfile(Lnet/minecraft/server/level/ServerLevel;Ljava/util/List;Ljava/util/List;Lnet/minecraft/resources/Identifier;Lcom/nonid/NonLiquidTankType;)Lcom/nonid/NeedsOfNature$LiquidGainProfile;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private static EntityType<?> non_remorphed_bridge$redirectLiquidGainEntityType(Entity entity) {
        return RemorphedActorHelper.getEffectiveEntityType(entity);
    }

    @Redirect(
            method = "fillHorseLiquidCollectorsOnPeak",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private static EntityType<?> non_remorphed_bridge$redirectHorseCollectorEntityType(Entity entity) {
        return RemorphedActorHelper.getEffectiveEntityType(entity);
    }

    @ModifyVariable(
            method = "capturePregnancyVariantData",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static Entity non_remorphed_bridge$redirectCaptureVariantEntity(Entity entity) {
        Entity morph = RemorphedActorHelper.getMorph(entity);
        return morph != null ? morph : entity;
    }
}
