/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.EntityAccessor
 *  lombok.Generated
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$PosRot
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 */
package lightning.managers;

import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ClientRotationEvent;
import lightning.events.impl.KeyboardTickEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.events.impl.UpdateMovementEvent;
import lightning.events.impl.UpdateVelocityEvent;
import lightning.mixins.accessors.EntityAccessor;
import lightning.modules.Module;
import lightning.modules.impl.core.RotationsModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.utils.IMinecraft;
import lightning.utils.animations.Easing;
import lightning.utils.rotations.LegacyRotation;
import lightning.utils.rotations.Rotation;
import lightning.utils.system.MathUtils;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RotationManager
implements IMinecraft {
    private Rotation rotation = null;
    private Module rotationOwner = null;
    private float prevYaw;
    private float prevPitch;
    private float serverYaw;
    private float serverPitch;
    private float lastPacketTargetYaw = Float.NaN;
    private float lastPacketTargetPitch = Float.NaN;
    private float lastSilentTargetYaw = Float.NaN;
    private float lastSilentTargetPitch = Float.NaN;
    private double lastSilentX = Double.NaN;
    private double lastSilentY = Double.NaN;
    private double lastSilentZ = Double.NaN;
    private float prevRenderYaw;
    private float prevRenderPitch;
    private long lastRenderTime = 0L;
    private boolean batchingRotations = false;
    private boolean batchHasPending = false;
    private float batchYaw;
    private float batchPitch;
    public static boolean swapActive = false;
    private final PriorityBlockingQueue<LegacyRotation> legacyQueue = new PriorityBlockingQueue<>(11, RotationManager::compareLegacyRotations);
    private static final HashMap<String, Integer> LEGACY_PRIORITIES = new HashMap();
    private float prevFixYaw;
    private long lastPacketRotateTime = 0L;
    private boolean silentSyncRequired = false;

    public RotationManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent(priority=-2147483648)
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        if (RotationManager.mc.player == null) {
            return;
        }
        this.legacyQueue.removeIf(legacyRotation -> System.currentTimeMillis() - legacyRotation.getTime() > 100L);
        LegacyRotation legacyRotation2 = this.legacyQueue.peek();
        Rotation rotation = new Rotation((Entity)RotationManager.mc.player);
        ClientRotationEvent clientRotationEvent = new ClientRotationEvent(rotation);
        Night.EVENT_HANDLER.post(clientRotationEvent);
        int priority = clientRotationEvent.isCancelled() && clientRotationEvent.getOwner() != null ? this.getLegacyModulePriority(clientRotationEvent.getOwner()) : (clientRotationEvent.isCancelled() ? 0 : -1);
        if (legacyRotation2 != null && legacyRotation2.getPriority() > priority) {
            this.rotation = new Rotation(legacyRotation2.getYaw(), legacyRotation2.getPitch());
            this.rotationOwner = legacyRotation2.getModule();
            this.lastRenderTime = System.currentTimeMillis();
        } else if (clientRotationEvent.isCancelled()) {
            this.rotation = rotation;
            this.rotationOwner = clientRotationEvent.getOwner();
            this.lastRenderTime = System.currentTimeMillis();
        } else {
            this.rotation = null;
            this.rotationOwner = null;
        }
    }

    @SubscribeEvent(priority=0x7FFFFFFF)
    public void onUpdateMovement(UpdateMovementEvent updateMovementEvent) {
        if (this.rotation == null) {
            return;
        }
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        EntityAccessor entityAccessor = (EntityAccessor)RotationManager.mc.player;
        this.prevYaw = entityAccessor.getRawYRot();
        this.prevPitch = entityAccessor.getRawXRot();
        entityAccessor.setRawYRot(this.rotation.getYaw());
        entityAccessor.setRawXRot(this.rotation.getPitch());
        swapActive = true;
    }

    @SubscribeEvent(priority=-2147483648)
    public void onUpdateMovement$POST(UpdateMovementEvent.Post post) {
        if (this.rotation == null) {
            return;
        }
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer()) {
            return;
        }
        swapActive = false;
        EntityAccessor entityAccessor = (EntityAccessor)RotationManager.mc.player;
        entityAccessor.setRawYRot(this.prevYaw);
        entityAccessor.setRawXRot(this.prevPitch);
    }

    public int getLegacyModulePriority(Module module) {
        return LEGACY_PRIORITIES.getOrDefault(module.getName(), 0);
    }

    private static int compareLegacyRotations(LegacyRotation legacyRotation, LegacyRotation legacyRotation2) {
        if (legacyRotation.getPriority() == legacyRotation2.getPriority()) {
            return -Long.compare(legacyRotation.getTime(), legacyRotation2.getTime());
        }
        return -Integer.compare(legacyRotation.getPriority(), legacyRotation2.getPriority());
    }

    public void legacyRotate(float[] fArray, int n) {
        this.legacyRotate(fArray[0], fArray[1], n);
    }

    public void legacyRotate(float f, float f2, int n) {
        this.legacyQueue.removeIf(legacyRotation -> legacyRotation.getModule() == null && legacyRotation.getPriority() == n);
        this.legacyQueue.add(new LegacyRotation(f, f2, n));
    }

    public void legacyRotate(float[] fArray, Module module, int n) {
        this.legacyRotate(fArray[0], fArray[1], module, n);
    }

    public void legacyRotate(float f, float f2, Module module, int n) {
        this.legacyQueue.removeIf(legacyRotation -> legacyRotation.getModule() == module);
        this.legacyQueue.add(new LegacyRotation(f, f2, module, n));
    }

    @SubscribeEvent
    public void onUpdateVelocity(UpdateVelocityEvent updateVelocityEvent) {
        if (RotationManager.mc.player == null) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) {
            return;
        }
        if (this.rotation == null) {
            return;
        }
        updateVelocityEvent.setVelocity(EntityAccessor.invokeMovementInputToVelocity((Vec3)updateVelocityEvent.getMovementInput(), (float)updateVelocityEvent.getSpeed(), (float)this.rotation.getYaw()));
        updateVelocityEvent.setCancelled(true);
    }

    @SubscribeEvent
    public void onKeyboardTick(KeyboardTickEvent keyboardTickEvent) {
        if (RotationManager.mc.player == null || RotationManager.mc.level == null || RotationManager.mc.player.isPassenger()) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) {
            return;
        }
        if (this.rotation == null) {
            return;
        }
        float f = keyboardTickEvent.getMovementForward();
        float f2 = keyboardTickEvent.getMovementSideways();
        float f3 = (RotationManager.mc.player.getYRot() - this.rotation.getYaw()) * ((float)Math.PI / 180);
        float f4 = Mth.cos((double)f3);
        float f5 = Mth.sin((double)f3);
        keyboardTickEvent.setMovementForward(Math.round(f * f4 + f2 * f5));
        keyboardTickEvent.setMovementSideways(Math.round(f2 * f4 - f * f5));
        keyboardTickEvent.setCancelled(true);
    }

    @SubscribeEvent
    public void onPlayerJump(PlayerJumpEvent playerJumpEvent) {
        if (RotationManager.mc.player == null || RotationManager.mc.level == null || RotationManager.mc.player.isPassenger()) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) {
            return;
        }
        if (this.rotation == null) {
            return;
        }
        this.prevFixYaw = RotationManager.mc.player.getYRot();
        RotationManager.mc.player.setYRot(this.rotation.getYaw());
    }

    @SubscribeEvent
    public void onPlayerJump$POST(PlayerJumpEvent.Post post) {
        if (RotationManager.mc.player == null || RotationManager.mc.level == null || RotationManager.mc.player.isPassenger()) {
            return;
        }
        if (!Night.MODULE_MANAGER.getModule(RotationsModule.class).movementFix.getValue()) {
            return;
        }
        if (this.rotation == null) {
            return;
        }
        RotationManager.mc.player.setYRot(this.prevFixYaw);
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        if (RotationManager.mc.player == null) {
            return;
        }
        Packet<?> packet = packetSendEvent.getPacket();
        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket serverboundMovePlayerPacket = (ServerboundMovePlayerPacket)packet;
            if (!serverboundMovePlayerPacket.hasRotation()) {
                return;
            }
            this.serverYaw = serverboundMovePlayerPacket.getYRot(RotationManager.mc.player.getYRot());
            this.serverPitch = serverboundMovePlayerPacket.getXRot(RotationManager.mc.player.getXRot());
        }
    }

    public void packetRotate(float[] fArray) {
        this.packetRotate(fArray[0], fArray[1]);
    }

    private float[] applyJitter(float f, float f2) {
        if (f2 <= -89.5f || f2 >= 89.5f) {
            return new float[]{f, f2};
        }
        String string = Night.MODULE_MANAGER.getModule(RotationsModule.class).jitter.getValue();
        if ("Grim".equalsIgnoreCase(string)) {
            float f3 = (float)((Math.random() * 2.0 - 1.0) * (double)0.001f);
            f2 = Mth.clamp((float)(f2 + f3), (float)-90.0f, (float)90.0f);
        } else if ("Normal".equalsIgnoreCase(string)) {
            float f4 = 0.15f;
            float f5 = 0.35f;
            float f6 = f4 + (float)(Math.random() * (double)(f5 - f4));
            float f7 = f4 + (float)(Math.random() * (double)(f5 - f4));
            float f8 = Math.random() < 0.5 ? -1.0f : 1.0f;
            f += (f6 *= Math.random() < 0.5 ? -1.0f : 1.0f);
            f2 = Mth.clamp((float)(f2 + (f7 *= f8)), (float)-90.0f, (float)90.0f);
        }
        return new float[]{f, f2};
    }

    public boolean isPacketRotateActive() {
        return System.currentTimeMillis() - this.lastPacketRotateTime < 500L;
    }

    public void packetRotate(float f, float f2) {
        this.packetRotate(f, f2, false);
    }

    public void packetRotate(float f, float f2, boolean bl) {
        if (!bl && this.lastPacketTargetYaw == f && this.lastPacketTargetPitch == f2) {
            return;
        }
        this.lastPacketTargetYaw = f;
        this.lastPacketTargetPitch = f2;
        float[] fArray = this.applyJitter(f, f2);
        float f3 = fArray[0];
        float f4 = fArray[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.serverYaw = f3;
        this.serverPitch = f4;
        double d = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.getServerX() : RotationManager.mc.player.getX();
        double d2 = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.getServerY() : RotationManager.mc.player.getY();
        double d3 = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.getServerZ() : RotationManager.mc.player.getZ();
        boolean bl2 = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(f3, f4, bl2, RotationManager.mc.player.horizontalCollision)));
    }

    public void silentRotate(float[] fArray) {
        this.silentRotate(fArray[0], fArray[1]);
    }

    public void beginBatchRotation() {
        this.batchingRotations = true;
        this.batchHasPending = false;
    }

    public void batchRotate(float f, float f2) {
        this.batchYaw = f;
        this.batchPitch = f2;
        this.batchHasPending = true;
        float[] fArray = this.applyJitter(f, f2);
        this.serverYaw = fArray[0];
        this.serverPitch = fArray[1];
        this.silentSyncRequired = true;
    }

    public void endBatchRotation() {
        this.batchingRotations = false;
        if (!this.batchHasPending) {
            return;
        }
        this.batchHasPending = false;
        this.silentRotate(this.batchYaw, this.batchPitch);
    }

    public void silentRotate(float f, float f2) {
        if (this.batchingRotations) {
            this.batchRotate(f, f2);
            return;
        }
        boolean bl = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer();
        double d = bl ? Night.POSITION_MANAGER.getServerX() : RotationManager.mc.player.getX();
        double d2 = bl ? Night.POSITION_MANAGER.getServerY() : RotationManager.mc.player.getY();
        double d3 = bl ? Night.POSITION_MANAGER.getServerZ() : RotationManager.mc.player.getZ();
        boolean bl2 = bl ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        boolean bl3 = bl2;
        if (this.lastSilentTargetYaw == f && this.lastSilentTargetPitch == f2 && this.lastSilentX == d && this.lastSilentY == d2 && this.lastSilentZ == d3) {
            return;
        }
        this.lastSilentTargetYaw = f;
        this.lastSilentTargetPitch = f2;
        this.lastSilentX = d;
        this.lastSilentY = d2;
        this.lastSilentZ = d3;
        float[] fArray = this.applyJitter(f, f2);
        float f3 = fArray[0];
        float f4 = fArray[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.silentSyncRequired = true;
        this.serverYaw = f3;
        this.serverPitch = f4;
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(d, d2, d3, f3, f4, bl2, RotationManager.mc.player.horizontalCollision)));
    }

    public void resetSilentRotation() {
        if (RotationManager.mc.player == null) {
            return;
        }
        float f = this.rotation != null ? this.rotation.getYaw() : RotationManager.mc.player.getYRot();
        float f2 = this.rotation != null ? this.rotation.getPitch() : RotationManager.mc.player.getXRot();
        float f3 = f2;
        if (this.serverYaw == f && this.serverPitch == f2) {
            return;
        }
        this.silentRotate(f, f2);
    }

    public void packetRotateRot(float[] fArray) {
        this.packetRotateRot(fArray[0], fArray[1]);
    }

    public void packetRotateRot(float f, float f2) {
        if (this.lastPacketTargetYaw == f && this.lastPacketTargetPitch == f2) {
            return;
        }
        this.lastPacketTargetYaw = f;
        this.lastPacketTargetPitch = f2;
        float[] fArray = this.applyJitter(f, f2);
        float f3 = fArray[0];
        float f4 = fArray[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.serverYaw = f3;
        this.serverPitch = f4;
        boolean bl = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(f3, f4, bl, RotationManager.mc.player.horizontalCollision)));
    }

    public void wireRotate(String string, float[] fArray) {
        if ("Silent".equalsIgnoreCase(string)) {
            this.silentRotate(fArray);
        } else {
            this.packetRotate(fArray);
        }
    }

    public boolean inRenderTime() {
        return System.currentTimeMillis() - this.lastRenderTime < 1000L;
    }

    public float[] getRenderRotations() {
        float f = MathUtils.wrapAngle(this.prevRenderYaw);
        float f2 = MathUtils.wrapAngle(this.rotation == null ? RotationManager.mc.player.getYRot() : this.getServerYaw());
        float f3 = f2 - f;
        if (f3 > 180.0f) {
            f3 -= 380.0f;
        } else if (f3 < -180.0f) {
            f3 += 360.0f;
        }
        float f4 = Mth.lerp((float)Easing.toDelta(this.lastRenderTime, 1000), (float)f, (float)(f + f3));
        float f5 = Mth.lerp((float)Easing.toDelta(this.lastRenderTime, 1000), (float)this.prevRenderPitch, (float)(this.rotation == null ? RotationManager.mc.player.getXRot() : this.getServerPitch()));
        this.prevRenderYaw = f4;
        this.prevRenderPitch = f5;
        return new float[]{f4, f5};
    }

    @Generated
    public Rotation getRotation() {
        return this.rotation;
    }

    @Generated
    public Module getRotationOwner() {
        return this.rotationOwner;
    }

    @Generated
    public float getServerYaw() {
        return this.serverYaw;
    }

    @Generated
    public float getServerPitch() {
        return this.serverPitch;
    }

    @Generated
    public boolean isBatchingRotations() {
        return this.batchingRotations;
    }

    @Generated
    public boolean isSilentSyncRequired() {
        return this.silentSyncRequired;
    }

    @Generated
    public void setSilentSyncRequired(boolean bl) {
        this.silentSyncRequired = bl;
    }

    static {
        LEGACY_PRIORITIES.put("KillAura", 1);
        LEGACY_PRIORITIES.put("AutoCrystal", 2);
        LEGACY_PRIORITIES.put("SpeedMine", 3);
        LEGACY_PRIORITIES.put("Sprint", 4);
        LEGACY_PRIORITIES.put("SelfFill", 5);
    }
}

