package com.deconstructedcube.non_remorphed_bridge.util;

import com.deconstructedcube.non_remorphed_bridge.client.RemorphedClientActorHelper;
import com.nonid.GenderHolder;
import dev.tocraft.walkers.api.PlayerShape;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RemorphedActorHelper {

    private record TagCacheKey(EntityType<?> type, int genderMask, boolean isLiving) {
    }

    private static final Map<TagCacheKey, Set<String>> TAG_CACHE = new ConcurrentHashMap<>();

    private RemorphedActorHelper() {
    }

    @Nullable
    public static Entity getMorph(@Nullable Entity entity) {
        if (entity instanceof Player player) {
            return PlayerShape.getCurrentShape(player);
        }
        return null;
    }

    @Nullable
    public static EntityType<?> getEffectiveEntityType(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        Entity morph = getMorph(entity);
        if (morph != null) {
            return morph.getType();
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
    public static Set<String> provideMorphActorTags(Entity entity) {
        if (entity == null) {
            return Set.of();
        }
        EntityType<?> morphType = getEffectiveEntityType(entity);
        if (morphType == null || morphType == EntityType.PLAYER) {
            return Set.of();
        }

        int mask = (entity instanceof GenderHolder holder) ? (holder.getGenderMask() & 3) : 0;
        Entity morph = getMorph(entity);
        boolean isLiving = morph instanceof LivingEntity || entity instanceof LivingEntity;
        TagCacheKey key = new TagCacheKey(morphType, mask, isLiving);
        return TAG_CACHE.computeIfAbsent(key, RemorphedActorHelper::buildMorphActorTags);
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

    @Nullable
    public static Identifier resolveMorphNativeTexture(Entity morph) {
        if (!(morph instanceof LivingEntity)) {
            return null;
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return RemorphedClientActorHelper.resolveMorphNativeTexture(morph, null);
        }

        return null;
    }
}
