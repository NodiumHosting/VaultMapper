package com.nodiumhosting.vaultmapper;

import com.mojang.logging.LogUtils;
import com.nodiumhosting.vaultmapper.commands.VaultMapperCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("vaultmapper")
public class VaultMapper
{
    public static final String MODID = "vaultmapper";

    private static final Logger LOGGER = LogUtils.getLogger();

    public VaultMapper()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
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
        }
    }
}
