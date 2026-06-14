package com.berotech.voxellink.client;

public record BridgeStatus(
        boolean enabled,
        boolean running,
        int port,
        boolean authRequired,
        int totalConnections,
        int authenticatedConnections,
        boolean preferLabelIds
) {
    public boolean isEditorLinked() {
        return enabled && running && authenticatedConnections > 0;
    }

    public String format() {
        return "enabled=" + enabled
                + ", running=" + running
                + ", port=" + port
                + ", authRequired=" + authRequired
                + ", connections=" + totalConnections
                + ", authenticated=" + authenticatedConnections
                + ", preferLabelIds=" + preferLabelIds;
    }
}
