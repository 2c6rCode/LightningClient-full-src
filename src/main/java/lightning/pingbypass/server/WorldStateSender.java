/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.Connection
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.server;

import lightning.pingbypass.server.WorldStateReplay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldStateSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldStateSender.class);

    private WorldStateSender() {
    }

    public static void sendWorld(ClientLevel clientLevel, LocalPlayer localPlayer, Connection connection) {
        LOGGER.info("Sending world state to reconnecting client...");
        try {
            if (clientLevel == null || localPlayer == null || connection == null) {
                LOGGER.warn("Cannot send world state: null parameter(s)");
                return;
            }
            WorldStateReplay.replay(connection, clientLevel.registryAccess());
            LOGGER.info("World state sent successfully.");
        }
        catch (Exception exception) {
            LOGGER.error("Error sending world state to client", (Throwable)exception);
        }
    }
}

