package com.berotech.voxellink.command;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.berotech.voxellink.VoxelLink;
import com.berotech.voxellink.client.BridgeStatus;
import com.berotech.voxellink.client.EditorBridgeService;
import com.berotech.voxellink.client.TargetTargeting;
import com.berotech.voxellink.cc.LabelComputerReference;
import com.berotech.voxellink.network.client.ClientPacketSender;
import com.berotech.voxellink.network.payload.FileListResponsePayload;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = VoxelLink.MODID, value = Dist.CLIENT)
public final class VoxelLinkCommands {
    private static final int PORT_TEST_TIMEOUT_MS = 1000;

    private VoxelLinkCommands() {}

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("voxellink")
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context)))
                .then(Commands.literal("test")
                        .executes(context -> runSelfTest(context)))
                .then(Commands.literal("reload")
                        .executes(context -> reloadConfig(context)))
                .then(Commands.literal("id")
                        .executes(context -> showTargetId(context)))
                .then(Commands.literal("list")
                        .executes(context -> listFiles(context, TargetTargeting.getTargetId(), "/"))
                        .then(Commands.argument("target", StringArgumentType.greedyString())
                                .executes(context -> listFiles(
                                        context,
                                        StringArgumentType.getString(context, "target"),
                                        "/"
                                ))))
                .then(Commands.literal("testpacket")
                        .executes(context -> {
                            String targetId = TargetTargeting.getTargetId();
                            if (targetId == null) {
                                context.getSource().sendFailure(Component.literal("Look at a supported target, then run /voxellink testpacket"));
                                return 0;
                            }
                            return listFiles(context, targetId, "/");
                        }))
                .then(Commands.literal("testwrite")
                        .executes(context -> {
                            String targetId = TargetTargeting.getTargetId();
                            if (targetId == null) {
                                context.getSource().sendFailure(Component.literal("Look at a CC computer, then run /voxellink testwrite"));
                                return 0;
                            }

                            TargetTargeting.Target target = TargetTargeting.getTarget();
                            String testPath = "cc".equals(target.backend()) ? "voxellink_test.lua" : "program.sfml";
                            String testContent = "cc".equals(target.backend())
                                    ? "print('hi from voxellink')"
                                    : "-- voxellink test\n";

                            ClientPacketSender.sendFileWriteRequest(targetId, testPath, testContent)
                                    .whenComplete((response, error) -> Minecraft.getInstance().execute(() -> {
                                        if (error != null) {
                                            context.getSource().sendFailure(Component.literal("Write test failed: " + error.getMessage()));
                                            return;
                                        }
                                        if (!response.isSuccess()) {
                                            context.getSource().sendFailure(Component.literal("Write test failed: " + response.errorMessage()));
                                            return;
                                        }
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Write test ok on " + targetId + "; file_modified event should reach connected editors"),
                                                false
                                        );
                                    }));
                            return 1;
                        }));

        event.getDispatcher().register(root);
    }

    private static int showStatus(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        BridgeStatus status = EditorBridgeService.status();
        String address = "ws://127.0.0.1:" + status.port() + "/";
        context.getSource().sendSuccess(
                () -> Component.literal("Bridge status: " + status.format() + " | address=" + address),
                false
        );
        return 1;
    }

    private static int runSelfTest(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        BridgeStatus status = EditorBridgeService.status();
        if (!status.enabled()) {
            context.getSource().sendFailure(Component.literal("Bridge disabled in config (enabled=false)"));
            return 0;
        }
        if (!status.running()) {
            context.getSource().sendFailure(Component.literal("Bridge enabled but WebSocket server is not running"));
            return 0;
        }
        if (!isPortOpen(status.port())) {
            context.getSource().sendFailure(Component.literal("Bridge reports running but localhost:" + status.port() + " is not accepting connections"));
            return 0;
        }

        if (Minecraft.getInstance().getConnection() == null) {
            context.getSource().sendSuccess(
                    () -> Component.literal("Bridge self-test ok: WebSocket listening on ws://127.0.0.1:" + status.port() + "/ (not in world for packet test)"),
                    false
            );
            return 1;
        }

        ClientPacketSender.sendFileListRequest("cc:pos:minecraft:overworld:0:0:0", "/")
                .whenComplete((response, error) -> Minecraft.getInstance().execute(() -> {
                    if (error != null) {
                        context.getSource().sendSuccess(
                                () -> Component.literal("Bridge self-test ok: WebSocket listening; packet path reachable (server responded)"),
                                false
                        );
                        return;
                    }
                    context.getSource().sendSuccess(
                            () -> Component.literal("Bridge self-test ok: WebSocket listening; packet round-trip succeeded"),
                            false
                    );
                }));
        return 1;
    }

    private static int reloadConfig(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());

        BridgeStatus status = EditorBridgeService.status();
        context.getSource().sendSuccess(
                () -> Component.literal("Reloaded client configs from disk. Bridge: " + status.format()),
                false
        );
        return 1;
    }

    private static boolean isPortOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), PORT_TEST_TIMEOUT_MS);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static int showTargetId(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
        TargetTargeting.Target target = TargetTargeting.getTarget();
        if (target == null) {
            context.getSource().sendFailure(Component.literal("Look at a CC computer or SFM manager first"));
            return 0;
        }

        StringBuilder message = new StringBuilder("Using id: " + target.id());
        message.append(" | backend: ").append(target.backend());
        message.append(" | kind: ").append(target.kind());
        message.append(" | position: ").append(target.positionId());
        if ("cc".equals(target.backend())) {
            if (target.label() != null && !target.label().isBlank()) {
                message.append(" | label: ").append(LabelComputerReference.of(target.label()).encode());
            } else {
                message.append(" | label: (none — set with os.setComputerLabel(\"name\"))");
            }
        }
        context.getSource().sendSuccess(() -> Component.literal(message.toString()), false);
        return 1;
    }

    private static int listFiles(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, String targetId, String path) {
        if (targetId == null || targetId.isBlank()) {
            context.getSource().sendFailure(Component.literal("Look at a supported target or pass cc:pos:..., cc:label:..., or sfm:pos:..."));
            return 0;
        }

        ClientPacketSender.sendFileListRequest(targetId, path).whenComplete((response, error) -> Minecraft.getInstance().execute(() -> {
            if (error != null) {
                context.getSource().sendFailure(Component.literal("List failed: " + error.getMessage()));
                return;
            }
            if (!response.isSuccess()) {
                context.getSource().sendFailure(Component.literal("List failed: " + response.errorMessage()));
                return;
            }
            context.getSource().sendSuccess(
                    () -> Component.literal("Files on " + targetId + ": " + formatFileListResponse(response)),
                    false
            );
        }));
        return 1;
    }

    private static String formatFileListResponse(FileListResponsePayload response) {
        if (!response.isSuccess()) {
            return "error=" + response.errorMessage();
        }
        return "files=" + response.files();
    }
}
