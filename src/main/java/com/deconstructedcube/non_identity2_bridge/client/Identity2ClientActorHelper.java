package com.deconstructedcube.non_identity2_bridge.client;

import com.nonid.internal.animation.client.runtime.ClientAnimationRuntime;
import com.nonid.internal.animation.network.AnimationStageInfo;
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

import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class Identity2ClientActorHelper {

    private Identity2ClientActorHelper() {
    }

    /**
     * 极速、零反射提取变身实体原生贴图：
     * 1. 优先复用 NoN 传入的已就绪 RenderState，避免多余对象分配。
     * 2. 利用泛型擦除直接 invokevirtual 触发 getTextureLocation，彻底告别反射开销。
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

        try {
            EntityRenderer<?, ?> renderer = dispatcher.getRenderer(livingMorph);
            if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer && !(renderer instanceof AvatarRenderer)) {
                // 如果传入的 renderState 已经是变身形态的状态对象（非人类皮肤状态），直接复用，0 分配！
                if (currentRenderState != null && !(currentRenderState instanceof AvatarRenderState)
                        && livingRenderer.createRenderState().getClass().isInstance(currentRenderState)) {
                    return ((LivingEntityRenderer) livingRenderer).getTextureLocation(currentRenderState);
                }

                // 极端情况下的兜底单次生成
                EntityRenderState tempState = ((EntityRenderer) livingRenderer).createRenderState(livingMorph, 0.0f);
                if (tempState instanceof LivingEntityRenderState livingTempState) {
                    return ((LivingEntityRenderer) livingRenderer).getTextureLocation(livingTempState);
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    public static double resolveAuthoredTimelineSeconds(@Nullable UUID instanceId, float tickDelta) {
        if (instanceId == null) {
            return 0.0;
        }
        ClientAnimationRuntime.StageTimelineSnapshot timeline = ClientAnimationRuntime.findStageTimeline(instanceId);
        if (timeline == null) {
            return 0.0;
        }
        AnimationStageInfo stage = ClientAnimationRuntime.findCurrentStage(instanceId);
        Minecraft client = Minecraft.getInstance();
        long now = (client != null && client.level != null) ? client.level.getGameTime() : 0;
        double speed = timeline.speed() > 0 ? timeline.speed() : 1.0;
        double elapsedTicks = Math.max(0.0, ((now + Math.max(0.0f, tickDelta)) - timeline.stageStartTick()) * speed);
        if (stage != null && stage.loop()) {
            double cycleTicks = stage.cycleTicks() > 0 ? stage.cycleTicks() : (double) stage.lengthTicks();
            if (cycleTicks > 0.0) {
                elapsedTicks = elapsedTicks % cycleTicks;
            }
        }
        return elapsedTicks / 20.0;
    }

    @Nullable
    public static String conjoinedStageKey(@Nullable String animationPath) {
        if (animationPath == null || animationPath.isEmpty()) {
            return null;
        }
        int marker = animationPath.lastIndexOf(".p");
        if (marker >= 0 && marker + 1 < animationPath.length()) {
            return animationPath.substring(marker + 1);
        }
        int slash = animationPath.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < animationPath.length()) {
            return animationPath.substring(slash + 1);
        }
        return null;
    }
}
