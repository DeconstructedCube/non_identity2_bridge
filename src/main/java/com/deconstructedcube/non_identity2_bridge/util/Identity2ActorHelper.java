package com.deconstructedcube.non_identity2_bridge.util;

import com.deconstructedcube.non_identity2_bridge.client.Identity2ClientActorHelper;
import com.nonid.GenderHolder;
import net.Gabou.identity2.api.IdentityApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Identity2ActorHelper {

    public static final Identifier PLAYER_TYPE_ID = Identifier.withDefaultNamespace("player");

    private record TagCacheKey(EntityType<?> type, int genderMask, boolean isLiving) {
    }

    private static final Map<TagCacheKey, Set<String>> TAG_CACHE = new ConcurrentHashMap<>();

    private Identity2ActorHelper() {
    }

    @Nullable
    public static Entity getMorph(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        Entity morph = IdentityApi.getCurrentMorph(entity);
        if (morph != null) {
            return morph;
        }
        Identifier morphId = IdentityApi.getCurrentMorphId(entity);
        if (morphId != null && !PLAYER_TYPE_ID.equals(morphId)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(morphId).orElse(null);
            if (type != null && entity.level() != null) {
                return type.create(entity.level(), net.minecraft.world.entity.EntitySpawnReason.LOAD);
            }
        }
        return null;
    }

    public static EntityType<?> getEffectiveEntityType(Entity entity) {
        Entity morph = getMorph(entity);
        if (morph != null) {
            return morph.getType();
        }
        Identifier morphId = IdentityApi.getCurrentMorphId(entity);
        if (morphId != null && !PLAYER_TYPE_ID.equals(morphId)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(morphId).orElse(null);
            if (type != null) {
                return type;
            }
        }
        return entity.getType();
    }

    public static boolean isEffectiveBaby(LivingEntity living) {
        Entity morph = getMorph(living);
        if (morph instanceof LivingEntity livingMorph) {
            return livingMorph.isBaby();
        }
        return living.isBaby();
    }

    public static Set<String> provideMorphActorTags(Entity entity) {
        EntityType<?> morphType = getEffectiveEntityType(entity);
        if (morphType == EntityType.PLAYER) {
            return Set.of();
        }

        int mask = (entity instanceof GenderHolder holder) ? (holder.getGenderMask() & 3) : 0;
        Entity morph = getMorph(entity);
        boolean isLiving = morph instanceof LivingEntity || entity instanceof LivingEntity;
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
     * 自动通过原版动物渲染器提取变身生物的原生材质。
     * 全自动支持 1.21.11 变种与材质包，零生物硬编码。
     */
    @Nullable
    public static Identifier resolveMorphNativeTexture(Entity morph) {
        if (!(morph instanceof LivingEntity livingMorph)) {
            return null;
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return Identity2ClientActorHelper.resolveMorphNativeTexture(morph, null);
        }

        return Identity2ClientActorHelper.fallbackDefaultMobTexture(livingMorph.getType());
    }

    public static boolean isNonPlayerTexture(Identifier id) {
        if (id == null) {
            return false;
        }
        String path = id.getPath().toLowerCase(java.util.Locale.ROOT);
        return !path.contains("missingno") && !path.contains("skin") && !path.startsWith("textures/entity/player/");
    }
}
