package com.deconstructedcube.non_remorphed_bridge.gametest;

import com.nonid.NeedsOfNature;
import com.nonid.entity.HorseLiquidCollectorEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;

/**
 * GameTests for the Horse Liquid Collector (种马采集器) absorption, storage, and clearing.
 */
public class LiquidCollectorGameTest {

    @GameTest
    public void testHorseLiquidCollector_AbsorptionAndCapacity(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        HorseLiquidCollectorEntity collector = helper.spawn(NeedsOfNature.HORSE_LIQUID_COLLECTOR_ENTITY_TYPE, pos);

        // Initial State
        helper.assertFalse(collector.isCollectorFull(), "Collector must initially be empty");

        // Absorb Horse Liquid
        Identifier horseId = Identifier.fromNamespaceAndPath("minecraft", "horse");
        boolean absorbed = collector.absorbEntityLiquid(horseId);
        helper.assertTrue(absorbed, "Collector must successfully absorb horse liquid");
        helper.assertTrue(collector.isCollectorFull(), "Collector must be full after absorption");
        helper.assertTrue(horseId.equals(collector.getBottleLiquidEntityTypeId()),
                "Stored liquid must match horse entity type");

        // Clearing Collector
        collector.clearStoredLiquid();
        helper.assertFalse(collector.isCollectorFull(), "Collector must be empty after clear");

        helper.succeed();
    }

    @GameTest
    public void testHorseLiquidCollector_MixedLiquidAbsorption(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 2, 1);
        HorseLiquidCollectorEntity collector = helper.spawn(NeedsOfNature.HORSE_LIQUID_COLLECTOR_ENTITY_TYPE, pos);

        // Absorb Donkey Liquid first
        Identifier donkeyId = Identifier.fromNamespaceAndPath("minecraft", "donkey");
        collector.absorbEntityLiquid(donkeyId);
        helper.assertTrue(collector.isCollectorFull(), "Collector must be full after donkey absorption");

        // Absorb Horse Liquid afterwards -> should transition to mixed
        Identifier horseId = Identifier.fromNamespaceAndPath("minecraft", "horse");
        collector.absorbEntityLiquid(horseId);
        helper.assertTrue(collector.isCollectorFull(), "Collector must remain full");
        helper.assertTrue(collector.getBottleLiquidEntityTypeId() == null,
                "Bottle liquid entity type must be null for mixed liquid");

        helper.succeed();
    }
}
