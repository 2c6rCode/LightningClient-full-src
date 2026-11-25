/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.ClientWorldAccessor
 *  net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerGamePacketListener
 */
package lightning.utils.minecraft;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import lightning.mixins.accessors.ClientWorldAccessor;
import lightning.utils.IMinecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class NetworkUtils
implements IMinecraft {
    private static Object vfpImpl;
    private static Method vfpTargetVersion;
    private static Method protocolVersionGetVersion;

    public static void sendIgnoredPacket(Packet<?> packet) {
        mc.getConnection().getConnection().send(packet, null, true);
    }

    public static void sendSequencedPacket(SequencedPacketCreator sequencedPacketCreator) {
        NetworkUtils.sendSequencedPacket(sequencedPacketCreator, packet -> mc.getConnection().send(packet));
    }

    public static void sendSequencedPacket(SequencedPacketCreator sequencedPacketCreator, Consumer<Packet<ServerGamePacketListener>> consumer) {
        try (BlockStatePredictionHandler blockStatePredictionHandler = ((ClientWorldAccessor)NetworkUtils.mc.level).invokeGetPendingUpdateManager().startPredicting();){
            Packet<ServerGamePacketListener> packet = sequencedPacketCreator.predict(blockStatePredictionHandler.currentSequence());
            consumer.accept(packet);
        }
    }

    public static boolean isLegacyProtocol() {
        try {
            Object targetVersion;
            if (vfpImpl == null) {
                vfpImpl = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus").getMethod("getImpl", new Class[0]).invoke(null, new Object[0]);
                vfpTargetVersion = Class.forName("com.viaversion.viafabricplus.api.ViaFabricPlusBase").getMethod("getTargetVersion", new Class[0]);
                protocolVersionGetVersion = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion").getMethod("getVersion", new Class[0]);
            }
            return (targetVersion = vfpTargetVersion.invoke(vfpImpl, new Object[0])) != null && (Integer)protocolVersionGetVersion.invoke(targetVersion, new Object[0]) < 768;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static interface SequencedPacketCreator {
        public Packet<ServerGamePacketListener> predict(int sequence);
    }
}

