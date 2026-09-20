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
import java.util.Set;

public final class Identity2ActorHelper {

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

        Identifier morphId = BuiltInRegistries.ENTITY_TYPE.getKey(morph.getType());
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add("actor.morph");
        tags.add("actor.feral");
        tags.add("actor." + morphId.getPath());
        tags.add("actor." + morphId.getNamespace() + "." + morphId.getPath());

        if (morph instanceof LivingEntity) {
            tags.add("actor.living");
        }

        // 继承性别标签，以满足动画的角色性别约束
        int mask = (entity instanceof GenderHolder holder) ? (holder.getGenderMask() & 3) : 0;
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
