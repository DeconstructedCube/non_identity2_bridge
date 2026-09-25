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
 * GameTests for Entity Type resolution, 1.21.11 mob variants, and baby filtering.
 */
public class EntityShapeGameTest {

    @GameTest
    public void testEffectiveEntityType_MultipleSpecies(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // 1. Unmorphed (Human)
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.PLAYER,
                "Unmorphed player must evaluate to EntityType.PLAYER");

        // 2. Horse
        Horse horse = EntityType.HORSE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(horse);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.HORSE,
                "Morphed horse player must evaluate to EntityType.HORSE");

        // 3. Wolf
        Wolf wolf = EntityType.WOLF.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(wolf);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.WOLF,
                "Morphed wolf player must evaluate to EntityType.WOLF");

        // 4. Cow
        Cow cow = EntityType.COW.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(cow);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.COW,
                "Morphed cow player must evaluate to EntityType.COW");

        // 5. Pig
        Pig pig = EntityType.PIG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(pig);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.PIG,
                "Morphed pig player must evaluate to EntityType.PIG");

        // 6. Sheep
        Sheep sheep = EntityType.SHEEP.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(sheep);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.SHEEP,
                "Morphed sheep player must evaluate to EntityType.SHEEP");

        helper.succeed();
    }

    @GameTest
    public void testBabyFiltering_PigAndWolf(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // Adult Pig
        Pig adultPig = EntityType.PIG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (adultPig != null) {
            adultPig.setBaby(false);
        }
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(adultPig);
        helper.assertFalse(RemorphedActorHelper.isEffectiveBaby(mockPlayer),
                "Adult pig morph must NOT evaluate to baby");

        // Baby Pig
        Pig babyPig = EntityType.PIG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (babyPig != null) {
            babyPig.setBaby(true);
        }
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(babyPig);
        helper.assertTrue(RemorphedActorHelper.isEffectiveBaby(mockPlayer),
                "Baby pig morph MUST evaluate to baby");

        helper.succeed();
    }

    @GameTest
    public void testVariantMobShapes_CatAndFrog(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // Cat
        Cat cat = EntityType.CAT.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(cat);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.CAT,
                "Cat morph must evaluate to EntityType.CAT");

        // Frog
        Frog frog = EntityType.FROG.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(frog);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.FROG,
                "Frog morph must evaluate to EntityType.FROG");

        helper.succeed();
    }
}
