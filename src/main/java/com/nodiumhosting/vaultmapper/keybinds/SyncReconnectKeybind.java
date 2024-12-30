package com.nodiumhosting.vaultmapper.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.gui.screen.VaultMapperConfigScreen;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = VaultMapper.MODID, value = Dist.CLIENT)
public class SyncReconnectKeybind {
    public static final KeyMapping syncReconnectKey = new KeyMapping(
            "key.vaultmapper.reconnectsync", // The translation key of the keybinding
            InputConstants.Type.KEYSYM, // Type of input, can be KEYSYM (keyboard), MOUSE (mouse), or SCANCODE
            GLFW.GLFW_KEY_UNKNOWN, // The default key for the keybinding
            "key.categories.vaultmapper" // The translation key of the category for the keybinding
    );

    public static void register(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(syncReconnectKey);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (syncReconnectKey.consumeClick()) {
                if (!VaultMap.enabled) return;
                Player player = Minecraft.getInstance().player;
                if (player == null) return;
                VaultMap.stopSync();
                VaultMap.startSync(player.getUUID().toString(), player.level.dimension().location().getPath());
                player.sendMessage(new TextComponent("VMSync Reconnected"), player.getUUID());
            }
        }
    }
}