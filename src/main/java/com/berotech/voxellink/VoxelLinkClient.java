package com.berotech.voxellink;

import com.berotech.voxellink.client.EditorBridgeService;
import com.berotech.voxellink.client.sfm.SFMManagerScreenHooks;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = VoxelLink.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = VoxelLink.MODID, value = Dist.CLIENT)
public class VoxelLinkClient {
    public VoxelLinkClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        VoxelLink.LOGGER.info("HELLO FROM CLIENT SETUP");
        VoxelLink.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(EditorBridgeService::startIfEnabled);
        NeoForge.EVENT_BUS.register(SFMManagerScreenHooks.class);
    }

    @SubscribeEvent
    static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(VoxelLink.MODID) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            EditorBridgeService.startIfEnabled();
        }
    }
}
