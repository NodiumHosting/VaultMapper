package com.nodiumhosting.vaultmapper.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.map.CellType;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.snapshots.MapSnapshot;
import iskallia.vault.client.gui.framework.element.ElasticContainerElement;
import iskallia.vault.client.gui.framework.element.LabelElement;
import iskallia.vault.client.gui.framework.element.VerticalScrollClipContainer;
import iskallia.vault.client.gui.framework.render.spi.IElementRenderer;
import iskallia.vault.client.gui.framework.spatial.Padding;
import iskallia.vault.client.gui.framework.spatial.Spatials;
import iskallia.vault.client.gui.framework.spatial.spi.IMutableSpatial;
import iskallia.vault.client.gui.framework.spatial.spi.IPosition;
import iskallia.vault.client.gui.framework.spatial.spi.ISpatial;
import iskallia.vault.client.gui.framework.text.LabelTextStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer.parseColor;

public class MapContainerElement extends VerticalScrollClipContainer<MapContainerElement> {
    boolean isDragging;
    double mapCenterX;
    double mapCenterZ;
    double prevMouseX;
    double prevMouseZ;

    public MapContainerElement(ISpatial spatial, UUID vaultUuid) {
        super(spatial, Padding.of(2, 0));
        this.addElement(new MapElement(Spatials.positionY(3), vaultUuid, this)).postLayout((screen, gui, parent, world) -> {
            world.translateX((this.innerWidth() - world.width()) / 2);
            return true;
        });
    }

    private static void renderCell(BufferBuilder bufferBuilder, VaultCell cell, int color, float centerX, float centerZ, float width) {
        if (cell.cellType != CellType.NONE) {
            if (cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get()) return;
            float mapX = centerX + cell.x * width;
            float mapZ = centerZ + cell.z * width;
            float coordOffset = width / 2;
            float startX;
            float startZ;
            float endX;
            float endZ;
            if (cell.cellType == CellType.TUNNEL_X || cell.cellType == CellType.TUNNEL_Z) {
                if (cell.cellType == CellType.TUNNEL_X) { // X facing
                    startX = mapX - coordOffset;
                    startZ = mapZ - coordOffset / 2;
                    endX = mapX + coordOffset;
                    endZ = mapZ + coordOffset / 2;
                } else { // Z facing
                    startX = mapX - coordOffset / 2;
                    startZ = mapZ - coordOffset;
                    endX = mapX + coordOffset / 2;
                    endZ = mapZ + coordOffset;
                }
            } else { // square
                startX = mapX - coordOffset;
                startZ = mapZ - coordOffset;
                endX = mapX + coordOffset;
                endZ = mapZ + coordOffset;
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
    public void onMouseMoved(double mouseX, double mouseY) {
        if (isDragging) {
            mapCenterX += (mouseX - prevMouseX);
            mapCenterZ += (mouseY - prevMouseZ);
        }
        prevMouseX = mouseX;
        prevMouseZ = mouseY;
        super.onMouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, int buttonIndex) {
        isDragging = true;
        return super.onMouseClicked(mouseX, mouseY, buttonIndex);
    }

    @Override
    public boolean onMouseReleased(double mouseX, double mouseY, int buttonIndex) {
        isDragging = false;
        return super.onMouseReleased(mouseX, mouseY, buttonIndex);
    }

    private static final class MapElement extends ElasticContainerElement<MapElement> {
        List<VaultCell> cells;
        MapContainerElement window;

        private MapElement(IPosition position, UUID vaultUuid, MapContainerElement window) {
            super(Spatials.positionXYZ(position));
            this.window = window;
            Optional<MapSnapshot> optMap = MapSnapshot.from(vaultUuid);
            if (optMap.isEmpty()) {
                return;
            }
            MapSnapshot map = optMap.get();

            cells = map.cells;
            int cellCount = cells.stream().filter(cell -> cell.cellType == CellType.ROOM && cell.explored).toArray().length;
            int inscriptionCount = cells.stream().filter(cell -> cell.inscripted).toArray().length;
            int markedCount = cells.stream().filter(cell -> cell.marked).toArray().length;

            IMutableSpatial spatial = Spatials.positionXYZ(position);
            spatial.positionZ(10); // TRY JUST ONE

            // x was -35
            this.addElement(new LabelElement(spatial.positionX(-55).positionY(5), new TextComponent("Explored Rooms: " + cellCount), new LabelTextStyle.Builder()));
            this.addElement(new LabelElement(spatial.positionX(-55).positionY(15), new TextComponent("Inscription Rooms: " + inscriptionCount), new LabelTextStyle.Builder()));
            this.addElement(new LabelElement(spatial.positionX(-55).positionY(25), new TextComponent("Marked Rooms: " + markedCount), new LabelTextStyle.Builder()));

            int top = spatial.top();
            int left = spatial.left();
            int right = spatial.right();
            int bottom = spatial.bottom();
            int width = spatial.width();
            int height = spatial.height();

            VaultMapper.LOGGER.info("top: " + top);
            VaultMapper.LOGGER.info("left: " + left);
            VaultMapper.LOGGER.info("right: " + right);
            VaultMapper.LOGGER.info("bottom: " + bottom);
            VaultMapper.LOGGER.info("width: " + width);
            VaultMapper.LOGGER.info("height: " + height);
        }


        @Override
        public void render(IElementRenderer renderer, @Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            super.render(renderer, poseStack, mouseX, mouseY, partialTick);

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            float h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            float windowWidth = this.width();

            float mapRoomWidth = (windowWidth / 49) * 2;

            // Tunnel map
            cells.stream().filter((cell) -> cell.cellType == CellType.TUNNEL_X || cell.cellType == CellType.TUNNEL_Z).forEach((cell) -> {
                renderCell(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)), (float) (w / 2 + window.mapCenterX), (float) (125 + window.mapCenterZ), mapRoomWidth);
            });

            // cell map
            cells.stream().filter((cell) -> cell.cellType == CellType.ROOM).forEach((cell) -> {
                renderCell(bufferBuilder, cell, parseColor(VaultMap.getCellColor(cell)), (float) (w / 2 + window.mapCenterX), (float) (125 + window.mapCenterZ), mapRoomWidth);
            });

            bufferBuilder.end();
            BufferUploader.end(bufferBuilder); // render the map

            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
        }

    }
}
