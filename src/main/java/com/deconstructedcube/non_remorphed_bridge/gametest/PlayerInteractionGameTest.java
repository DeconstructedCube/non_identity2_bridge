package com.deconstructedcube.non_remorphed_bridge.gametest;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import dev.tocraft.walkers.impl.PlayerDataProvider;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.Set;

/**
 * GameTests focused on Multi-Player (Player A + Player B) interaction scenarios.
 */
public class PlayerInteractionGameTest {

    @GameTest
    public void testDualPlayer_MorphedStallionAndHumanFemale(GameTestHelper helper) {
        Player playerA = helper.makeMockPlayer(GameType.SURVIVAL); // Morphed Stallion
        Player playerB = helper.makeMockPlayer(GameType.SURVIVAL); // Human Female Receiver

        // Player A morphs into Horse
        Horse horse = EntityType.HORSE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) playerA).walkers$updateShapes(horse);

        // Assert Player A role
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(playerA) == EntityType.HORSE,
                "Player A must be recognized as Horse");
        Set<String> tagsA = RemorphedActorHelper.provideMorphActorTags(playerA);
        helper.assertTrue(tagsA.contains("actor.horse"), "Player A must have 'actor.horse' tag");

        // Assert Player B remains unmodified Human
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(playerB) == EntityType.PLAYER,
                "Player B must remain EntityType.PLAYER");
        Set<String> tagsB = RemorphedActorHelper.provideMorphActorTags(playerB);
        helper.assertTrue(tagsB.isEmpty(), "Player B must have no morph tags");

        helper.succeed();
    }

    @GameTest
    public void testDualPlayer_SameSpeciesCanine(GameTestHelper helper) {
        Player playerA = helper.makeMockPlayer(GameType.SURVIVAL);
        Player playerB = helper.makeMockPlayer(GameType.SURVIVAL);

        // Both players morph into Wolves
        Wolf wolfA = EntityType.WOLF.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        Wolf wolfB = EntityType.WOLF.create(helper.getLevel(), EntitySpawnReason.COMMAND);

        ((PlayerDataProvider) playerA).walkers$updateShapes(wolfA);
        ((PlayerDataProvider) playerB).walkers$updateShapes(wolfB);

        // Assert both players resolve to WOLF
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(playerA) == EntityType.WOLF,
                "Player A must resolve to EntityType.WOLF");
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(playerB) == EntityType.WOLF,
                "Player B must resolve to EntityType.WOLF");

        // Assert morph instances are distinct per player
        helper.assertTrue(RemorphedActorHelper.getMorph(playerA) == wolfA,
                "Player A morph must point to wolfA instance");
        helper.assertTrue(RemorphedActorHelper.getMorph(playerB) == wolfB,
                "Player B morph must point to wolfB instance");

        helper.succeed();
    }

    @GameTest
    public void testDualPlayer_CrossSpeciesEquineHybrid(GameTestHelper helper) {
        Player playerSire = helper.makeMockPlayer(GameType.SURVIVAL); // Donkey
        Player playerDam = helper.makeMockPlayer(GameType.SURVIVAL);  // Horse

        Donkey donkey = EntityType.DONKEY.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        Horse horse = EntityType.HORSE.create(helper.getLevel(), EntitySpawnReason.COMMAND);

        ((PlayerDataProvider) playerSire).walkers$updateShapes(donkey);
        ((PlayerDataProvider) playerDam).walkers$updateShapes(horse);

        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(playerSire) == EntityType.DONKEY,
                "Sire must be recognized as Donkey");
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(playerDam) == EntityType.HORSE,
                "Dam must be recognized as Horse");

        helper.succeed();
    }
}
