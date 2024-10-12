package com.nodiumhosting.vaultmapper.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.util.ResearchUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMapOverlayRenderer {
    public static boolean enabled = false;
    public static boolean ignoreResearchRequirement = false;

    static float mapScaleMultiplier;
    static float mapRoomWidth;

    static boolean prepped = false;

    static float centerX;
    static float centerZ;

    static float mapAnchorX = 0;
    static float mapAnchorZ = 0;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Post event) {
        if (!ResearchUtil.hasResearch("Vault Compass") && !ignoreResearchRequirement) return;
        if (!enabled) return;
        if (!ClientConfig.MAP_ENABLED.get()) return;
        if (!prepped) prep();

        int offsetX = ClientConfig.MAP_X_OFFSET.get();
        int offsetZ = ClientConfig.MAP_Y_OFFSET.get();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // Tunnel map
        VaultMap.cells.stream().filter((cell) -> cell.cellType == CellType.TUNNEL_X || cell.cellType == CellType.TUNNEL_Z).forEach((cell) -> {
            renderCell(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)));
        });

        // cell map
        VaultMap.cells.stream().filter((cell) -> cell.cellType == CellType.ROOM).forEach((cell) -> {
            renderCell(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)));
        });

        bufferBuilder.end();
        BufferUploader.end(bufferBuilder); // render the map

        // render icons
        VaultMap.cells.stream().filter((cell) -> cell.cellType == CellType.ROOM).forEach((cell) -> {
            // TODO: render icon
            if (cell.roomName == RoomName.UNKNOWN) return;

            String path = "textures/icons/" + cell.roomName.getName().toLowerCase() + ".png";
            VaultMapper.LOGGER.info("path: " + path);

            try {
                ResourceLocation icon = new ResourceLocation("vaultmapper", path);
                RenderSystem.setShaderTexture(0, icon);
                Gui.blit(event.getMatrixStack(), (int) (centerX + cell.x * mapRoomWidth + offsetX), (int) (centerZ + cell.z * mapRoomWidth + offsetZ), 0, 0, (int) mapRoomWidth, (int) mapRoomWidth, 16, 16);
            } catch (Exception e) {
                VaultMapper.LOGGER.error("Failed to render icon for room: " + cell.roomName.getName());
            }
        });

        // player thingy
        if (VaultMap.currentRoom != null) {
            float mapX = centerX + VaultMap.currentRoom.x * mapRoomWidth + offsetX; //breaks with certain high values, god knows why
            float mapZ = centerZ + VaultMap.currentRoom.z * mapRoomWidth + offsetZ; //breaks with certain high values, god knows why
            var triag = getRotatedTriangle();
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(triag.get(0) + mapX + (3 * mapScaleMultiplier), triag.get(1) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
            bufferBuilder.vertex(triag.get(2) + mapX + (3 * mapScaleMultiplier), triag.get(3) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
            bufferBuilder.vertex(triag.get(4) + mapX + (3 * mapScaleMultiplier), triag.get(5) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
        }

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static ArrayList<Float> getRotatedTriangle() { // returns three points that make a rotated triangle when added with mapx,z
        double x1 = -3 * mapScaleMultiplier;
        double y1 = -2 * mapScaleMultiplier;
        double x2 = -3 * mapScaleMultiplier;
        double y2 = 2 * mapScaleMultiplier;
        double x3 = 3 * mapScaleMultiplier;
        double y3 = 0/* * mapScaleMultiplier*/;

        double cx = -3 * mapScaleMultiplier; // centers to rotate about
        double cy = 0/*  * mapScaleMultiplier*/;
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

    public static void renderCell(BufferBuilder bufferBuilder, VaultCell cell, int color) {
        if (cell.cellType != CellType.NONE) {
            if (cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get()) return;
            float mapX = centerX + cell.x * mapRoomWidth + ClientConfig.MAP_X_OFFSET.get();
            float mapZ = centerZ + cell.z * mapRoomWidth + ClientConfig.MAP_Y_OFFSET.get();
            float halfRoomWidth = mapRoomWidth / 2;
            float startX;
            float startZ;
            float endX;
            float endZ;
            if (cell.cellType == CellType.TUNNEL_X || cell.cellType == CellType.TUNNEL_Z) {
                if (cell.cellType == CellType.TUNNEL_X) { // X facing
                    startX = mapX - halfRoomWidth;
                    startZ = mapZ - halfRoomWidth / 2;
                    endX = mapX + halfRoomWidth;
                    endZ = mapZ + halfRoomWidth / 2;
                } else { // Z facing
                    startX = mapX - halfRoomWidth / 2;
                    startZ = mapZ - halfRoomWidth;
                    endX = mapX + halfRoomWidth / 2;
                    endZ = mapZ + halfRoomWidth;
                }
            } else { // square
                startX = mapX - halfRoomWidth;
                startZ = mapZ - halfRoomWidth;
                endX = mapX + halfRoomWidth;
                endZ = mapZ + halfRoomWidth;
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
        int baseMapRoomWidth = mapSize / 49;
        mapScaleMultiplier = (float) ClientConfig.MAP_SCALE.get() / 10;
        mapRoomWidth = ((float) baseMapRoomWidth * mapScaleMultiplier);

        updateAnchor();
    }

    public static void updateAnchor() {
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        float mapSize = (VaultMap.currentMapSize * mapRoomWidth);

        switch (ClientConfig.MAP_X_ANCHOR.get()) {
            case 0 -> {
                mapAnchorX = (mapSize / 3) * 2;
            }
            case 1 -> {
                mapAnchorX = (float) width / 4;
            }
            case 2 -> {
                mapAnchorX = (float) width / 2;
            }
            case 3 -> {
                mapAnchorX = width - (float) width / 4;
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
                mapAnchorZ = (float) height / 4;
            }
            case 2 -> {
                mapAnchorZ = (float) height / 2;
            }
            case 3 -> {
                mapAnchorZ = height - (float) height / 4;
            }
            case 4 -> {
                mapAnchorZ = height - (mapSize / 3) * 2;
            }
        }

        centerX = mapAnchorX;
        centerZ = mapAnchorZ;
    }

    public static int parseColor(String hexColor) {
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