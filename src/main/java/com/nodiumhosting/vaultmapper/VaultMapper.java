package com.nodiumhosting.vaultmapper;

import com.mojang.logging.LogUtils;
import com.nodiumhosting.vaultmapper.commands.VaultMapperCommand;
import com.nodiumhosting.vaultmapper.keybinds.MarkRoomKeybind;
import com.nodiumhosting.vaultmapper.keybinds.ToggleVaultMapKeybind;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRendererold;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("vaultmapper")
public class VaultMapper
{
    public static final String MODID = "vaultmapper";

    public static final Logger LOGGER = LogUtils.getLogger();

    public VaultMapper()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MarkRoomKeybind::register);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ToggleVaultMapKeybind::register);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("hallo! (VaultMapper)");

    }

    @Mod.EventBusSubscriber(modid = VaultMapper.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ModEventListener {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event){
            VaultMapperCommand.register(event.getDispatcher());
            LOGGER.info("registered client commands");
        }
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        //Mixins.addConfiguration("vaultmapper.mixins.json");
        VaultMapOverlayRenderer.prep();
        LOGGER.info("doin prep stuff");
    }
}
