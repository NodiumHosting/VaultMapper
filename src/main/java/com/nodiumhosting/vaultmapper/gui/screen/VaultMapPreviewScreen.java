package com.nodiumhosting.vaultmapper.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nodiumhosting.vaultmapper.snapshots.MapSnapshot;
import com.nodiumhosting.vaultmapper.map.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;

import java.util.List;
import java.util.Optional;

public class VaultMapPreviewScreen extends Screen {
    private final List<VaultCell> cells;

    private final Optional<Screen> lastScreen;

    public VaultMapPreviewScreen(MapSnapshot snapshot, Optional<Screen> previousScreen) {
        super(new TextComponent(""));

        cells = snapshot.cells;

        lastScreen = previousScreen;
    }

    protected void init() {
        super.init();
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        //draw basic container
        int x = this.width / 2 - 150;
        int y = this.height / 2 - 150;
        int w = 300;
        int h = 300;
        this.fillGradient(pose, x, y, x + w, y + h, 0xFF000000, 0xFF000000);
        this.fillGradient(pose, x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC6C6C6, 0xFFC6C6C6);

        //draw string saying how many of the cell types there are
        int cellCount = cells.stream().filter(cell -> cell.cellType == CellType.ROOM && cell.explored).toArray().length;
        int inscriptionCount = cells.stream().filter(cell -> cell.inscripted).toArray().length;
        int markedCount = cells.stream().filter(cell -> cell.marked).toArray().length;
        this.font.drawShadow(pose, "Explored Rooms: " + cellCount, x + 5, y + 5, 0xFFFFFF);
        this.font.drawShadow(pose, "Inscription Rooms: " + inscriptionCount, x + 5, y + 15, 0xFFFFFF);
        this.font.drawShadow(pose, "Marked Rooms: " + markedCount, x + 5, y + 25, 0xFFFFFF);

        //draw the map
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // cell map
        cells.forEach((cell) -> {
            renderCell(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)));
        });

        bufferBuilder.end();
        BufferUploader.end(bufferBuilder); // render the map

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        super.render(pose, mouseX, mouseY, partialTick);

        // Render things after widgets (tooltips)
    }

    private void renderCell(BufferBuilder bufferBuilder, VaultCell cell, int color) {
        if (cell.cellType != CellType.NONE) {
            int mapRoomWidth = 250 / 49;
            int centerX = this.width / 2;
            int centerZ = this.height / 2;
            int mapX = centerX + cell.x * mapRoomWidth;
            int mapZ = centerZ + cell.z * mapRoomWidth;
            int startX;
            int startZ;
            int endX;
            int endZ;
            if (cell.cellType == CellType.TUNNEL_X || cell.cellType == CellType.TUNNEL_Z) {
                if (cell.cellType == CellType.TUNNEL_X) { // X facing
                    startX = mapX - (mapRoomWidth / 2);
                    startZ = mapZ - (mapRoomWidth / 4);
                    endX = mapX + (mapRoomWidth / 2);
                    endZ = mapZ + (mapRoomWidth / 4);
                } else { // Z facing
                    startX = mapX - (mapRoomWidth / 4);
                    startZ = mapZ - (mapRoomWidth / 2);
                    endX = mapX + (mapRoomWidth / 4);
                    endZ = mapZ + (mapRoomWidth / 2);
                }
            } else { // square
                startX = mapX - (mapRoomWidth / 2);
                startZ = mapZ - (mapRoomWidth / 2);
                endX = mapX + (mapRoomWidth / 2);
                endZ = mapZ + (mapRoomWidth / 2);
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

    @Override
    public void onClose() {
        // Stop any handlers here

        // Call last in case it interferes with the override
        super.onClose();

        if (lastScreen.isPresent()) {
            this.minecraft.setScreen(lastScreen.get());
        }
    }

    @Override
    public void removed() {
        // Reset initial states here

        // Call last in case it interferes with the override
        super.removed();
    }

    // copied block from overlay renderer, should move elsewhere
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
            return 0xFFFFFFFF; // Default color (white with full opacity)
        }
    }
}
