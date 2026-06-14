package com.berotech.voxellink.cc;

import com.berotech.voxellink.VoxelLink;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = VoxelLink.MODID)
public final class VoxelLinkCCRegistration {
    private VoxelLinkCCRegistration() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("computercraft")) {
            return;
        }

        event.enqueueWork(VoxelLinkCCRegistration::registerLuaApi);
    }

    private static void registerLuaApi() {
        try {
            Class<?> computerCraftApi = Class.forName("dan200.computercraft.api.ComputerCraftAPI");
            Class<?> luaComputerSystem = Class.forName("dan200.computercraft.api.lua.IComputerSystem");
            Class<?> luaApiClass = VoxelLinkLuaAPI.class;

            java.lang.reflect.Method registerFactory = computerCraftApi.getMethod(
                    "registerAPIFactory",
                    java.util.function.Function.class
            );
            registerFactory.invoke(null, (java.util.function.Function<Object, Object>) system -> {
                try {
                    return luaApiClass.getConstructor(luaComputerSystem).newInstance(system);
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException("Failed to create VoxelLink Lua API", exception);
                }
            });
            VoxelLink.LOGGER.info("Registered VoxelLink Lua API and vledit shell program");
        } catch (ReflectiveOperationException exception) {
            VoxelLink.LOGGER.error("Failed to register VoxelLink CC integration", exception);
        }
    }
}
