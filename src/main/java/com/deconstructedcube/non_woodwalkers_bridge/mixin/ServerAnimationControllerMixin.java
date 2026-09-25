package com.deconstructedcube.non_woodwalkers_bridge.mixin;

import com.deconstructedcube.non_woodwalkers_bridge.util.WoodwalkersActorHelper;
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
    private static EntityType<?> non_woodwalkers_bridge$redirectResolveModelRootsEntityType(Entity actor) {
        return WoodwalkersActorHelper.getEffectiveEntityType(actor);
    }
}
