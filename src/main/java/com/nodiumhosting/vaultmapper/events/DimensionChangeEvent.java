package com.nodiumhosting.vaultmapper.events;

import com.google.gson.Gson;
import com.nodiumhosting.vaultmapper.Snapshots.MapSnapshot;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class DimensionChangeEvent {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onDimChange(ClientPlayerNetworkEvent.RespawnEvent event) {
        String dimensionNamespace = event.getNewPlayer().level.dimension().location().getNamespace();

        MapSnapshot.lastSnapshotCache = MapSnapshot.takeSnapshot();
        VaultMap.resetMap();

        if (dimensionNamespace.equals("the_vault")) {
            VaultMap.enabled = true;
            VaultMapOverlayRenderer.enabled = true;

        }
        else {
                //exiting vault
                VaultMap.enabled = false;
                VaultMapOverlayRenderer.enabled = false;

                //map chat message, keeping here for debugging
                List<VaultCell> cells = VaultMap.getCells();
                List<VaultCell> inscriptionRooms = VaultMap.getInscriptionRooms();
                List<VaultCell> markedRooms = VaultMap.getMarkedRooms();

                //serialize cells
                Gson gson = new Gson();
                String cellsJson = gson.toJson(cells);
                String inscriptionRoomsJson = gson.toJson(inscriptionRooms);
                String markedRoomsJson = gson.toJson(markedRooms);

                String base64Cells = java.util.Base64.getEncoder().encodeToString(cellsJson.getBytes()).replaceAll("=", "-");
                String base64InscriptionRooms = java.util.Base64.getEncoder().encodeToString(inscriptionRoomsJson.getBytes()).replaceAll("=", "-");
                String base64MarkedRooms = java.util.Base64.getEncoder().encodeToString(markedRoomsJson.getBytes()).replaceAll("=", "-");

                TextComponent component = new TextComponent("[Open Vault Map]");
                component.withStyle(ChatFormatting.GRAY);
                component.withStyle(ChatFormatting.UNDERLINE);
                component.withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vaultmapper renderMap " + base64Cells + " " + base64InscriptionRooms + " " + base64MarkedRooms)));
                event.getNewPlayer().displayClientMessage(component, false);
            }
    }
}
