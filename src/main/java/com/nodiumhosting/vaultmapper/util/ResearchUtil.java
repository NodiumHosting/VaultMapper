package com.nodiumhosting.vaultmapper.util;

import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.StageManager;
import iskallia.vault.research.type.Research;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ResearchUtil {
    public static boolean hasResearch(String research) {
        Player player = Minecraft.getInstance().player != null ? Minecraft.getInstance().player : null;
        Research r = ModConfigs.RESEARCHES.getByName(research);
        return r != null && StageManager.getResearchTree(player).isResearched(r);
    }
}
