package com.deconstructedcube.non_identity2_bridge.util;

import com.nonid.GenderHolder;
import net.Gabou.identity2.api.IdentityApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Identity2ActorHelper {

    private record TagCacheKey(EntityType<?> type, int genderMask, boolean isLiving) {
    }

    private static final Map<TagCacheKey, Set<String>> TAG_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> TEXTURE_METHOD_CACHE = new ConcurrentHashMap<>();

    private Identity2ActorHelper() {
    }

    @Nullable
    public static Entity getMorph(@Nullable Entity entity) {
        return entity == null ? null : IdentityApi.getCurrentMorph(entity);
    }

    public static EntityType<?> getEffectiveEntityType(Entity entity) {
        Entity morph = getMorph(entity);
        return morph != null ? morph.getType() : entity.getType();
    }

    public static boolean isEffectiveBaby(LivingEntity living) {
        Entity morph = getMorph(living);
        if (morph instanceof LivingEntity livingMorph) {
            return livingMorph.isBaby();
        }
        return living.isBaby();
    }

    public static Set<String> provideMorphActorTags(Entity entity) {
        Entity morph = getMorph(entity);
        if (morph == null) {
            return Set.of();
        }

        EntityType<?> morphType = morph.getType();
        int mask = (entity instanceof GenderHolder holder) ? (holder.getGenderMask() & 3) : 0;
        boolean isLiving = morph instanceof LivingEntity;
        TagCacheKey key = new TagCacheKey(morphType, mask, isLiving);
        return TAG_CACHE.computeIfAbsent(key, Identity2ActorHelper::buildMorphActorTags);
    }

    private static Set<String> buildMorphActorTags(TagCacheKey key) {
        Identifier morphId = BuiltInRegistries.ENTITY_TYPE.getKey(key.type());
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add("actor.morph");
        tags.add("actor.feral");
        tags.add("actor." + morphId.getPath());
        tags.add("actor." + morphId.getNamespace() + "." + morphId.getPath());

        if (key.isLiving()) {
            tags.add("actor.living");
        }

        // 继承性别标签，以满足动画的角色性别约束
        int mask = key.genderMask();
        if ((mask & 1) != 0) {
            tags.add("gender.male");
        }
        if ((mask & 2) != 0) {
            tags.add("gender.female");
        }
        if (mask == 0) {
            tags.add("gender.male");
        }

        return Set.copyOf(tags);
    }

    /**
     * 提取变身生物的原生材质（完美支持 1.21.11 动物变种与材质包覆盖）。
     */
    @Nullable
    public static Identifier resolveMorphedTexture(@Nullable Entity entity, @Nullable LivingEntityRenderState renderState) {
        if (entity == null) {
            return null;
        }
        Entity morph = getMorph(entity);
        if (morph == null) {
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

        // 1. 优先从已经提取好的动物变种 RenderState（如 WolfRenderState, CatRenderState）中提取
        if (renderState != null && !(renderState instanceof AvatarRenderState)) {
            try {
                EntityRenderer<?, ?> renderer = dispatcher.getRenderer(renderState);
                if (renderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer) {
                    Method method = findTextureLocationMethod(livingRenderer.getClass());
                    if (method != null) {
                        Object result = method.invoke(livingRenderer, renderState);
                        if (result instanceof Identifier textureId && isNonPlayerTexture(textureId)) {
                            return textureId;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        // 2. 次选：直接从活体变身实体实例提取其原生渲染状态与变种贴图（支持 9 种狼、11 种猫、雪狐、16 色羊等变种与材质包）
        if (morph instanceof LivingEntity livingMorph) {
            try {
                EntityRenderer<?, ?> morphRenderer = dispatcher.getRenderer(livingMorph);
                if (morphRenderer instanceof LivingEntityRenderer<?, ?, ?> livingRenderer) {
                    EntityRenderState tempState = extractMorphRenderState(morphRenderer, livingMorph, 0.0f);
                    Method method = findTextureLocationMethod(livingRenderer.getClass());
                    if (method != null) {
                        Object result = method.invoke(livingRenderer, tempState);
                        if (result instanceof Identifier textureId && isNonPlayerTexture(textureId)) {
                            return textureId;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityRenderState extractMorphRenderState(EntityRenderer<T, ?> renderer, Entity entity, float tickProgress) {
        return ((EntityRenderer<T, EntityRenderState>) renderer).createRenderState((T) entity, tickProgress);
    }

    private static boolean isNonPlayerTexture(Identifier id) {
        if (id == null) {
            return false;
        }
        String path = id.getPath();
        return !path.contains("skin") && !path.startsWith("textures/entity/player/");
    }

    @Nullable
    private static Method findTextureLocationMethod(Class<?> rendererClass) {
        return TEXTURE_METHOD_CACHE.computeIfAbsent(rendererClass, cls -> {
            for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
                for (Method m : c.getDeclaredMethods()) {
                    if (Identifier.class.isAssignableFrom(m.getReturnType())
                            && m.getParameterCount() == 1
                            && LivingEntityRenderState.class.isAssignableFrom(m.getParameterTypes()[0])) {
                        m.setAccessible(true);
                        return m;
                    }
                }
            }
            return null;
        });
    }
}
