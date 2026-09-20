package com.deconstructedcube.non_identity2_bridge.mixin.client;

import com.deconstructedcube.non_identity2_bridge.util.Identity2ActorHelper;
import com.nonid.internal.animation.client.render.gecko.GeckoReplacedRender;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GeckoReplacedRender.class)
public abstract class GeckoReplacedRenderMixin {

    @Redirect(
            method = "resolveVanillaTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSkin()Lnet/minecraft/world/entity/player/PlayerSkin;"
            )
    )
    private static PlayerSkin non_identity2_bridge$suppressPlayerSkinForMorphedAnimal(AbstractClientPlayer player) {
        if (Identity2ActorHelper.getMorph(player) != null) {
            return null;
        }
        return player.getSkin();
    }
}
