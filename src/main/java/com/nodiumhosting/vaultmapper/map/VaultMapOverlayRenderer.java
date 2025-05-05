package com.nodiumhosting.vaultmapper.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.proto.CellType;
import com.nodiumhosting.vaultmapper.proto.RoomType;
import com.nodiumhosting.vaultmapper.util.MapRoomIconUtil;
import com.nodiumhosting.vaultmapper.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMapOverlayRenderer {
    public static boolean enabled = false;
    public static boolean ignoreResearchRequirement = false;
    public static boolean syncErrorState = false;
    static boolean playerCentricRender = ClientConfig.PLAYER_CENTRIC_RENDERING.get();
    static int cutoff = ClientConfig.PC_CUTOFF.get();
    static float mapScaleMultiplier;
    static float mapRoomWidth;
    static boolean prepped = false;
    static float centerX;
    static float centerZ;
    static float mapAnchorX = 0;
    static float mapAnchorZ = 0;
    static int playerX;
    static int playerZ;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Post event) {
//        if (!ResearchUtil.hasResearch("Vault Compass") && !ignoreResearchRequirement) return;
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (!enabled) return;
        if (!ClientConfig.MAP_ENABLED.get()) return;
        if (!prepped) prep();

        int offsetX = ClientConfig.MAP_X_OFFSET.get();
        int offsetZ = ClientConfig.MAP_Y_OFFSET.get();

        if (VaultMap.currentRoom != null) {
            playerX = VaultMap.currentRoom.x;
            playerZ = VaultMap.currentRoom.z;
        } else {
            playerX = 0;
            playerZ = 0;
        }
        if (syncErrorState) {
            float offset = playerCentricRender ? (cutoff + 1) * mapRoomWidth : (VaultMap.northSize + 1) * mapRoomWidth;
            TextComponent syncError = new TextComponent("Sync Error");
            GuiComponent.drawCenteredString(event.getMatrixStack(), Minecraft.getInstance().font, syncError, (int) centerX + offsetX, (int) mapAnchorZ + offsetZ - (int) offset - 9, 0xFFFFFF);
        }

        if (VaultMap.viewerCode != null && ClientConfig.SHOW_VIEWER_CODE.get()) {
            float offset = playerCentricRender ? (cutoff + 1) * mapRoomWidth : (VaultMap.southSize + 1) * mapRoomWidth;
            TextComponent syncError = new TextComponent("Viewer Code: " + VaultMap.viewerCode);
            GuiComponent.drawCenteredString(event.getMatrixStack(), Minecraft.getInstance().font, syncError, (int) centerX + offsetX, (int) mapAnchorZ + offsetZ + (int) offset, 0xFFFFFF);
        }

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // map border if player centric render and border enabled in config
        if (playerCentricRender && ClientConfig.PC_BORDER.get()) {
            renderMapBorderPC(bufferBuilder, 0xDD808080);
        }

        // Tunnel map
        VaultMap.cells.stream().filter((cell) -> cell.cellType == CellType.CELLTYPE_TUNNEL_X || cell.cellType == CellType.CELLTYPE_TUNNEL_Z).forEach((cell) -> {
            if (playerCentricRender) {
                renderCellPC(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)));
            } else {
                renderCell(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)));
            }
        });

        // cell map
        VaultMap.cells.stream().filter((cell) -> cell.cellType == CellType.CELLTYPE_ROOM).forEach((cell) -> {
            if (playerCentricRender) {
                renderCellPC(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)));
            } else {
                renderCell(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)));
            }
        });

        bufferBuilder.end();
        BufferUploader.end(bufferBuilder); // render the map

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        // render icons
        if (ClientConfig.SHOW_ROOM_ICONS.get()) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.disableBlend();
            VaultMap.cells.stream().filter((cell) -> cell.cellType == CellType.CELLTYPE_ROOM).forEach((cell) -> {
                if (cell.roomName == null || cell.roomName.equals("")) {
                    cell.roomName = cell.roomType.name();
                }

                try {
                    ResourceLocation icon = MapRoomIconUtil.getIconForRoom(cell.roomName);
                    if (!Minecraft.getInstance().getResourceManager().hasResource(icon)) {
                        VaultMapper.LOGGER.error("Icon {} not found for room: {}", icon, cell.roomName);
                    }
                    RenderSystem.setShaderTexture(0, icon);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

                    //Gui.blit(event.getMatrixStack(), (int) (centerX + cell.x * mapRoomWidth + offsetX), (int) (centerZ + cell.z * mapRoomWidth + offsetZ), 0, 0, (int) mapRoomWidth, (int) mapRoomWidth, 16, 16);
                    //VaultMapper.LOGGER.info(String.valueOf(mapRoomWidth));
                    if (playerCentricRender) {
                        renderTextureCellPC(bufferBuilder, cell);
                    } else {
                        renderTextureCell(bufferBuilder, cell);
                    }
                } catch (Exception e) {
                    VaultMapper.LOGGER.error("Failed to render icon for room: " + cell.roomName);
                }

                bufferBuilder.end();
                BufferUploader.end(bufferBuilder);
            });

            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }


        bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        // player thingies
        //Logger.getAnonymousLogger().info(String.valueOf(VaultMap.players.size()));
        if (playerCentricRender) {
            VaultMap.players.forEach((name, data) -> {
                if (abs(data.x - playerX) > cutoff || abs(data.y - playerZ) > cutoff) return;
                float mapX = centerX + (data.x - playerX) * mapRoomWidth + offsetX; //breaks with certain high values, god knows why
                float mapZ = centerZ + (data.y - playerZ) * mapRoomWidth + offsetZ; //breaks with certain high values, god knows why
                var triag = getRotatedTriangle(data.yaw);
                bufferBuilder.vertex(triag.get(0) + mapX + (3 * mapScaleMultiplier), triag.get(1) + mapZ, 0).color(parseColor(data.color)).endVertex();
                bufferBuilder.vertex(triag.get(2) + mapX + (3 * mapScaleMultiplier), triag.get(3) + mapZ, 0).color(parseColor(data.color)).endVertex();
                bufferBuilder.vertex(triag.get(4) + mapX + (3 * mapScaleMultiplier), triag.get(5) + mapZ, 0).color(parseColor(data.color)).endVertex();
            });
        } else {
            VaultMap.players.forEach((name, data) -> {
                float mapX = centerX + data.x * mapRoomWidth + offsetX; //breaks with certain high values, god knows why
                float mapZ = centerZ + data.y * mapRoomWidth + offsetZ; //breaks with certain high values, god knows why
                var triag = getRotatedTriangle(data.yaw);
                bufferBuilder.vertex(triag.get(0) + mapX + (3 * mapScaleMultiplier), triag.get(1) + mapZ, 0).color(parseColor(data.color)).endVertex();
                bufferBuilder.vertex(triag.get(2) + mapX + (3 * mapScaleMultiplier), triag.get(3) + mapZ, 0).color(parseColor(data.color)).endVertex();
                bufferBuilder.vertex(triag.get(4) + mapX + (3 * mapScaleMultiplier), triag.get(5) + mapZ, 0).color(parseColor(data.color)).endVertex();
            });
        }
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);


        // player thingy TODO: Might need to adjust this thing as the previous loop handles it
        if (playerCentricRender) {
            if (VaultMap.currentRoom != null) {
                float mapX = centerX + offsetX; //breaks with certain high values, god knows why
                float mapZ = centerZ + offsetZ; //breaks with certain high values, god knows why
                var triag = getRotatedTriangle();
                bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
                bufferBuilder.vertex(triag.get(0) + mapX + (3 * mapScaleMultiplier), triag.get(1) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
                bufferBuilder.vertex(triag.get(2) + mapX + (3 * mapScaleMultiplier), triag.get(3) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
                bufferBuilder.vertex(triag.get(4) + mapX + (3 * mapScaleMultiplier), triag.get(5) + mapZ, 0).color(parseColor(ClientConfig.POINTER_COLOR.get())).endVertex();
                bufferBuilder.end();
                BufferUploader.end(bufferBuilder);
            }
        } else {
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
        }

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static ArrayList<Float> getRotatedTriangle(float yaw) { // returns three points that make a rotated triangle when added with mapx,z
        double x1 = -3 * mapScaleMultiplier;
        double y1 = -2 * mapScaleMultiplier;
        double x2 = -3 * mapScaleMultiplier;
        double y2 = 2 * mapScaleMultiplier;
        double x3 = 3 * mapScaleMultiplier;
        double y3 = 0 * mapScaleMultiplier;

        double cx = -3 * mapScaleMultiplier; // centers to rotate about
        double cy = 0;
        float radangle = (float) Math.toRadians(yaw + 90);

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

    public static void renderCellPC(BufferBuilder bufferBuilder, VaultCell cell, int color) {
        if (cell.cellType != CellType.CELLTYPE_UNKNOWN) {
            if (cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get())
                return;
            if (abs(cell.x - playerX) > cutoff || abs(cell.z - playerZ) > cutoff) return;
            float mapX = centerX + (cell.x - playerX) * mapRoomWidth + ClientConfig.MAP_X_OFFSET.get();
            float mapZ = centerZ + (cell.z - playerZ) * mapRoomWidth + ClientConfig.MAP_Y_OFFSET.get();
            //float roomWidth = (float) ((mapRoomWidth / 2) * 1.5);
            //float tunnelLen = (float) ((mapRoomWidth / 2) * 0.5);
            float roomWidth = mapRoomWidth / 2;
            float tunnelLen = mapRoomWidth / 2;
            float startX;
            float startZ;
            float endX;
            float endZ;
            if (cell.cellType == CellType.CELLTYPE_TUNNEL_X || cell.cellType == CellType.CELLTYPE_TUNNEL_Z) {
                if (cell.cellType == CellType.CELLTYPE_TUNNEL_X) { // X facing
                    startX = mapX - tunnelLen;
                    startZ = mapZ - roomWidth / 2;
                    endX = mapX + tunnelLen;
                    endZ = mapZ + roomWidth / 2;
                } else { // Z facing
                    startX = mapX - roomWidth / 2;
                    startZ = mapZ - tunnelLen;
                    endX = mapX + roomWidth / 2;
                    endZ = mapZ + tunnelLen;
                }
            } else { // square
                startX = mapX - roomWidth;
                startZ = mapZ - roomWidth;
                endX = mapX + roomWidth;
                endZ = mapZ + roomWidth;
            }
            var minX = Math.min(startX, endX);
            var maxX = Math.max(startX, endX);
            var minZ = Math.min(startZ, endZ);
            var maxZ = Math.max(startZ, endZ);

            if (cell.marked && ClientConfig.SHOW_ROOM_ICONS.get()) {
                minX -= (float) (mapRoomWidth * 0.5);
                maxX += (float) (mapRoomWidth * 0.5);
                minZ -= (float) (mapRoomWidth * 0.5);
                maxZ += (float) (mapRoomWidth * 0.5);
            }

            bufferBuilder.vertex(minX, maxZ, 0).color(color).endVertex();
            bufferBuilder.vertex(maxX, maxZ, 0).color(color).endVertex();
            bufferBuilder.vertex(maxX, minZ, 0).color(color).endVertex();
            bufferBuilder.vertex(minX, minZ, 0).color(color).endVertex();
        }
    }

    public static void renderCell(BufferBuilder bufferBuilder, VaultCell cell, int color) {
        if (cell.cellType != CellType.CELLTYPE_UNKNOWN) {
            if (cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get())
                return; // VaultMap.currentRoom.x - cellx
            float mapX = centerX + (cell.x) * mapRoomWidth + ClientConfig.MAP_X_OFFSET.get();
            float mapZ = centerZ + (cell.z) * mapRoomWidth + ClientConfig.MAP_Y_OFFSET.get();
            //float roomWidth = (float) ((mapRoomWidth / 2) * 1.5);
            //float tunnelLen = (float) ((mapRoomWidth / 2) * 0.5);
            float roomWidth = mapRoomWidth / 2;
            float tunnelLen = mapRoomWidth / 2;
            float startX;
            float startZ;
            float endX;
            float endZ;

            if (cell.cellType == CellType.CELLTYPE_TUNNEL_X || cell.cellType == CellType.CELLTYPE_TUNNEL_Z) {
                if (cell.cellType == CellType.CELLTYPE_TUNNEL_X) { // X facing
                    startX = mapX - tunnelLen;
                    startZ = mapZ - roomWidth / 2;
                    endX = mapX + tunnelLen;
                    endZ = mapZ + roomWidth / 2;
                } else { // Z facing
                    startX = mapX - roomWidth / 2;
                    startZ = mapZ - tunnelLen;
                    endX = mapX + roomWidth / 2;
                    endZ = mapZ + tunnelLen;
                }
            } else { // square
                startX = mapX - roomWidth;
                startZ = mapZ - roomWidth;
                endX = mapX + roomWidth;
                endZ = mapZ + roomWidth;
            }
            var minX = Math.min(startX, endX);
            var maxX = Math.max(startX, endX);
            var minZ = Math.min(startZ, endZ);
            var maxZ = Math.max(startZ, endZ);

            if (cell.marked && ClientConfig.SHOW_ROOM_ICONS.get()) {
                minX -= (float) (mapRoomWidth * 0.5);
                maxX += (float) (mapRoomWidth * 0.5);
                minZ -= (float) (mapRoomWidth * 0.5);
                maxZ += (float) (mapRoomWidth * 0.5);
            }

            bufferBuilder.vertex(minX, maxZ, 0).color(color).endVertex();
            bufferBuilder.vertex(maxX, maxZ, 0).color(color).endVertex();
            bufferBuilder.vertex(maxX, minZ, 0).color(color).endVertex();
            bufferBuilder.vertex(minX, minZ, 0).color(color).endVertex();
        }
    }

    public static void renderTextureCell(BufferBuilder bufferBuilder, VaultCell cell) {
        if (cell.cellType == CellType.CELLTYPE_ROOM) {
            if (cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get()) return;
            float mapX = centerX + cell.x * mapRoomWidth + ClientConfig.MAP_X_OFFSET.get();
            float mapZ = centerZ + cell.z * mapRoomWidth + ClientConfig.MAP_Y_OFFSET.get();
            //float roomWidth = (float) (mapRoomWidth * 1.5);
            float roomWidth = mapRoomWidth;
            float startX;
            float startZ;
            float endX;
            float endZ;

            startX = mapX - roomWidth;
            startZ = mapZ - roomWidth;
            endX = mapX + roomWidth;
            endZ = mapZ + roomWidth;

            var minX = Math.min(startX, endX);
            var maxX = Math.max(startX, endX);
            var minZ = Math.min(startZ, endZ);
            var maxZ = Math.max(startZ, endZ);

            bufferBuilder.vertex(minX, maxZ, 0).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(maxX, maxZ, 0).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(maxX, minZ, 0).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(minX, minZ, 0).uv(0.0F, 0.0F).endVertex();
        }
    }

    public static void renderTextureCellPC(BufferBuilder bufferBuilder, VaultCell cell) {
        if (cell.cellType == CellType.CELLTYPE_ROOM) {
            if (cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get()) return;
            if (abs(cell.x - playerX) > cutoff || abs(cell.z - playerZ) > cutoff) return;
            float mapX = centerX + (cell.x - playerX) * mapRoomWidth + ClientConfig.MAP_X_OFFSET.get();
            float mapZ = centerZ + (cell.z - playerZ) * mapRoomWidth + ClientConfig.MAP_Y_OFFSET.get();
            //float roomWidth = (float) (mapRoomWidth * 1.5);
            float roomWidth = mapRoomWidth;
            float startX;
            float startZ;
            float endX;
            float endZ;

            startX = mapX - roomWidth;
            startZ = mapZ - roomWidth;
            endX = mapX + roomWidth;
            endZ = mapZ + roomWidth;

            var minX = Math.min(startX, endX);
            var maxX = Math.max(startX, endX);
            var minZ = Math.min(startZ, endZ);
            var maxZ = Math.max(startZ, endZ);

            bufferBuilder.vertex(minX, maxZ, 0).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(maxX, maxZ, 0).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(maxX, minZ, 0).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(minX, minZ, 0).uv(0.0F, 0.0F).endVertex();
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

        int sideMargin = 2*(int)mapRoomWidth + Minecraft.getInstance().font.lineHeight;
        switch (ClientConfig.MAP_X_ANCHOR.get()) {
            case 0 -> {
                if (playerCentricRender){
                    mapAnchorX = mapRoomWidth * cutoff + sideMargin;
                } else {
                    mapAnchorX = VaultMap.westSize * mapRoomWidth + mapRoomWidth + sideMargin;
                }
            }
            case 1 -> {
                mapAnchorX = (float) width / 4;
            }
            case 2 -> {
                mapAnchorX = (float) width / 2;
            }
            case 3 -> {
                mapAnchorX = width - ((float) width / 4);
            }
            case 4 -> {
                if (playerCentricRender){
                    mapAnchorX = width - mapRoomWidth * cutoff - sideMargin;
                } else {
                    mapAnchorX = width - VaultMap.eastSize * mapRoomWidth + mapRoomWidth - sideMargin;
                }
            }
        }

        switch (ClientConfig.MAP_Y_ANCHOR.get()) {
            case 0 -> {
                if (playerCentricRender){
                    mapAnchorZ = mapRoomWidth * cutoff + sideMargin;
                } else {
                    mapAnchorZ = VaultMap.northSize * mapRoomWidth + mapRoomWidth + sideMargin;
                }
            }
            case 1 -> {
                mapAnchorZ = (float) height / 4;
            }
            case 2 -> {
                mapAnchorZ = (float) height / 2;
            }
            case 3 -> {
                mapAnchorZ = height - ((float) height / 4);
            }
            case 4 -> {
                if (playerCentricRender){
                    mapAnchorZ = height - mapRoomWidth * cutoff - sideMargin;
                } else {
                    mapAnchorZ = height - VaultMap.southSize * mapRoomWidth + mapRoomWidth - sideMargin;
                }
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
            return 0xFFFFFFFF; // Default color (white with full opacity)
        }
    }

    public static void prep() {
        playerCentricRender = ClientConfig.PLAYER_CENTRIC_RENDERING.get();
        cutoff = ClientConfig.PC_CUTOFF.get();

        onWindowResize();
        VaultMapper.LOGGER.info("prep ran");
        prepped = true;
    }

    public static void renderMapBorderPC(BufferBuilder bufferBuilder, int color) {
        float lineWidth = 1;
        float mapSize = (float) (((cutoff + 0.5) * mapRoomWidth) * 2);
        float mapSizeDelta = mapSize / 2;
        float mapX = centerX - mapSizeDelta + ClientConfig.MAP_X_OFFSET.get();
        float mapZ = centerZ - mapSizeDelta + ClientConfig.MAP_Y_OFFSET.get();

        bufferBuilder.vertex(mapX - lineWidth, mapZ, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX + mapSize + lineWidth, mapZ, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX + mapSize + lineWidth, mapZ - lineWidth, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX - lineWidth, mapZ - lineWidth, 0).color(color).endVertex();

        // Bottom border
        bufferBuilder.vertex(mapX - lineWidth, mapZ + mapSize + lineWidth, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX + mapSize + lineWidth, mapZ + mapSize + lineWidth, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX + mapSize + lineWidth, mapZ + mapSize, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX - lineWidth, mapZ + mapSize, 0).color(color).endVertex();

        // Left border
        bufferBuilder.vertex(mapX - lineWidth, mapZ + mapSize, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX, mapZ + mapSize, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX, mapZ, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX - lineWidth, mapZ, 0).color(color).endVertex();

        // Right border
        bufferBuilder.vertex(mapX + mapSize, mapZ + mapSize, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX + mapSize + lineWidth, mapZ + mapSize, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX + mapSize + lineWidth, mapZ, 0).color(color).endVertex();
        bufferBuilder.vertex(mapX + mapSize, mapZ, 0).color(color).endVertex();

    }
}