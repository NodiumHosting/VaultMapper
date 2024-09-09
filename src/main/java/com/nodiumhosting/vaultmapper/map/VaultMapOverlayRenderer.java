package com.nodiumhosting.vaultmapper.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMapOverlayRenderer {
    public static boolean enabled = false;
    
    static int mapRoomWidth;

    static boolean prepped = false;

    static int centerX;
    static int centerZ;

    static int mapAnchorX = 0;
    static int mapAnchorZ = 0;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Post event) {
        if (!enabled) return;
        if (!prepped) prep();

        int offsetX = ClientConfig.MAP_X_OFFSET.get();
        int offsetZ = ClientConfig.MAP_Y_OFFSET.get();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // cell map
        VaultMap.cells.forEach((cell) -> {
            renderCell(bufferBuilder, cell, parseColor(ClientConfig.ROOM_COLOR.get()));
        });

        // start room
        renderCell(bufferBuilder, VaultMap.startRoom, parseColor(ClientConfig.START_ROOM_COLOR.get()));

        // inscription rooms
        VaultMap.inscriptionRooms.forEach((cell) -> {
            renderCell(bufferBuilder, cell, parseColor(ClientConfig.INSCRIPTION_ROOM_COLOR.get()));
        });

        // marked rooms
        VaultMap.markedRooms.forEach((cell -> {
            renderCell(bufferBuilder, cell, parseColor(ClientConfig.MARKED_ROOM_COLOR.get()));
        }));

        bufferBuilder.end();
        BufferUploader.end(bufferBuilder); // render the map

        // player thingy
        if(VaultMap.currentRoom != null) {
            int mapX = centerX + VaultMap.currentRoom.x * mapRoomWidth + offsetX; //breaks with certain high values, god knows why
            int mapZ = centerZ + VaultMap.currentRoom.z * mapRoomWidth + offsetZ; //breaks with certain high values, god knows why
            var triag = getRotatedTriangle();
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(triag.get(0) + mapX + 3, triag.get(1) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
            bufferBuilder.vertex(triag.get(2) + mapX + 3, triag.get(3) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
            bufferBuilder.vertex(triag.get(4) + mapX + 3, triag.get(5) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
        }

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static ArrayList<Float> getRotatedTriangle() { // returns three points that make a rotated triangle when added with mapx,z
        double x1 = -3;
        double y1 = -2;
        double x2 = -3;
        double y2 = 2;
        double x3 = 3;
        double y3 = 0;

        double cx = -3; // centers to rotate about
        double cy = 0;
        float playerYaw = Minecraft.getInstance().player.getYHeadRot();
        float radangle = (float) Math.toRadians(playerYaw + 90);

        double[] rotatedVert1 = rotatePoint(x1, y1, cx, cy, radangle);
        double[] rotatedVert2 = rotatePoint(x2, y2, cx, cy, radangle);
        double[] rotatedVert3 = rotatePoint(x3, y3, cx, cy, radangle);

        var retlist = new ArrayList<Float>();
        retlist.add((float) rotatedVert1[0]);
        retlist.add((float) rotatedVert1[1]);
        retlist.add((float) rotatedVert2[0]);
        retlist.add((float) rotatedVert2[1]);
        retlist.add((float) rotatedVert3[0]);
        retlist.add((float) rotatedVert3[1]);
        return retlist;
    }

    private static double[] rotatePoint(double x, double y, double cx, double cy, double angle) {
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);

        // Translate point to origin
        double translatedX = x - cx;
        double translatedY = y - cy;

        // Rotate point
        double rotatedX = translatedX * cosTheta - translatedY * sinTheta;
        double rotatedY = translatedX * sinTheta + translatedY * cosTheta;

        // Translate point back
        double finalX = rotatedX + cx;
        double finalY = rotatedY + cy;

        return new double[]{finalX, finalY};
    }

    private static void renderCell(BufferBuilder bufferBuilder, VaultCell cell, int color) {
        if (cell.type != CellType.NONE) {
            int mapX = centerX + cell.x * mapRoomWidth + ClientConfig.MAP_X_OFFSET.get();
            int mapZ = centerZ + cell.z * mapRoomWidth + ClientConfig.MAP_Y_OFFSET.get();
            int startX;
            int startZ;
            int endX;
            int endZ;
            if (cell.type == CellType.TUNNEL) {
                if (cell.tType == TunnelType.X_FACING) {
                    startX = mapX - 2;
                    startZ = mapZ - 1;
                    endX = mapX + 2;
                    endZ = mapZ + 1;
                } else { // Z facing
                    startX = mapX - 1;
                    startZ = mapZ - 2;
                    endX = mapX + 1;
                    endZ = mapZ + 2;
                }
            } else { // square
                startX = mapX - 2;
                startZ = mapZ - 2;
                endX = mapX + 2;
                endZ = mapZ + 2;
            }
            var minX = Math.min(startX, endX);
            var maxX = Math.max(startX, endX);
            var minZ = Math.min(startZ, endZ);
            var maxZ = Math.max(startZ, endZ);

            bufferBuilder.vertex(minX, maxZ, 0).color(color).endVertex();
            bufferBuilder.vertex(maxX, maxZ, 0).color(color).endVertex();
            bufferBuilder.vertex(maxX, minZ, 0).color(color).endVertex();
            bufferBuilder.vertex(minX, minZ, 0).color(color).endVertex();
        }
    }

    public static void onWindowResize() {
        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int mapSize = (int) (w * 0.25f);

        mapRoomWidth = mapSize / 49;

        updateAnchor();
    }

    public static void updateAnchor() {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int mapSize = (VaultMap.currentMapSize * mapRoomWidth);

        switch (ClientConfig.MAP_X_ANCHOR.get()) {
            case 0 -> {
                mapAnchorX = (mapSize / 3) * 2;
            }
            case 1 -> {
                mapAnchorX = width / 4;
            }
            case 2 -> {
                mapAnchorX = width / 2;
            }
            case 3 -> {
                mapAnchorX = width - width / 4;
            }
            case 4 -> {
                mapAnchorX = width - (mapSize / 3) * 2;
            }
        }

        switch (ClientConfig.MAP_Y_ANCHOR.get()) {
            case 0 -> {
                mapAnchorZ = (mapSize / 3) * 2;
            }
            case 1 -> {
                mapAnchorZ = height / 4;
            }
            case 2 -> {
                mapAnchorZ = height / 2;
            }
            case 3 -> {
                mapAnchorZ = height - height / 4;
            }
            case 4 -> {
                mapAnchorZ = height - (mapSize / 3) * 2;
            }
        }

        centerX = mapAnchorX;
        centerZ = mapAnchorZ;
    }

    private static int parseColor(String hexColor) {
        try {
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }

            if (hexColor.length() == 6) {
                hexColor = "FF" + hexColor;  // Add full opacity if not specified
            }

            // Cast to int to use it as a 32-bit ARGB color
            return (int) Long.parseLong(hexColor, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF00; // Default color (white with full opacity)
        }
    }

    public static void prep() {
        onWindowResize();
        VaultMapper.LOGGER.info("prep ran");
        prepped = true;
    }
}