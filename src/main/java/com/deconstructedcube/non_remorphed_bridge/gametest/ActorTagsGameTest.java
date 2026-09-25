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
 * GameTests for dynamic Actor Tag generation, caching, and gender masking.
 */
public class ActorTagsGameTest {

    @GameTest
    public void testMorphActorTags_Canine(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        Wolf wolf = EntityType.WOLF.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(wolf);

        Set<String> tags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(tags.contains("actor.morph"), "Tags must contain 'actor.morph'");
        helper.assertTrue(tags.contains("actor.feral"), "Tags must contain 'actor.feral'");
        helper.assertTrue(tags.contains("actor.wolf"), "Tags must contain 'actor.wolf'");
        helper.assertTrue(tags.contains("actor.minecraft.wolf"), "Tags must contain 'actor.minecraft.wolf'");
        helper.assertTrue(tags.contains("actor.living"), "Tags must contain 'actor.living'");

        helper.succeed();
    }

    @GameTest
    public void testMorphActorTags_Equine(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        Horse horse = EntityType.HORSE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(horse);

        Set<String> tags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(tags.contains("actor.morph"), "Tags must contain 'actor.morph'");
        helper.assertTrue(tags.contains("actor.feral"), "Tags must contain 'actor.feral'");
        helper.assertTrue(tags.contains("actor.horse"), "Tags must contain 'actor.horse'");
        helper.assertTrue(tags.contains("actor.minecraft.horse"), "Tags must contain 'actor.minecraft.horse'");

        helper.succeed();
    }

    @GameTest
    public void testMorphActorTags_BovineAndPorcine(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // Cow
        Cow cow = EntityType.COW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(cow);
        Set<String> cowTags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(cowTags.contains("actor.cow"), "Cow tags must contain 'actor.cow'");
        helper.assertTrue(cowTags.contains("actor.minecraft.cow"), "Cow tags must contain 'actor.minecraft.cow'");

        // Pig
        Pig pig = EntityType.PIG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(pig);
        Set<String> pigTags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(pigTags.contains("actor.pig"), "Pig tags must contain 'actor.pig'");
        helper.assertTrue(pigTags.contains("actor.minecraft.pig"), "Pig tags must contain 'actor.minecraft.pig'");

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
