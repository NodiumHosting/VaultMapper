package com.nodiumhosting.vaultmapper.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nodiumhosting.vaultmapper.VaultMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMapOverlayRendererold {
    public static boolean enabled = false;

    static int mapStartX;
    static int mapStartY;
    static int mapRoomWidth;

    static boolean prepped = false;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Post event) {
        if (!enabled) return;
        if (!prepped) prep();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        VaultMapold.mapData.forEach((room) ->{
            var minX = Math.min(room.mapStartX, room.mapEndX);
            var maxX = Math.max(room.mapStartX, room.mapEndX);
            var minZ = Math.min(room.mapStartZ, room.mapEndZ);
            var maxZ = Math.max(room.mapStartZ, room.mapEndZ);
            var color = room.mapColor.getColor();
            bufferBuilder.vertex(minX, maxZ, 0).color(color).endVertex();
            bufferBuilder.vertex(maxX, maxZ, 0).color(color).endVertex();
            bufferBuilder.vertex(maxX, minZ, 0).color(color).endVertex();
            bufferBuilder.vertex(minX, minZ, 0).color(color).endVertex();
        });
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

    }

    public static void onWindowResize() {
        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int mapSize = (int) (w * 0.25f);
        mapStartX = w - mapSize;
        mapStartY = h - mapSize;

        mapRoomWidth = mapSize / 49;
    }

    public static void prep() {
        onWindowResize();
        VaultMapper.LOGGER.info("prep ran");
        prepped = true;
    }
}