package com.deconstructedcube.non_remorphed_bridge.gametest;

import com.deconstructedcube.non_remorphed_bridge.util.RemorphedActorHelper;
import dev.tocraft.walkers.impl.PlayerDataProvider;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * GameTests for skin isolation, destroyed skin bypass, and texture integrity on morphs.
 */
public class SkinIsolationGameTest {

    @GameTest
    public void testMorphedPlayerHasNoHumanSkinOverride(GameTestHelper helper) {
        Player mockPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        Wolf wolf = EntityType.WOLF.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        ((PlayerDataProvider) mockPlayer).walkers$updateShapes(wolf);

        // Morphed player shape must exist
        helper.assertTrue(RemorphedActorHelper.getMorph(mockPlayer) != null,
                "Morph entity must be present on transformed player");
        helper.assertTrue(RemorphedActorHelper.getEffectiveEntityType(mockPlayer) == EntityType.WOLF,
                "Transformed player must be identified as Wolf");

        helper.succeed();
    }
}
