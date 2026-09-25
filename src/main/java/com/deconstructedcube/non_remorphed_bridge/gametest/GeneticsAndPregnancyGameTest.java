package com.deconstructedcube.non_remorphed_bridge.gametest;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import dev.tocraft.walkers.impl.PlayerDataProvider;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * GameTests for genetics, variant inheritance, and equine morph donor data.
 */
public class GeneticsAndPregnancyGameTest {

    @GameTest
    public void testEquineCrossSpeciesGenetics(GameTestHelper helper) {
        Player mockStallion = helper.makeMockPlayer(GameType.SURVIVAL);
        Player mockMare = helper.makeMockPlayer(GameType.SURVIVAL);

        // Stallion (Donkey)
        Donkey donkey = EntityType.DONKEY.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockStallion).walkers$updateShapes(donkey);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockStallion) == EntityType.DONKEY,
                "Sire must be identified as Donkey");

        // Mare (Horse)
        Horse horse = EntityType.HORSE.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockMare).walkers$updateShapes(horse);
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockMare) == EntityType.HORSE,
                "Dam must be identified as Horse");

        helper.succeed();
    }
}
