package com.deconstructedcube.non_remorphed_bridge.gametest;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import dev.tocraft.walkers.impl.PlayerDataProvider;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * GameTests focused on the Player entity lifecycle and shape transformations.
 */
public class PlayerMorphGameTest {

    @GameTest
    public void testPlayerMorphLifecycle(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // 1. Initial State: Human
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.PLAYER,
                "Player must initially have EntityType.PLAYER");
        helper.assertTrue(RemorphedActorHelper.getMorph(mockPlayer) == null,
                "Unmorphed player must have null morph");

        // 2. Transform into Wolf
        Wolf wolf = EntityType.WOLF.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(wolf);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.WOLF,
                "Morphed player must resolve to EntityType.WOLF");
        helper.assertTrue(RemorphedActorHelper.getMorph(mockPlayer) == wolf,
                "Morph instance must match the assigned wolf");

        // 3. Switch to Horse
        Horse horse = EntityType.HORSE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(horse);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.HORSE,
                "Morphed player must switch to EntityType.HORSE");

        // 4. Revert back to Human
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(null);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.PLAYER,
                "Unmorphed player must return to EntityType.PLAYER");
        helper.assertTrue(RemorphedActorHelper.getMorph(mockPlayer) == null,
                "Reverted player must have null morph");

        helper.succeed();
    }

    @GameTest
    public void testPlayerBabyMorphStates(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // Adult Pig Morph
        Pig adultPig = EntityType.PIG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (adultPig != null) {
            adultPig.setBaby(false);
        }
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(adultPig);
        helper.assertFalse(RemorphedActorHelper.isEffectiveBaby(mockPlayer),
                "Adult pig morph on player must NOT be baby");

        // Baby Pig Morph
        Pig babyPig = EntityType.PIG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (babyPig != null) {
            babyPig.setBaby(true);
        }
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(babyPig);
        helper.assertTrue(RemorphedActorHelper.isEffectiveBaby(mockPlayer),
                "Baby pig morph on player MUST be baby");

        helper.succeed();
    }

    @GameTest
    public void testPlayerFarmAndVariantMorphs(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // Cow
        Cow cow = EntityType.COW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(cow);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.COW,
                "Player morphed as cow must evaluate to EntityType.COW");

        // Sheep
        Sheep sheep = EntityType.SHEEP.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(sheep);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.SHEEP,
                "Player morphed as sheep must evaluate to EntityType.SHEEP");

        // Cat
        Cat cat = EntityType.CAT.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(cat);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.CAT,
                "Player morphed as cat must evaluate to EntityType.CAT");

        // Frog
        Frog frog = EntityType.FROG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(frog);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.FROG,
                "Player morphed as frog must evaluate to EntityType.FROG");

        helper.succeed();
    }
}
