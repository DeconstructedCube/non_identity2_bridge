package com.deconstructedcube.non_identity2_bridge.util;

import com.nonid.GenderHolder;
import net.Gabou.identity2.api.IdentityApi;
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
        return IdentityApi.getCurrentMorph(entity);
    }

    public static EntityType<?> getEffectiveEntityType(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        Entity morph = IdentityApi.getCurrentMorph(entity);
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

    public static boolean isEffectiveBaby(@Nullable LivingEntity living) {
        if (living == null) {
            return false;
        }
        Entity morph = getMorph(living);
        if (morph instanceof LivingEntity livingMorph) {
            return livingMorph.isBaby();
        }
        return living.isBaby();
    }

    public static Set<String> provideMorphActorTags(@Nullable Entity entity) {
        if (entity == null) {
            return Set.of();
        }
        EntityType<?> morphType = getEffectiveEntityType(entity);
        if (morphType == null || morphType == EntityType.PLAYER) {
            return Set.of();
        }

        int mask = (entity instanceof GenderHolder holder) ? (holder.getGenderMask() & 3) : 0;
        if (mask == 0) {
            Set<String> tags = entity.getTags();
            if (tags.contains("gender.male")) {
                mask |= 1;
            }
            if (tags.contains("gender.female")) {
                mask |= 2;
            }
        }

        Entity morph = getMorph(entity);
        boolean isLiving = morph instanceof LivingEntity || entity instanceof LivingEntity;
        TagCacheKey key = new TagCacheKey(morphType, mask, isLiving);
        return TAG_CACHE.computeIfAbsent(key, Identity2ActorHelper::buildMorphActorTags);
    }

    private static Set<String> buildMorphActorTags(TagCacheKey key) {
        Identifier morphId = BuiltInRegistries.ENTITY_TYPE.getKey(key.type());
        if (morphId == null) {
            return Set.of("actor.morph", "actor.feral");
        }
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

        return Set.copyOf(tags);
    }
}
