package com.berotech.cceb;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = CCEditorBridge.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Enable the localhost WebSocket bridge for external editors.")
            .define("enabled", false);

    public static final ModConfigSpec.IntValue SOCKET_PORT = BUILDER
            .comment("Port for the editor WebSocket server (localhost only).")
            .defineInRange("socketPort", 8765, 1024, 65535);

    public static final ModConfigSpec.ConfigValue<String> AUTH_TOKEN = BUILDER
            .comment("Optional auth token editors must send on connect. Leave empty to disable.")
            .define("authToken", "");

    public static final ModConfigSpec.BooleanValue SKIP_AUTH_FOR_DEV = BUILDER
            .comment("Skip auth checks during development even when authToken is set.")
            .define("skipAuthForDev", false);

    public static final ModConfigSpec.BooleanValue PREFER_LABEL_IDS = BUILDER
            .comment("When targeting a looked-at computer, prefer label:<name> over pos:<dimension>:x:y:z if the computer has a label set via os.setComputerLabel.")
            .define("preferLabelIds", true);

    public static final ModConfigSpec.IntValue MAX_OPERATIONS_PER_MINUTE = BUILDER
            .comment("Maximum file operations per editor connection per minute. Set to 0 to disable rate limiting.")
            .defineInRange("maxOperationsPerMinute", 120, 0, 10_000);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(CCEditorBridge.MODID) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            logConfig("loaded");
        }
    }

    @SubscribeEvent
    static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(CCEditorBridge.MODID) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
            logConfig("reloaded");
        }
    }

    private static void logConfig(String action) {
        CCEditorBridge.LOGGER.info(
                "Bridge config {}: enabled={}, port={}, authToken={}, skipAuthForDev={}, preferLabelIds={}, maxOperationsPerMinute={}",
                action,
                ENABLED.get(),
                SOCKET_PORT.get(),
                AUTH_TOKEN.get().isEmpty() ? "(none)" : "(set)",
                SKIP_AUTH_FOR_DEV.get(),
                PREFER_LABEL_IDS.get(),
                MAX_OPERATIONS_PER_MINUTE.get()
        );
    }
}
