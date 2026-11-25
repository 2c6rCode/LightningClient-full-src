/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package lightning.managers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderOverlayEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.impl.combat.AutoCrystalModule;
import lightning.modules.impl.core.RendersModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.S2CRenderPositionPacket;
import lightning.utils.IMinecraft;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.EspShader;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.WorldUtils;
import lightning.utils.miscellaneous.RenderPosition;
import lightning.utils.system.Counter;
import lightning.utils.system.MathUtils;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public class RenderManager
implements IMinecraft {
    public static final List<Runnable> POST_GUI_RENDERERS = new CopyOnWriteArrayList<Runnable>();
    private final Counter counter = new Counter();
    private int fps;
    public CopyOnWriteArrayList<RenderPosition> renderPositions = new CopyOnWriteArrayList();
    private Target crystalTarget;
    private BlockPos prevPosition = null;
    private Vec3 renderPosition = null;
    private long animationStart = 0L;

    public RenderManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderOverlayEvent renderOverlayEvent) {
        this.fps = this.counter.getCount();
        this.counter.increment();
    }

    @SubscribeEvent
    public void onRenderWorld$placePositions(RenderWorldEvent renderWorldEvent) {
        if (RenderManager.mc.player == null || RenderManager.mc.level == null || this.renderPositions.isEmpty()) {
            return;
        }
        RendersModule rendersModule = Night.MODULE_MANAGER.getModule(RendersModule.class);
        for (RenderPosition posEntry : this.renderPositions) {
            float progress = posEntry.get();
            AABB box = new AABB(posEntry.getPos());
            if (rendersModule.mode.getValue().equals("Shrink")) {
                box = new AABB(posEntry.getPos()).deflate(0.5).inflate(Mth.clamp((double)((double)progress / 2.0), (double)0.0, (double)0.5));
            }
            if (rendersModule.renderMode.getValue().equalsIgnoreCase("Fill") || rendersModule.renderMode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(renderWorldEvent.getMatrices(), box, rendersModule.getColor(rendersModule.mode.getValue(), rendersModule.fillColor.getColor(), progress));
            }
            if (!rendersModule.renderMode.getValue().equalsIgnoreCase("Outline") && !rendersModule.renderMode.getValue().equalsIgnoreCase("Both")) continue;
            Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), box, rendersModule.getColor(rendersModule.mode.getValue(), rendersModule.outlineColor.getColor(), progress));
        }
        this.renderPositions.removeIf(posEntry -> posEntry.get() <= 0.0f);
    }

    @SubscribeEvent
    public void onRenderWorld$autoCrystal(RenderWorldEvent renderWorldEvent) {
        if (RenderManager.mc.player == null || RenderManager.mc.level == null) {
            return;
        }
        if (this.crystalTarget == null || this.crystalTarget.getPosition() == null) {
            return;
        }
        AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        float fadeProgress = this.crystalTarget.getTarget() == 1 ? Easing.ease(Easing.toDelta(this.crystalTarget.getTime(), autoCrystalModule.duration.getValue().intValue()), Easing.Method.EASE_OUT_CUBIC) : 1.0f - Easing.ease(Easing.toDelta(this.crystalTarget.getTime(), autoCrystalModule.duration.getValue().intValue()), Easing.Method.EASE_IN_CUBIC);
        AABB box = new AABB(this.crystalTarget.getPosition());
        if (autoCrystalModule.mode.getValue().equals("Shrink")) {
            box = new AABB(this.crystalTarget.getPosition()).deflate(0.5).inflate(Mth.clamp((double)((double)fadeProgress / 2.0), (double)0.0, (double)0.5));
        }
        if (autoCrystalModule.animationMode.getValue().equals("Slide")) {
            if (this.renderPosition == null) {
                this.renderPosition = MathUtils.getVec(this.crystalTarget.getPosition());
            }
            if (!WorldUtils.equals(this.crystalTarget.getPosition(), this.prevPosition)) {
                this.animationStart = System.currentTimeMillis();
                this.prevPosition = this.crystalTarget.getPosition();
            }
            float slideFactor = Easing.ease(Easing.toDelta(this.animationStart, (int)(Math.pow(autoCrystalModule.slideSmoothness.getValue().doubleValue(), 1.4) * 1000.0)), Easing.Method.EASE_OUT_QUART);
            this.renderPosition = this.renderPosition.add(MathUtils.scale(MathUtils.getVec(this.crystalTarget.getPosition()).subtract(this.renderPosition), slideFactor));
            box = MathUtils.getBox(this.renderPosition);
            if (autoCrystalModule.mode.getValue().equals("Shrink")) {
                box = MathUtils.getBox(this.renderPosition).deflate(0.5).inflate(Mth.clamp((double)((double)fadeProgress / 2.0), (double)0.0, (double)0.5));
            }
        }
        RendersModule rendersModule = Night.MODULE_MANAGER.getModule(RendersModule.class);
        boolean renderFill = autoCrystalModule.renderMode.getValue().equalsIgnoreCase("Fill") || autoCrystalModule.renderMode.getValue().equalsIgnoreCase("Both");
        boolean renderOutline = autoCrystalModule.renderMode.getValue().equalsIgnoreCase("Outline") || autoCrystalModule.renderMode.getValue().equalsIgnoreCase("Both");
        int shaderMode = EspShader.modeIndex(autoCrystalModule.shader.getValue());
        List<Renderer3D.VertexCollection> quadBatches = shaderMode == 0 ? Renderer3D.QUADS : Renderer3D.SHADER_QUADS;
        List<Renderer3D.VertexCollection> lineBatches = shaderMode == 0 ? Renderer3D.DEBUG_LINES : Renderer3D.SHADER_DEBUG_LINES;
        if (shaderMode != 0) {
            float speed = autoCrystalModule.shaderSpeed.getValue().floatValue();
            float step = autoCrystalModule.shaderStep.getValue().floatValue();
            float time = (float)(System.currentTimeMillis() % 2000000L) / 1000.0f * speed * (shaderMode == 1 ? step : 1.0f);
            float distanceScale = autoCrystalModule.shaderDistanceScaling.getValue() ? (float)Math.sqrt(RenderManager.mc.player.distanceToSqr(box.getCenter())) : 1.0f;
            EspShader.setSettings(new EspShader.Settings(shaderMode, time, step, autoCrystalModule.shaderOpacity.getValue().floatValue() / 100.0f, distanceScale, shaderMode == 7 ? autoCrystalModule.shaderGlowColor.getColor() : autoCrystalModule.shaderColor1.getColor(), autoCrystalModule.shaderColor2.getColor(), autoCrystalModule.shaderColor3.getColor(), autoCrystalModule.shaderColor4.getColor()));
        }
        if (renderFill) {
            Renderer3D.renderGradientBox(quadBatches, renderWorldEvent.getMatrices(), box, rendersModule.getColor(autoCrystalModule.mode.getValue(), autoCrystalModule.fillColorUp.getColor(), fadeProgress), rendersModule.getColor(autoCrystalModule.mode.getValue(), autoCrystalModule.fillColorDown.getColor(), fadeProgress));
        }
        if (renderOutline) {
            Renderer3D.renderGradientBoxOutline(lineBatches, renderWorldEvent.getMatrices(), box, rendersModule.getColor(autoCrystalModule.mode.getValue(), autoCrystalModule.outlineColorUp.getColor(), fadeProgress), rendersModule.getColor(autoCrystalModule.mode.getValue(), autoCrystalModule.outlineColorDown.getColor(), fadeProgress));
        }
    }

    @SubscribeEvent
    public void onRenderWorld$autoCrystalExtra(RenderWorldEvent.Post post) {
        if (RenderManager.mc.player == null || RenderManager.mc.level == null) {
            return;
        }
        if (this.crystalTarget == null || this.crystalTarget.getPosition() == null) {
            return;
        }
        if (this.crystalTarget.getTarget() != 1) {
            return;
        }
        PoseStack poseStack = post.getMatrices();
        AutoCrystalModule autoCrystalModule = Night.MODULE_MANAGER.getModule(AutoCrystalModule.class);
        Vec3 centerPos = Vec3.atCenterOf((Vec3i)this.crystalTarget.getPosition());
        Vec3 relativePos = new Vec3(centerPos.x - RenderManager.mc.gameRenderer.mainCamera().position().x, centerPos.y - RenderManager.mc.gameRenderer.mainCamera().position().y, centerPos.z - RenderManager.mc.gameRenderer.mainCamera().position().z);
        if (autoCrystalModule.animationMode.getValue().equals("Slide")) {
            relativePos = new Vec3(this.renderPosition.x + 0.5 - RenderManager.mc.gameRenderer.mainCamera().position().x, this.renderPosition.y + 0.5 - RenderManager.mc.gameRenderer.mainCamera().position().y, this.renderPosition.z + 0.5 - RenderManager.mc.gameRenderer.mainCamera().position().z);
        }
        if (autoCrystalModule.icon.getValue()) {
            float iconScale = autoCrystalModule.iconScale.getValue().floatValue() / 100.0f;
            poseStack.pushPose();
            poseStack.translate(relativePos.x, relativePos.y, relativePos.z);
            poseStack.mulPose((Quaternionfc)RenderManager.mc.gameRenderer.mainCamera().rotation());
            poseStack.scale(iconScale, -iconScale, iconScale);
            Renderer2D.renderCircle(poseStack, 0.0f, 0.0f, 12.0f, new Color(0, 0, 0, 100));
            Renderer2D.renderCircle(poseStack, 0.0f, 0.0f, 12.0f - autoCrystalModule.iconRadius.getValue().floatValue(), autoCrystalModule.iconColor.getColor());
            if (autoCrystalModule.renderDamage.getValue()) {
                Renderer2D.renderTexture(poseStack, -5.5f, -8.5f, 5.5f, 2.5f, Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/crystal.png"), Color.WHITE);
                poseStack.pushPose();
                poseStack.scale(0.45f, 0.45f, 0.45f);
                String damageText = autoCrystalModule.getCalculationDamage();
                Night.FONT_MANAGER.drawTextWithShadow(poseStack, damageText, -Night.FONT_MANAGER.getWidth(damageText) / 2 - 1, 7, Color.WHITE);
                poseStack.popPose();
            } else {
                Renderer2D.renderTexture(poseStack, -6.5f, -6.5f, 6.5f, 6.5f, Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/crystal.png"), Color.WHITE);
            }
            poseStack.popPose();
        } else if (autoCrystalModule.renderDamage.getValue()) {
            poseStack.pushPose();
            poseStack.translate(relativePos.x, relativePos.y, relativePos.z);
            poseStack.mulPose((Quaternionfc)RenderManager.mc.gameRenderer.mainCamera().rotation());
            poseStack.scale(0.025f, -0.025f, 0.025f);
            String damageText = autoCrystalModule.getCalculationDamage();
            Night.FONT_MANAGER.drawTextWithShadow(poseStack, damageText, -Night.FONT_MANAGER.getWidth(damageText) / 2, -Night.FONT_MANAGER.getHeight() / 2, Color.WHITE);
            poseStack.popPose();
        }
    }

    public void setRenderPosition(BlockPos blockPos) {
        if (!Night.MODULE_MANAGER.getModule(AutoCrystalModule.class).isToggled()) {
            blockPos = null;
        }
        if (blockPos == null) {
            if (this.crystalTarget != null) {
                if (this.crystalTarget.getTarget() != 0) {
                    this.crystalTarget.setTarget(0);
                    this.crystalTarget.setTime(System.currentTimeMillis());
                }
            } else {
                this.crystalTarget = new Target(null, 0, System.currentTimeMillis());
            }
        } else if (this.crystalTarget == null || this.crystalTarget.getTarget() == 0) {
            this.crystalTarget = new Target(blockPos, 1, System.currentTimeMillis());
        } else {
            this.crystalTarget.setPosition(blockPos);
        }
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && Night.PINGBYPASS_CONFIG.isServer() && Night.PROXY_SERVER != null) {
            this.sendRenderPositionToClient(blockPos);
        }
    }

    private void sendRenderPositionToClient(BlockPos blockPos) {
        ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new ClientboundCustomPayloadPacket((CustomPacketPayload)PbCustomPayload.fromPacket(new S2CRenderPositionPacket(blockPos)));
        for (Connection connection : Night.PROXY_SERVER.getConnections()) {
            if (!connection.isConnected()) continue;
            connection.send((Packet)clientboundCustomPayloadPacket);
        }
    }

    @Generated
    public int getFps() {
        return this.fps;
    }

    @Generated
    public CopyOnWriteArrayList<RenderPosition> getRenderPositions() {
        return this.renderPositions;
    }

    public static class Target {
        private BlockPos position;
        private int target;
        private long time;

        @Generated
        public Target(BlockPos blockPos, int target, long time) {
            this.position = blockPos;
            this.target = target;
            this.time = time;
        }

        @Generated
        public BlockPos getPosition() {
            return this.position;
        }

        @Generated
        public int getTarget() {
            return this.target;
        }

        @Generated
        public long getTime() {
            return this.time;
        }

        @Generated
        public void setPosition(BlockPos blockPos) {
            this.position = blockPos;
        }

        @Generated
        public void setTarget(int target) {
            this.target = target;
        }

        @Generated
        public void setTime(long time) {
            this.time = time;
        }
    }
}

