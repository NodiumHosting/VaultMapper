package com.nodiumhosting.vaultmapper.map;

import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;

import java.util.*;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMapOverlayRenderer {
    public static boolean enabled = false;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Pre event) {
        if (!enabled) return;

        int w = event.getWindow().getGuiScaledWidth();
        int h = event.getWindow().getGuiScaledHeight();

        int mapWidth = (int) (w * 0.2f);
        int mapHeight = (int) (w * 0.2f);
        int mapStartX = w - mapWidth;
        int mapStartY = h - mapHeight;

        int mapRoomWidth = mapWidth / 29;

        HashMap<Integer, Integer> roomColors = new HashMap<>();
        roomColors.put(0, 0xFF000000); // void
        roomColors.put(1, 0xFF0000FF); // tunnelX
        roomColors.put(2, 0xFF0000FF); // tunnelZ
        roomColors.put(3, 0xFF0000FF); // room
        roomColors.put(4, 0xFFFF0000); // start
        roomColors.put(5, 0xFF00FF00); // current

        int importantRoomColor = 0xFFFF00FF;

        //draw map grid
        for (int x = -14; x <= 14; x++) {
            for (int z = -14; z <= 14; z++) {
                int mapX = mapStartX + (x + 14) * mapRoomWidth;
                int mapZ = mapStartY + (z + 14) * mapRoomWidth;

                int[] roomData = VaultMap.mapData[x + 14][z + 14];

                int roomType = roomData[0];
                int roomSize = roomData[1];
                int roomVisited = roomData[2];

                if (roomVisited == 1 || roomVisited == 2) GuiComponent.fill(event.getMatrixStack(), mapX - roomSize, mapZ - roomSize, mapX + roomSize, mapZ + roomSize, roomColors.get(roomType));

                if (roomVisited == 2 || roomVisited == 3) GuiComponent.fill(event.getMatrixStack(), mapX - 1, mapZ - 1, mapX + 1, mapZ + 1, importantRoomColor);
            }
        }
    }
}