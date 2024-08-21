package com.nodiumhosting.vaultmapper.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class VaultMapOverlayRenderer {
    public static boolean enabled = false;

    static int mapStartX;
    static int mapStartY;
    static int mapRoomWidth;

    static boolean prepped = false;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Pre event) {
        if (!enabled) return;
        if (!prepped) prep();

        PoseStack poseStack = event.getMatrixStack();

        VaultMap.mapData.forEach((room) -> {
            GuiComponent.fill(poseStack, room.mapStartX, room.mapStartZ, room.mapEndX, room.mapEndZ, room.mapColor.getColor());
        });
    }

    public static void onWindowResize() {
        int w = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int h = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        int mapSize = (int) (w * 0.25f);
        mapStartX = w - mapSize;
        mapStartY = h - mapSize;

        mapRoomWidth = mapSize / 49;
    };

    public static void prep() {
        onWindowResize();
        prepped = true;
    }
}