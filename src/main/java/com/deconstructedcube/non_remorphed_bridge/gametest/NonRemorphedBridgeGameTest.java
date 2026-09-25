package com.deconstructedcube.non_remorphed_bridge.gametest;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import com.nonid.NeedsOfNature;
import com.nonid.entity.HorseLiquidCollectorEntity;
import dev.tocraft.walkers.impl.PlayerDataProvider;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.Set;

/**
 * Automated Headless GameTest Suite for Needs of Nature x ReMorphed Bridge.
 * Runs in pure headless server environment without graphics/display requirements.
 */
public class NonRemorphedBridgeGameTest {

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

        helper.succeed();
    }

    @GameTest
    public void testBabyCheck_AdultVsBaby(GameTestHelper helper) {
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
    public void testHorseLiquidCollector_AbsorptionAndCapacity(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        HorseLiquidCollectorEntity collector = helper.spawn(NeedsOfNature.HORSE_LIQUID_COLLECTOR_ENTITY_TYPE, pos);

        // Initial State
        helper.assertFalse(collector.isCollectorFull(), "Collector must initially be empty");

        // Absorb Horse Liquid
        boolean absorbed = collector.absorbEntityLiquid(Identifier.fromNamespaceAndPath("minecraft", "horse"));
        helper.assertTrue(absorbed, "Collector must successfully absorb horse liquid");
        helper.assertTrue(collector.isCollectorFull(), "Collector must be full after absorption");
        helper.assertTrue(Identifier.fromNamespaceAndPath("minecraft", "horse").equals(collector.getBottleLiquidEntityTypeId()),
                "Stored liquid must match horse entity type");

        // Clearing Collector
        collector.clearStoredLiquid();
        helper.assertFalse(collector.isCollectorFull(), "Collector must be empty after clear");

        helper.succeed();
    }

    @GameTest
    public void testFarmAnimalShapes(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);

        // Sheep
        Sheep sheep = EntityType.SHEEP.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(sheep);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.SHEEP,
                "Sheep morph must be recognized correctly");

        Set<String> sheepTags = RemorphedActorHelper.provideMorphActorTags(mockPlayer);
        helper.assertTrue(sheepTags.contains("actor.sheep"), "Sheep tags must contain 'actor.sheep'");

        helper.succeed();
    }
}
