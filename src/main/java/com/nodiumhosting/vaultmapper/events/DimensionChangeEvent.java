package com.nodiumhosting.vaultmapper.events;

import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class DimensionChangeEvent {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(PlayerEvent.PlayerChangedDimensionEvent event) {
        // DOES NOT SEEM TO WORK ON SERVERS, HAVE TO USE SOME KIND OF NETWORKEVENT

        String dimensionNamespace = event.getTo().location().getNamespace(); // namespace

        VaultMap.resetMap();

        if (dimensionNamespace.equals("the_vault")) {
            VaultMap.enabled = true;
        }
        else {
            VaultMap.enabled = false;
            VaultMapOverlayRenderer.enabled = false;
        }
    }
}
