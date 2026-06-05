package com.berotech.cceb.command;

import com.berotech.cceb.CCEditorBridge;
import com.berotech.cceb.network.client.ClientPacketSender;
import com.berotech.cceb.network.payload.FileListResponsePayload;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = CCEditorBridge.MODID, value = Dist.CLIENT)
public final class CCEditorCommands {
    private CCEditorCommands() {}

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("cceditor")
                .then(Commands.literal("testpacket")
                        .executes(context -> {
                            ClientPacketSender.sendTestFileListRequest().whenComplete((response, error) -> Minecraft.getInstance().execute(() -> {
                                if (error != null) {
                                    context.getSource().sendFailure(Component.literal("Packet test failed: " + error.getMessage()));
                                    return;
                                }
                                context.getSource().sendSuccess(
                                        () -> Component.literal("Packet test ok: " + formatFileListResponse(response)),
                                        false
                                );
                            }));
                            return 1;
                        }))
                .then(Commands.literal("testwrite")
                        .executes(context -> {
                            ClientPacketSender.sendFileWriteRequest("test-computer", "test.lua", "print('hi')")
                                    .whenComplete((response, error) -> Minecraft.getInstance().execute(() -> {
                                        if (error != null) {
                                            context.getSource().sendFailure(Component.literal("Write test failed: " + error.getMessage()));
                                            return;
                                        }
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Write test ok; file_modified event should reach connected editors"),
                                                false
                                        );
                                    }));
                            return 1;
                        }));

        event.getDispatcher().register(root);
    }

    private static String formatFileListResponse(FileListResponsePayload response) {
        if (!response.isSuccess()) {
            return "error=" + response.errorMessage();
        }
        return "files=" + response.files();
    }
}
