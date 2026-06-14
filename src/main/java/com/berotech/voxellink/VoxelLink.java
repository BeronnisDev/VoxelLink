package com.berotech.voxellink;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.berotech.voxellink.network.BridgeNetworking;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(VoxelLink.MODID)
public class VoxelLink {
    public static final String MODID = "voxellink";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VoxelLink(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        BridgeNetworking.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("VoxelLink common setup complete");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("VoxelLink server starting");
    }
}
