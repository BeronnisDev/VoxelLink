package com.berotech.voxellink.client.sfm;

import java.lang.reflect.Field;

import com.berotech.voxellink.VoxelLink;
import com.berotech.voxellink.network.client.ClientPacketSender;
import com.berotech.voxellink.sfm.SFMProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class SFMManagerScreenHooks {
    private static final String MANAGER_SCREEN_CLASS = "ca.teamdman.sfm.client.screen.ManagerScreen";
    private static final String MANAGER_MENU_CLASS = "ca.teamdman.sfm.common.containermenu.ManagerContainerMenu";

    private SFMManagerScreenHooks() {}

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Pre event) {
        if (!ModList.get().isLoaded("sfm")) {
            return;
        }

        if (!MANAGER_SCREEN_CLASS.equals(event.getScreen().getClass().getName())) {
            return;
        }

        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        BlockPos managerPosition = getManagerPosition(screen.getMenu());
        if (managerPosition == null) {
            return;
        }

        int imageWidth = getImageDimension(screen, "imageWidth");
        int imageHeight = getImageDimension(screen, "imageHeight");
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        int buttonWidth = 120;
        int buttonHeight = 16;
        int x = (screen.width + imageWidth) / 2 + 8;
        int y = (screen.height - imageHeight) / 2 + 16 + 50;

        EditorLinkButton button = EditorLinkButton.create(
                x,
                y,
                buttonWidth,
                buttonHeight,
                Component.translatable("voxellink.sfm.open_in_editor"),
                ignored -> openInEditor(managerPosition),
                () -> hasDisk(screen.getMenu()) && !isReadOnly()
        );
        button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("voxellink.sfm.open_in_editor.tooltip")
        ));
        event.addListener(button);
    }

    private static void openInEditor(BlockPos managerPosition) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        String targetId = SFMProvider.encodeTargetId(minecraft.level, managerPosition);
        try {
            ClientPacketSender.sendOpenEditorRequest(targetId);
        } catch (IllegalStateException exception) {
            VoxelLink.LOGGER.debug("Cannot request external editor open while disconnected", exception);
        }
    }

    private static BlockPos getManagerPosition(AbstractContainerMenu menu) {
        if (!MANAGER_MENU_CLASS.equals(menu.getClass().getName())) {
            return null;
        }

        try {
            Field field = menu.getClass().getField("MANAGER_POSITION");
            return (BlockPos) field.get(menu);
        } catch (ReflectiveOperationException exception) {
            VoxelLink.LOGGER.warn("Failed to read SFM manager position from container menu", exception);
            return null;
        }
    }

    private static int getImageDimension(AbstractContainerScreen<?> screen, String fieldName) {
        try {
            Field field = AbstractContainerScreen.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(screen);
        } catch (ReflectiveOperationException exception) {
            VoxelLink.LOGGER.warn("Failed to read container screen dimension '{}'", fieldName, exception);
            return 0;
        }
    }

    private static boolean hasDisk(AbstractContainerMenu menu) {
        return menu.getSlot(0).hasItem();
    }

    private static boolean isReadOnly() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null || player.isSpectator();
    }
}
