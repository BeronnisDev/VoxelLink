package com.berotech.voxellink.client.sfm;

import java.util.function.BooleanSupplier;

import com.berotech.voxellink.client.BridgeStatus;
import com.berotech.voxellink.client.EditorBridgeService;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class EditorLinkButton extends Button {
    private static final int BLIP_SIZE = 4;
    private static final int BLIP_MARGIN = 2;

    private final BooleanSupplier visibleWhen;

    private EditorLinkButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            OnPress onPress,
            BooleanSupplier visibleWhen
    ) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.visibleWhen = visibleWhen;
    }

    public static EditorLinkButton create(
            int x,
            int y,
            int width,
            int height,
            Component message,
            OnPress onPress,
            BooleanSupplier visibleWhen
    ) {
        return new EditorLinkButton(x, y, width, height, message, onPress, visibleWhen);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.visible = visibleWhen.getAsBoolean();
        if (!this.visible) {
            return;
        }

        BridgeStatus status = EditorBridgeService.status();
        this.active = status.isEditorLinked();
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        drawStatusBlip(graphics, status);
    }

    private void drawStatusBlip(GuiGraphics graphics, BridgeStatus status) {
        int color = blipColor(status);
        int blipX = getX() + getWidth() - BLIP_SIZE - BLIP_MARGIN;
        int blipY = getY() + BLIP_MARGIN;
        graphics.fill(blipX, blipY, blipX + BLIP_SIZE, blipY + BLIP_SIZE, color);
    }

    private static int blipColor(BridgeStatus status) {
        if (!status.enabled() || !status.running()) {
            return 0xFF_66_66_66;
        }
        if (status.authenticatedConnections() > 0) {
            return 0xFF_55_FF_55;
        }
        if (status.totalConnections() > 0) {
            return 0xFF_FF_AA_00;
        }
        return 0xFF_FF_FF_55;
    }
}
