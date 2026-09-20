package com.deconstructedcube.non_identity2_bridge.mixin;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.internal.animation.data.AnimationDefinitions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.nonid.mixin.MobEntityEnergyMixin")
public abstract class NonMobEntityEnergyMixin {

    @Redirect(
            method = "non$maybeStartEnergyAnimation",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/nonid/internal/animation/data/AnimationDefinitions;areEnabledGenericMobPairTypesCompatible(Lnet/minecraft/resources/Identifier;ZLnet/minecraft/resources/Identifier;Z)Z"
            )
    )
    private boolean non_identity2_bridge$redirectMobPairCompatibility(
            Identifier selfTypeId,
            boolean selfIsBaby,
            Identifier targetTypeId,
            boolean targetIsBaby
    ) {
        return AnimationDefinitions.areEnabledGenericMobPairTypesCompatible(selfTypeId, selfIsBaby, targetTypeId, targetIsBaby);
    }

    @Redirect(
            method = "non$maybeStartEnergyAnimation",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private net.minecraft.world.entity.EntityType<?> non_identity2_bridge$redirectSearchCandidateEntityType(Entity entity) {
        return Identity2ActorHelper.getEffectiveEntityType(entity);
    }

    @Redirect(
            method = "non$maybeStartEnergyAnimation",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isBaby()Z"
            )
    )
    private boolean non_identity2_bridge$redirectSearchCandidateIsBaby(LivingEntity living) {
        return Identity2ActorHelper.isEffectiveBaby(living);
    }

    @Redirect(
            method = "non$tryPlayerInteraction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private net.minecraft.world.entity.EntityType<?> non_identity2_bridge$redirectTryPlayerEntityType(Entity entity) {
        return Identity2ActorHelper.getEffectiveEntityType(entity);
    }
}
