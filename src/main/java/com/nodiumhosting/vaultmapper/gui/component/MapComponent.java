package com.nodiumhosting.vaultmapper.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.proto.CellType;
import com.nodiumhosting.vaultmapper.util.MapRoomIconUtil;
import com.nodiumhosting.vaultmapper.util.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MapComponent extends GuiComponent implements Widget {
    private final int x0;
    private final int y0;
    private final int x1;
    private final int y1;
    private final int width;
    private final int height;
    private final int centerX;
    private final int centerZ;

    public final List<VaultCell> cells;

    private static final int mapRoomWidth = 16;

    public MapComponent(int x, int y, int width, int height, List<VaultCell> cells) {
        super();

        this.x0 = x;
        this.y0 = y;
        this.x1 = x + width;
        this.y1 = y + height;
        this.width = width;
        this.height = height;
        this.centerX = x + width / 2;
        this.centerZ = y + height / 2;

        this.cells = cells;
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int x, int y, float partialTicks) {
        boolean showRoomIcons = ClientConfig.SHOW_ROOM_ICONS.get();

        cells.forEach((cell) -> {
            if (cell.cellType == CellType.CELLTYPE_UNKNOWN) return;
            renderCell(poseStack, cell, showRoomIcons);
        });
    }

    private void renderCell(PoseStack poseStack, VaultCell cell, boolean renderIcon) {
        if (cell.cellType == CellType.CELLTYPE_UNKNOWN) return;
        if (cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get()) return;

        float mapX = centerX + (cell.x) * mapRoomWidth;
        float mapZ = centerZ + (cell.z) * mapRoomWidth;
        float roomWidth = (float) mapRoomWidth / 2;
        float tunnelLen = (float) mapRoomWidth / 2;
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

        if ((cell.marked || cell.inscripted) && ClientConfig.SHOW_ROOM_ICONS.get()) {
            minX -= (float) (mapRoomWidth * 0.5);
            maxX += (float) (mapRoomWidth * 0.5);
            minZ -= (float) (mapRoomWidth * 0.5);
            maxZ += (float) (mapRoomWidth * 0.5);
        }

        int color = parseColor(VaultMap.getCellColor(cell));
        this.fillGradient(poseStack, (int) minX, (int) minZ, (int) maxX, (int) maxZ, color, color);

        if (!renderIcon) return;

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        renderCellIcon(poseStack, cell);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
    }

    private void renderCellIcon(PoseStack poseStack, VaultCell cell) {
        if (cell.roomName == null || cell.roomName.equals("")) {
            cell.roomName = cell.roomType.name();
        }

        ResourceLocation icon = MapRoomIconUtil.getIconForRoom(cell.roomName);

        RenderSystem.setShaderTexture(0, icon);
        this.blit(poseStack, (centerX + cell.x * mapRoomWidth), (centerZ + cell.z * mapRoomWidth), 0, 0, 16, 16);
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
}
