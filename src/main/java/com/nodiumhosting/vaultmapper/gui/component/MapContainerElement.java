package com.nodiumhosting.vaultmapper.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.map.*;
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
import net.minecraft.resources.ResourceLocation;

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
    double zoomVal = 1;

    public MapContainerElement(ISpatial spatial, UUID vaultUuid) {
        super(spatial, Padding.of(2, 0));
        this.addElement(new MapElement(Spatials.positionY(3), vaultUuid, this)).postLayout((screen, gui, parent, world) -> {
            world.translateX((this.innerWidth() - world.width()) / 2);
            return true;
        });
    }

    private static void renderTextureCell(BufferBuilder bufferBuilder, VaultCell cell, float centerX, float centerZ, float width) {
        if (cell.cellType == CellType.ROOM) {
            if (cell.inscripted && !cell.explored && !ClientConfig.SHOW_INSCRIPTIONS.get()) return;
            float mapX = centerX + cell.x * (width / 2);
            float mapZ = centerZ + cell.z * (width / 2);
            float coordOffset = width / 2;
            float startX;
            float startZ;
            float endX;
            float endZ;

            startX = mapX - coordOffset;
            startZ = mapZ - coordOffset;
            endX = mapX + coordOffset;
            endZ = mapZ + coordOffset;

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
    public boolean onMouseScrolled(double mouseX, double mouseY, double delta) { // delta is +-1
        if (delta > 0) { // zoom in
            this.zoomVal += 0.1;
        } else if (delta < 0) { // zoom out
            this.zoomVal -= 0.1;
        }
        if (this.zoomVal <= 0.3) {
            this.zoomVal = 0.3;
        }

        return super.onMouseScrolled(mouseX, mouseY, delta);
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
            IMutableSpatial spatial = Spatials.positionXYZ(position);
            spatial.positionZ(10); // TRY JUST ONE
            Optional<MapSnapshot> optMap = MapSnapshot.from(vaultUuid);
            if (optMap.isEmpty()) {
                this.addElement(new LabelElement(spatial.positionX(0).positionY(5), new TextComponent("No map save available for this vault"), new LabelTextStyle.Builder()));
                return;
            }
            MapSnapshot map = optMap.get();

            cells = map.cells;
            int cellCount = cells.stream().filter(cell -> cell.cellType == CellType.ROOM && cell.explored).toArray().length;
            int inscriptionCount = cells.stream().filter(cell -> cell.inscripted).toArray().length;
            int markedCount = cells.stream().filter(cell -> cell.marked).toArray().length;
            int omegaRoomCount = cells.stream().filter(cell -> cell.roomType == RoomType.OMEGA).toArray().length;
            int challengeRoomCount = cells.stream().filter(cell -> cell.roomType == RoomType.CHALLENGE).toArray().length;

            // x was -35
            this.addElement(new LabelElement(spatial.positionX(-55).positionY(5), new TextComponent("Explored Rooms: " + cellCount), new LabelTextStyle.Builder()));
            this.addElement(new LabelElement(spatial.positionX(-55).positionY(15), new TextComponent("Inscription Rooms: " + inscriptionCount), new LabelTextStyle.Builder()));
            this.addElement(new LabelElement(spatial.positionX(-55).positionY(25), new TextComponent("Marked Rooms: " + markedCount), new LabelTextStyle.Builder()));
            this.addElement(new LabelElement(spatial.positionX(-55).positionY(35), new TextComponent("Omega Rooms: " + omegaRoomCount), new LabelTextStyle.Builder()));
            this.addElement(new LabelElement(spatial.positionX(-55).positionY(45), new TextComponent("Challenge Rooms: " + challengeRoomCount), new LabelTextStyle.Builder()));
        }


        @Override
        public void render(IElementRenderer renderer, @Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            super.render(renderer, poseStack, mouseX, mouseY, partialTick);
            if (cells == null) return;

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            RenderSystem.enableBlend();
            RenderSystem.disableTexture();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            float w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            float h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            float windowWidth = this.width();

            float mapRoomWidth = (windowWidth / 49) * 2 * (float) window.zoomVal;

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

            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            cells.stream().filter((cell) -> cell.cellType == CellType.ROOM).forEach((cell) -> {
                if (cell.roomName == null || cell.roomName == RoomName.UNKNOWN) return;

                String path = "/textures/icons/" + cell.roomName.getName().toLowerCase().replace(" ", "_").replace("-", "_") + ".png";
                ResourceLocation icon = new ResourceLocation("vaultmapper", path);
                RenderSystem.setShaderTexture(0, icon);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

                try {
                    renderTextureCell(bufferBuilder, cell, (float) (w / 2 + window.mapCenterX), (float) (125 + window.mapCenterZ), mapRoomWidth * 2);
                } catch (Exception e) {
                    VaultMapper.LOGGER.error("Failed to render icon for room: " + cell.roomName.getName());
                }
                bufferBuilder.end();
                BufferUploader.end(bufferBuilder);
            });
        }

    }
}
