package com.nodiumhosting.vaultmapper.map;

import net.minecraft.client.Minecraft;
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

    public static HashMap<Integer, Integer> roomColors = new HashMap<>();

    static int importantRoomColor = 0xFFFF00FF;

    static int mapStartX;
    static int mapStartY;
    static int mapRoomWidth;



    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Pre event) {
        if (!enabled) return;

        //draw map grid
        for (int x = -24; x <= 24; x++) {
            for (int z = -24; z <= 24; z++) {
                int mapX = mapStartX + (x + 24) * mapRoomWidth;
                int mapZ = mapStartY + (z + 24) * mapRoomWidth;

                int[] roomData = VaultMap.mapData[x + 24][z + 24];

                int roomType = roomData[0];
                int roomSize = roomData[1];
                int roomVisited = roomData[2];

                if (roomVisited == 1 || roomVisited == 2) GuiComponent.fill(event.getMatrixStack(), mapX - roomSize, mapZ - roomSize, mapX + roomSize, mapZ + roomSize, roomColors.get(roomType));

                if (roomVisited == 2 || roomVisited == 3) GuiComponent.fill(event.getMatrixStack(), mapX - 1, mapZ - 1, mapX + 1, mapZ + 1, importantRoomColor);
            }
        }
    }

    public static void onWindowResize(long window, int width, int height) {
        int mapWidth = (int) (width * 0.25f);
        int mapHeight = (int) (width * 0.25f); // this seems weird but it was similar in original implementation
        mapStartX = width - mapWidth;
        mapStartY = height - mapHeight;

        mapRoomWidth = mapWidth / 49;
    };

    public static void prep() {
        roomColors.put(0, 0xFF000000); // void
        roomColors.put(1, 0xFF0000FF); // tunnelX
        roomColors.put(2, 0xFF0000FF); // tunnelZ
        roomColors.put(3, 0xFF0000FF); // room
        roomColors.put(4, 0xFFFF0000); // start
        roomColors.put(5, 0xFF00FF00); // current
        roomColors.put(6, 0xFFFFD700); // inscription


        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int mapWidth = (int) (w * 0.25f);
        int mapHeight = (int) (w * 0.25f);
        mapStartX = w - mapWidth;
        mapStartY = h - mapHeight;

        mapRoomWidth = mapWidth / 49;
    }
}