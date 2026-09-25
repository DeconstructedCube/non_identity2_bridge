package com.deconstructedcube.non_woodwalkers_bridge.client;

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
public final class WoodwalkersClientActorHelper {

    private WoodwalkersClientActorHelper() {
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
}
