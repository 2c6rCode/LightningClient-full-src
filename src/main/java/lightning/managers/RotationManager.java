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

    public void legacyRotate(float[] rotations, int priority) {
        this.legacyRotate(rotations[0], rotations[1], priority);
    }

    public void legacyRotate(float yaw, float pitch, int priority) {
        this.legacyQueue.removeIf(legacyRotation -> legacyRotation.getModule() == null && legacyRotation.getPriority() == priority);
        this.legacyQueue.add(new LegacyRotation(yaw, pitch, priority));
    }

    public void legacyRotate(float[] rotations, Module module, int priority) {
        this.legacyRotate(rotations[0], rotations[1], module, priority);
    }

    public void legacyRotate(float yaw, float pitch, Module module, int priority) {
        this.legacyQueue.removeIf(legacyRotation -> legacyRotation.getModule() == module);
        this.legacyQueue.add(new LegacyRotation(yaw, pitch, module, priority));
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
        float forward = keyboardTickEvent.getMovementForward();
        float sideways = keyboardTickEvent.getMovementSideways();
        float deltaRad = (RotationManager.mc.player.getYRot() - this.rotation.getYaw()) * ((float)Math.PI / 180);
        float cos = Mth.cos((double)deltaRad);
        float sin = Mth.sin((double)deltaRad);
        keyboardTickEvent.setMovementForward(Math.round(forward * cos + sideways * sin));
        keyboardTickEvent.setMovementSideways(Math.round(sideways * cos - forward * sin));
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

    public void packetRotate(float[] rotations) {
        this.packetRotate(rotations[0], rotations[1]);
    }

    private float[] applyJitter(float yaw, float pitch) {
        if (pitch <= -89.5f || pitch >= 89.5f) {
            return new float[]{yaw, pitch};
        }
        String jitterMode = Night.MODULE_MANAGER.getModule(RotationsModule.class).jitter.getValue();
        if ("Grim".equalsIgnoreCase(jitterMode)) {
            float pitchOffset = (float)((Math.random() * 2.0 - 1.0) * (double)0.001f);
            pitch = Mth.clamp((float)(pitch + pitchOffset), (float)-90.0f, (float)90.0f);
        } else if ("Normal".equalsIgnoreCase(jitterMode)) {
            float minJitter = 0.15f;
            float maxJitter = 0.35f;
            float yawJitter = minJitter + (float)(Math.random() * (double)(maxJitter - minJitter));
            float pitchJitter = minJitter + (float)(Math.random() * (double)(maxJitter - minJitter));
            float pitchSign = Math.random() < 0.5 ? -1.0f : 1.0f;
            yaw += (yawJitter *= Math.random() < 0.5 ? -1.0f : 1.0f);
            pitch = Mth.clamp((float)(pitch + (pitchJitter *= pitchSign)), (float)-90.0f, (float)90.0f);
        }
        return new float[]{yaw, pitch};
    }

    public boolean isPacketRotateActive() {
        return System.currentTimeMillis() - this.lastPacketRotateTime < 500L;
    }

    public void packetRotate(float yaw, float pitch) {
        this.packetRotate(yaw, pitch, false);
    }

    public void packetRotate(float yaw, float pitch, boolean force) {
        if (!force && this.lastPacketTargetYaw == yaw && this.lastPacketTargetPitch == pitch) {
            return;
        }
        this.lastPacketTargetYaw = yaw;
        this.lastPacketTargetPitch = pitch;
        float[] jittered = this.applyJitter(yaw, pitch);
        float finalYaw = jittered[0];
        float finalPitch = jittered[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.serverYaw = finalYaw;
        this.serverPitch = finalPitch;
        boolean onGround = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(finalYaw, finalPitch, onGround, RotationManager.mc.player.horizontalCollision)));
    }

    public void silentRotate(float[] rotations) {
        this.silentRotate(rotations[0], rotations[1]);
    }

    public void beginBatchRotation() {
        this.batchingRotations = true;
        this.batchHasPending = false;
    }

    public void batchRotate(float yaw, float pitch) {
        this.batchYaw = yaw;
        this.batchPitch = pitch;
        this.batchHasPending = true;
        float[] jittered = this.applyJitter(yaw, pitch);
        this.serverYaw = jittered[0];
        this.serverPitch = jittered[1];
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

    public void silentRotate(float yaw, float pitch) {
        if (this.batchingRotations) {
            this.batchRotate(yaw, pitch);
            return;
        }
        boolean isServerProxy = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer();
        double posX = isServerProxy ? Night.POSITION_MANAGER.getServerX() : RotationManager.mc.player.getX();
        double posY = isServerProxy ? Night.POSITION_MANAGER.getServerY() : RotationManager.mc.player.getY();
        double posZ = isServerProxy ? Night.POSITION_MANAGER.getServerZ() : RotationManager.mc.player.getZ();
        boolean onGround = isServerProxy ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        if (this.lastSilentTargetYaw == yaw && this.lastSilentTargetPitch == pitch && this.lastSilentX == posX && this.lastSilentY == posY && this.lastSilentZ == posZ) {
            return;
        }
        this.lastSilentTargetYaw = yaw;
        this.lastSilentTargetPitch = pitch;
        this.lastSilentX = posX;
        this.lastSilentY = posY;
        this.lastSilentZ = posZ;
        float[] jittered = this.applyJitter(yaw, pitch);
        float finalYaw = jittered[0];
        float finalPitch = jittered[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.silentSyncRequired = true;
        this.serverYaw = finalYaw;
        this.serverPitch = finalPitch;
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.PosRot(posX, posY, posZ, finalYaw, finalPitch, onGround, RotationManager.mc.player.horizontalCollision)));
    }

    public void resetSilentRotation() {
        if (RotationManager.mc.player == null) {
            return;
        }
        float targetYaw = this.rotation != null ? this.rotation.getYaw() : RotationManager.mc.player.getYRot();
        float targetPitch = this.rotation != null ? this.rotation.getPitch() : RotationManager.mc.player.getXRot();
        if (this.serverYaw == targetYaw && this.serverPitch == targetPitch) {
            return;
        }
        this.silentRotate(targetYaw, targetPitch);
    }

    public void packetRotateRot(float[] rotations) {
        this.packetRotateRot(rotations[0], rotations[1]);
    }

    public void packetRotateRot(float yaw, float pitch) {
        if (this.lastPacketTargetYaw == yaw && this.lastPacketTargetPitch == pitch) {
            return;
        }
        this.lastPacketTargetYaw = yaw;
        this.lastPacketTargetPitch = pitch;
        float[] jittered = this.applyJitter(yaw, pitch);
        float finalYaw = jittered[0];
        float finalPitch = jittered[1];
        this.lastPacketRotateTime = System.currentTimeMillis();
        this.serverYaw = finalYaw;
        this.serverPitch = finalPitch;
        boolean onGround = PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() ? Night.POSITION_MANAGER.isServerOnGround() : RotationManager.mc.player.onGround();
        ProxyServerTickListener.allowSend(() -> mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(finalYaw, finalPitch, onGround, RotationManager.mc.player.horizontalCollision)));
    }

    public void wireRotate(String mode, float[] rotations) {
        if ("Silent".equalsIgnoreCase(mode)) {
            this.silentRotate(rotations);
        } else {
            this.packetRotate(rotations);
        }
    }

    public boolean inRenderTime() {
        return System.currentTimeMillis() - this.lastRenderTime < 1000L;
    }

    public float[] getRenderRotations() {
        float prevYawWrapped = MathUtils.wrapAngle(this.prevRenderYaw);
        float targetYawWrapped = MathUtils.wrapAngle(this.rotation == null ? RotationManager.mc.player.getYRot() : this.getServerYaw());
        float deltaYaw = targetYawWrapped - prevYawWrapped;
        if (deltaYaw > 180.0f) {
            deltaYaw -= 380.0f;
        } else if (deltaYaw < -180.0f) {
            deltaYaw += 360.0f;
        }
        float lerpedYaw = Mth.lerp((float)Easing.toDelta(this.lastRenderTime, 1000), (float)prevYawWrapped, (float)(prevYawWrapped + deltaYaw));
        float lerpedPitch = Mth.lerp((float)Easing.toDelta(this.lastRenderTime, 1000), (float)this.prevRenderPitch, (float)(this.rotation == null ? RotationManager.mc.player.getXRot() : this.getServerPitch()));
        this.prevRenderYaw = lerpedYaw;
        this.prevRenderPitch = lerpedPitch;
        return new float[]{lerpedYaw, lerpedPitch};
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
    public void setSilentSyncRequired(boolean silentSyncRequired) {
        this.silentSyncRequired = silentSyncRequired;
    }

    static {
        LEGACY_PRIORITIES.put("KillAura", 1);
        LEGACY_PRIORITIES.put("AutoCrystal", 2);
        LEGACY_PRIORITIES.put("SpeedMine", 3);
        LEGACY_PRIORITIES.put("Sprint", 4);
        LEGACY_PRIORITIES.put("SelfFill", 5);
    }
}

