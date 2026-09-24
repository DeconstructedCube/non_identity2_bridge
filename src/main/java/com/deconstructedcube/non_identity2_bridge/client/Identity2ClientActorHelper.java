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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
     * 2. 利用泛型擦除直接 invokevirtual 触发 getTextureLocation。
     * 3. 若返回 missingno 或提取失败，使用原版标准实体贴图兜底，彻底防止触发 NoN 的红色错误材质。
     */
    @Nullable
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Identifier resolveMorphNativeTexture(Entity morph, @Nullable LivingEntityRenderState currentRenderState) {
        if (!(morph instanceof LivingEntity livingMorph)) {
            return null;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return fallbackDefaultMobTexture(livingMorph.getType());
        }
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        if (dispatcher == null) {
            return fallbackDefaultMobTexture(livingMorph.getType());
        }

        try {
            EntityRenderer<?, ?> renderer = dispatcher.getRenderer(livingMorph);
            if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer && !(renderer instanceof AvatarRenderer)) {
                if (currentRenderState != null && !(currentRenderState instanceof AvatarRenderState)
                        && livingRenderer.createRenderState().getClass().isInstance(currentRenderState)) {
                    Identifier id = ((LivingEntityRenderer) livingRenderer).getTextureLocation(currentRenderState);
                    if (isValidMobTexture(id)) {
                        return id;
                    }
                }

                EntityRenderState tempState = ((EntityRenderer) livingRenderer).createRenderState(livingMorph, 0.0f);
                if (tempState instanceof LivingEntityRenderState livingTempState) {
                    Identifier id = ((LivingEntityRenderer) livingRenderer).getTextureLocation(livingTempState);
                    if (isValidMobTexture(id)) {
                        return id;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return fallbackDefaultMobTexture(livingMorph.getType());
    }

    public static boolean isValidMobTexture(@Nullable Identifier id) {
        if (id == null) {
            return false;
        }
        String path = id.getPath().toLowerCase(java.util.Locale.ROOT);
        return !path.contains("missingno") && !path.contains("skin") && !path.startsWith("textures/entity/player/");
    }

    @Nullable
    public static Identifier fallbackDefaultMobTexture(EntityType<?> type) {
        if (type == null) {
            return null;
        }
        Identifier typeId = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (typeId == null) {
            return null;
        }
        String path = typeId.getPath();
        if ("pig".equals(path)) {
            return Identifier.withDefaultNamespace("textures/entity/pig/temperate_pig.png");
        }
        if ("cow".equals(path)) {
            return Identifier.withDefaultNamespace("textures/entity/cow/temperate_cow.png");
        }
        if ("chicken".equals(path)) {
            return Identifier.withDefaultNamespace("textures/entity/chicken/temperate_chicken.png");
        }
        if ("sheep".equals(path)) {
            return Identifier.withDefaultNamespace("textures/entity/sheep/sheep.png");
        }
        if ("wolf".equals(path)) {
            return Identifier.withDefaultNamespace("textures/entity/wolf/wolf.png");
        }
        if ("cat".equals(path)) {
            return Identifier.withDefaultNamespace("textures/entity/cat/tabby.png");
        }
        return Identifier.withDefaultNamespace("textures/entity/" + path + "/" + path + ".png");
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
