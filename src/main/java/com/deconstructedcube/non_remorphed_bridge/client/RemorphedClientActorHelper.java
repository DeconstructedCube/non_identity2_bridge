package com.deconstructedcube.non_remorphed_bridge.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class RemorphedClientActorHelper {

    private RemorphedClientActorHelper() {
    }

    /**
     * 极速、真实提取变身实体原生贴图：
     * 1. 优先复用 NoN 传入的已就绪 RenderState，避免多余对象分配。
     * 2. 利用泛型擦除直接调用原版 LivingEntityRenderer 的 getTextureLocation。
     * 3. 绝不使用静态假贴图兜底，杜绝假阴性并忠实反映 1.21.11 变种与高清材质包。
     */
    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Identifier resolveMorphNativeTexture(Entity morph, @Nullable LivingEntityRenderState currentRenderState) {
        if (!(morph instanceof LivingEntity livingMorph)) {
            return null;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return null;
        }
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        if (dispatcher == null) {
            return null;
        }

        EntityRenderer<?, ?> renderer = dispatcher.getRenderer(livingMorph);
        if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer && !(renderer instanceof AvatarRenderer)) {
            if (currentRenderState != null && !(currentRenderState instanceof AvatarRenderState)
                    && livingRenderer.createRenderState().getClass().isInstance(currentRenderState)) {
                return ((LivingEntityRenderer) livingRenderer).getTextureLocation(currentRenderState);
            }

            EntityRenderState tempState = ((EntityRenderer) livingRenderer).createRenderState(livingMorph, 0.0f);
            if (tempState instanceof LivingEntityRenderState livingTempState) {
                return ((LivingEntityRenderer) livingRenderer).getTextureLocation(livingTempState);
            }
        }

        return null;
    }
}
