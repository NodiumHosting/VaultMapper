package com.nodiumhosting.vaultmapper.map;

import com.nodiumhosting.vaultmapper.VaultMapper;
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

    static boolean prepped = false;



    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Pre event) {
        if (!enabled) return;
        if (!prepped) prep();

        //draw map grid
        for (int x = 0; x <= 48; x++) {
            for (int z = 0; z <= 48; z++) {
                int mapX = mapStartX + (x) * mapRoomWidth;
                int mapZ = mapStartY + (z) * mapRoomWidth;

                int[] roomData = VaultMap.mapData[x][z];

                int roomType = roomData[0];
                int roomSize = roomData[1];
                int roomVisited = roomData[2];

                var poseStack = event.getMatrixStack();

                if (roomVisited == 1 || roomVisited == 2) GuiComponent.fill(poseStack, mapX - roomSize, mapZ - roomSize, mapX + roomSize, mapZ + roomSize, roomColors.get(roomType));

                if (roomVisited == 2 || roomVisited == 3) GuiComponent.fill(poseStack, mapX - 1, mapZ - 1, mapX + 1, mapZ + 1, importantRoomColor);
            }
        }
    }

    public static void onWindowResize() {
        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int mapWidth = (int) (w * 0.25f);
        int mapHeight = (int) (w * 0.25f); // this seems weird but it was similar in original implementation
        mapStartX = w - mapWidth;
        mapStartY = h - mapHeight;

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
        prepped = true;
    }
}