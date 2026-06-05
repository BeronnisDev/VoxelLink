package com.berotech.cceb;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.berotech.cceb.network.BridgeNetworking;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(CCEditorBridge.MODID)
public class CCEditorBridge {
    public static final String MODID = "cceditorbridge";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CCEditorBridge(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        BridgeNetworking.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("CC Editor Bridge common setup complete");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("CC Editor Bridge server starting");
    }
}
