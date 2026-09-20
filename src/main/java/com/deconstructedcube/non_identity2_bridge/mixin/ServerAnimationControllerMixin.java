package com.deconstructedcube.non_identity2_bridge.mixin;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.internal.animation.server.ServerAnimationController;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerAnimationController.class)
public abstract class ServerAnimationControllerMixin {

    @Redirect(
            method = "resolveModelRootsByActor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getType()Lnet/minecraft/world/entity/EntityType;"
            )
    )
    private static EntityType<?> non_identity2_bridge$redirectResolveModelRootsEntityType(Entity actor) {
        if (actor != null && Identity2ActorHelper.isMorphed(actor)) {
            EntityType<?> morphType = Identity2ActorHelper.getMorphEntityType(actor);
            if (morphType != null) {
                return morphType;
            }
        }
        return actor == null ? null : actor.getType();
    }
}
