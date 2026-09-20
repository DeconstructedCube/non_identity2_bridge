package com.deconstructedcube.non_identity2_bridge.util;

import com.nonid.GenderHolder;
import com.nonid.internal.animation.util.EntityVariants;
import net.Gabou.identity2.api.IdentityApi;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public final class Identity2ActorHelper {

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
            String variant = EntityVariants.resolveVariant(morph);
            if (variant != null && !variant.isBlank()) {
                return variant;
            }
        }
        CompoundTag tag = IdentityApi.getCurrentMorphVariant(entity);
        if (tag != null && !tag.isEmpty()) {
            Optional<String> variant = tag.getString("variant");
            if (variant.isPresent() && !variant.get().isBlank()) {
                return variant.get();
            }
            Optional<String> variantCapital = tag.getString("Variant");
            if (variantCapital.isPresent() && !variantCapital.get().isBlank()) {
                return variantCapital.get();
            }
            Optional<String> type = tag.getString("Type");
            if (type.isPresent() && !type.get().isBlank()) {
                return type.get();
            }
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

        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add("actor.morph");
        tags.add("actor.identity2");
        tags.add("actor." + morphId.getPath());
        tags.add("actor." + morphId.getNamespace() + "." + morphId.getPath());

        Entity morph = IdentityApi.getCurrentMorph(entity);
        if (morph != null) {
            tags.add("actor.feral");
            if (morph instanceof LivingEntity) {
                tags.add("actor.living");
            }
            // 复制生物自身的性别/标签
            for (String tag : morph.getTags()) {
                if (tag.startsWith("gender.") || tag.startsWith("actor.")) {
                    tags.add(tag);
                }
            }
        }

        // 如果变身未自带显式性别标签，则继承宿主玩家本身的性别标签
        if (!tags.contains("gender.male") && !tags.contains("gender.female")) {
            if (entity instanceof GenderHolder genderHolder) {
                int mask = genderHolder.getGenderMask();
                if ((mask & 1) != 0) {
                    tags.add("gender.male");
                }
                if ((mask & 2) != 0) {
                    tags.add("gender.female");
                }
            }
            for (String tag : entity.getTags()) {
                if (tag.startsWith("gender.")) {
                    tags.add(tag);
                }
            }
            // 若仍然未设置任何性别，默认提供 male 标签以满足单性别动画约束
            if (!tags.contains("gender.male") && !tags.contains("gender.female")) {
                tags.add("gender.male");
            }
        }

        return Set.copyOf(tags);
    }
}
