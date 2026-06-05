package com.berotech.cceb.cc;

import dan200.computercraft.api.ComputerCraftAPI;

import com.berotech.cceb.CCEditorBridge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = CCEditorBridge.MODID)
public final class CCEditorCCRegistration {
    private CCEditorCCRegistration() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> ComputerCraftAPI.registerAPIFactory(CCEditorLuaAPI::new));
        CCEditorBridge.LOGGER.info("Registered CC Editor Bridge Lua API and shell program");
    }
}
