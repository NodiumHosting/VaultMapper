package com.nodiumhosting.vaultmapper;

import com.mojang.logging.LogUtils;
import com.nodiumhosting.vaultmapper.commands.VaultMapperCommand;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

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
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("hallo! (VaultMapper)");

    }

    @Mod.EventBusSubscriber(modid = VaultMapper.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ModEventListener {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event){
            VaultMapperCommand.register(event.getDispatcher());
            LOGGER.info("doin cmd stuff");
        }
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        //Mixins.addConfiguration("vaultmapper.mixins.json");
        VaultMapOverlayRenderer.prep();
        LOGGER.info("doin prep stuff");
    }
}
