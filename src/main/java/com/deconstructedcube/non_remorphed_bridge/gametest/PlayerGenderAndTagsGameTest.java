package com.deconstructedcube.non_remorphed_bridge.gametest;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import dev.tocraft.walkers.impl.PlayerDataProvider;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.Set;

/**
 * GameTests focused on Player actor tags, caching consistency, and gender traits.
 */
public class PlayerGenderAndTagsGameTest {

    @GameTest
    public void testPlayerMorphTags_SpeciesNamespaces(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // Canine Tags
        Wolf wolf = EntityType.WOLF.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(wolf);

        Set<String> wolfTags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(wolfTags.contains("actor.morph"), "Must contain 'actor.morph'");
        helper.assertTrue(wolfTags.contains("actor.feral"), "Must contain 'actor.feral'");
        helper.assertTrue(wolfTags.contains("actor.wolf"), "Must contain 'actor.wolf'");
        helper.assertTrue(wolfTags.contains("actor.minecraft.wolf"), "Must contain 'actor.minecraft.wolf'");
        helper.assertTrue(wolfTags.contains("actor.living"), "Must contain 'actor.living'");

        // Equine Tags
        Horse horse = EntityType.HORSE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(horse);

        Set<String> horseTags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(horseTags.contains("actor.morph"), "Must contain 'actor.morph'");
        helper.assertTrue(horseTags.contains("actor.horse"), "Must contain 'actor.horse'");
        helper.assertTrue(horseTags.contains("actor.minecraft.horse"), "Must contain 'actor.minecraft.horse'");

        // Bovine Tags
        Cow cow = EntityType.COW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(cow);

        Set<String> cowTags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(cowTags.contains("actor.cow"), "Must contain 'actor.cow'");
        helper.assertTrue(cowTags.contains("actor.minecraft.cow"), "Must contain 'actor.minecraft.cow'");

        // Porcine Tags
        Pig pig = EntityType.PIG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(pig);

        Set<String> pigTags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(pigTags.contains("actor.pig"), "Must contain 'actor.pig'");
        helper.assertTrue(pigTags.contains("actor.minecraft.pig"), "Must contain 'actor.minecraft.pig'");

        helper.succeed();
    }

    @GameTest
    public void testPlayerTagCachingConsistency(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        Wolf wolf = EntityType.WOLF.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(wolf);

        Set<String> tagsFirst = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        Set<String> tagsSecond = RemorphedActorHelper.provideMorphActorTags(mockPlayer);

        // Must return the exact cached Set instance without allocation
        helper.assertTrue(tagsFirst == tagsSecond, "Repeated tag queries must return cached Set instance");

        helper.succeed();
    }

    @GameTest
    public void testUnmorphedPlayerReturnsEmptyTags(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        Set<String> tags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(tags.isEmpty(), "Unmorphed player must yield empty morph actor tags");

        helper.succeed();
    }
}
