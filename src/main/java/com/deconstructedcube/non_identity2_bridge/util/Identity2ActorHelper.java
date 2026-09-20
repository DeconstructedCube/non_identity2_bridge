package com.deconstructedcube.non_identity2_bridge.util;

import com.nonid.GenderHolder;
import com.nonid.internal.animation.util.EntityVariants;
import net.Gabou.identity2.api.IdentityApi;
import net.minecraft.core.Holder;
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

    private static final Map<TagCacheKey, Set<String>> TAG_CACHE = new ConcurrentHashMap<>();

    private record TagCacheKey(Identifier morphId, int genderMask, boolean isLiving) {
    }

    private Identity2ActorHelper() {
    }

    public static boolean isMorphed(@Nullable Entity entity) {
        return entity != null && IdentityApi.isMorphed(entity);
    }

    @Nullable
    public static Entity getMorphEntity(Entity entity) {
        return IdentityApi.getCurrentMorph(entity);
    }

    @Nullable
    public static EntityType<?> getMorphEntityType(Entity entity) {
        Entity morph = IdentityApi.getCurrentMorph(entity);
        if (morph != null) {
            return morph.getType();
        }
        Identifier id = IdentityApi.getCurrentMorphId(entity);
        if (id != null) {
            return BuiltInRegistries.ENTITY_TYPE.get(id).map(Holder::value).orElse(null);
        }
        return null;
    }

    public static EntityType<?> getEffectiveEntityType(Entity entity) {
        if (entity != null && IdentityApi.isMorphed(entity)) {
            EntityType<?> morphType = getMorphEntityType(entity);
            if (morphType != null) {
                return morphType;
            }
        }
        return entity == null ? null : entity.getType();
    }

    @Nullable
    public static Identifier getMorphId(Entity entity) {
        return IdentityApi.getCurrentMorphId(entity);
    }

    public static boolean isMorphBaby(LivingEntity entity) {
        Entity morph = IdentityApi.getCurrentMorph(entity);
        if (morph instanceof LivingEntity livingMorph) {
            return livingMorph.isBaby();
        }
        return entity.isBaby();
    }

    public static boolean isEffectiveBaby(LivingEntity entity) {
        if (entity != null && IdentityApi.isMorphed(entity)) {
            return isMorphBaby(entity);
        }
        return entity != null && entity.isBaby();
    }

    @Nullable
    public static String getMorphVariant(Entity entity) {
        Entity morph = IdentityApi.getCurrentMorph(entity);
        if (morph != null) {
            return EntityVariants.resolveVariant(morph);
        }
        return null;
    }

    public static Set<String> provideMorphActorTags(Entity entity) {
        if (!IdentityApi.isMorphed(entity)) {
            return Set.of();
        }
        Identifier morphId = IdentityApi.getCurrentMorphId(entity);
        if (morphId == null) {
            return Set.of();
        }

        Entity morph = IdentityApi.getCurrentMorph(entity);
        boolean isLiving = morph instanceof LivingEntity;

        int mask = (entity instanceof GenderHolder holder) ? (holder.getGenderMask() & 3) : 0;
        if (mask == 0 && morph instanceof GenderHolder morphHolder) {
            mask = morphHolder.getGenderMask() & 3;
        }

        TagCacheKey key = new TagCacheKey(morphId, mask, isLiving);
        return TAG_CACHE.computeIfAbsent(key, Identity2ActorHelper::buildMorphTags);
    }

    private static Set<String> buildMorphTags(TagCacheKey key) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        Identifier morphId = key.morphId();

        tags.add("actor.morph");
        tags.add("actor.identity2");
        tags.add("actor." + morphId.getPath());
        tags.add("actor." + morphId.getNamespace() + "." + morphId.getPath());
        tags.add("actor.feral");

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
}
