package com.berotech.cceb;

import com.berotech.cceb.client.EditorBridgeService;

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

@Mod(value = CCEditorBridge.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CCEditorBridge.MODID, value = Dist.CLIENT)
public class CCEditorBridgeClient {
    public CCEditorBridgeClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CCEditorBridge.LOGGER.info("HELLO FROM CLIENT SETUP");
        CCEditorBridge.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(EditorBridgeService::startIfEnabled);
    }

    @SubscribeEvent
    static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(CCEditorBridge.MODID) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            EditorBridgeService.startIfEnabled();
        }
    }
}
