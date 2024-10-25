package com.nodiumhosting.vaultmapper;

import com.mojang.logging.LogUtils;
import com.nodiumhosting.vaultmapper.commands.VaultMapperCommand;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.keybinds.MarkRoomKeybind;
import com.nodiumhosting.vaultmapper.keybinds.OpenConfigScreenKeybind;
import com.nodiumhosting.vaultmapper.keybinds.ToggleVaultMapKeybind;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import com.nodiumhosting.vaultmapper.network.handlers.AuthNetworkingHandler;
import com.nodiumhosting.vaultmapper.roomdetection.RoomData;
import com.nodiumhosting.vaultmapper.webmap.SocketServer;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.net.InetSocketAddress;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("vaultmapper")
public class VaultMapper {
    public static final String MODID = "vaultmapper";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static SocketServer wsServer;

    public VaultMapper() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(MarkRoomKeybind::register);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(OpenConfigScreenKeybind::register);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ToggleVaultMapKeybind::register);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, MODID + "-client.toml");
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("hallo! (VaultMapper)");

    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        VaultMapOverlayRenderer.prep();
        VaultMapOverlayRenderer.ignoreResearchRequirement = ClientConfig.IGNORE_RESEARCH_REQUIREMENT.get();

        AuthNetworkingHandler.register();

        InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 58008);
        wsServer = new SocketServer(addr);

        if (ClientConfig.WEBMAP_ENABLED.get()) {
            wsServer.start();
        }
        if (RoomData.omegaRooms == null || RoomData.challengeRooms == null) {
            RoomData.initRooms();
        }
    }

    @Mod.EventBusSubscriber(modid = VaultMapper.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ModEventListener {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            VaultMapperCommand.register(event.getDispatcher());
            LOGGER.info("registered client commands");
        }
    }

    public static String getVersion() {
        return ModList.get()
                .getModContainerById(VaultMapper.MODID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("Unknown");
    }
}
