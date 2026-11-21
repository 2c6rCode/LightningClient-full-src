/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  lightning.mixins.accessors.LimbAnimatorAccessor
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package lightning.modules.impl.visuals;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerPopEvent;
import lightning.events.impl.TickEvent;
import lightning.mixins.accessors.LimbAnimatorAccessor;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@RegisterModule(name="PopChams", description="Renders chams on a frozen snapshot of the entity's pose the moment they pop a totem.", category=Module.Category.VISUALS)
public class PopChamsModule
extends Module {
    public NumberSetting duration = new NumberSetting("Duration", "The duration for the pop chams fade.", 1500, 0, 5000);
    public NumberSetting yOffset = new NumberSetting("YOffset", "The vertical float/rise offset for pop chams.", 0, -5, 5);
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the pop chams.", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    private static final EquipmentSlot[] COPIED_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Map<RemotePlayer, Long> ghosts = new ConcurrentHashMap<RemotePlayer, Long>();
    private int nextId = -90000;

    @SubscribeEvent
    public void onPlayerPop(PlayerPopEvent playerPopEvent) {
        if (!this.isToggled() || PopChamsModule.mc.level == null || playerPopEvent.getPlayer() == PopChamsModule.mc.player) {
            return;
        }
        Player player = playerPopEvent.getPlayer();
        mc.execute(() -> {
            if (PopChamsModule.mc.level == null) {
                return;
            }
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
            RemotePlayer remotePlayer = new RemotePlayer(PopChamsModule.mc.level, gameProfile);
            remotePlayer.setId(this.nextId--);
            remotePlayer.setPos(player.getX(), player.getY(), player.getZ());
            remotePlayer.xo = player.getX();
            remotePlayer.yo = player.getY();
            remotePlayer.zo = player.getZ();
            remotePlayer.xOld = player.getX();
            remotePlayer.yOld = player.getY();
            remotePlayer.zOld = player.getZ();
            remotePlayer.setYRot(player.getYRot());
            remotePlayer.setXRot(player.getXRot());
            remotePlayer.yRotO = player.getYRot();
            remotePlayer.xRotO = player.getXRot();
            remotePlayer.setYHeadRot(player.getYHeadRot());
            remotePlayer.yHeadRotO = player.getYHeadRot();
            remotePlayer.yBodyRot = player.yBodyRot;
            remotePlayer.yBodyRotO = player.yBodyRot;
            remotePlayer.setPose(player.getPose());
            remotePlayer.setShiftKeyDown(player.isShiftKeyDown());
            remotePlayer.setSwimming(player.isSwimming());
            remotePlayer.setSprinting(player.isSprinting());
            remotePlayer.refreshDimensions();
            remotePlayer.walkAnimation.setSpeed(player.walkAnimation.speed());
            ((LimbAnimatorAccessor)remotePlayer.walkAnimation).setPos(player.walkAnimation.position());
            remotePlayer.swinging = player.swinging;
            remotePlayer.swingTime = player.swingTime;
            remotePlayer.swingingArm = player.swingingArm;
            remotePlayer.attackAnim = player.attackAnim;
            remotePlayer.oAttackAnim = player.oAttackAnim;
            remotePlayer.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            remotePlayer.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            for (EquipmentSlot equipmentSlot : COPIED_SLOTS) {
                remotePlayer.setItemSlot(equipmentSlot, player.getItemBySlot(equipmentSlot).copy());
            }
            PopChamsModule.mc.level.addEntity((Entity)remotePlayer);
            this.ghosts.put(remotePlayer, System.currentTimeMillis());
        });
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        long l = System.currentTimeMillis();
        this.ghosts.entrySet().removeIf(entry -> {
            if (l - (Long)entry.getValue() <= (long)this.duration.getValue().intValue()) {
                return false;
            }
            this.despawn((RemotePlayer)entry.getKey());
            return true;
        });
    }

    @Override
    public void onDisable() {
        this.ghosts.keySet().forEach(this::despawn);
        this.ghosts.clear();
    }

    private void despawn(RemotePlayer remotePlayer) {
        if (PopChamsModule.mc.level != null) {
            PopChamsModule.mc.level.removeEntity(remotePlayer.getId(), Entity.RemovalReason.DISCARDED);
        }
    }

    public boolean isGhost(Entity entity) {
        return this.ghosts.containsKey(entity);
    }

    public boolean shouldFill() {
        return this.mode.getValue().equals("Fill") || this.mode.getValue().equals("Both");
    }

    public boolean shouldOutline() {
        return this.mode.getValue().equals("Outline") || this.mode.getValue().equals("Both");
    }

    public Color getFillColor(RemotePlayer remotePlayer) {
        return this.withFade(this.fillColor.getColor(), remotePlayer);
    }

    public Color getOutlineColor(RemotePlayer remotePlayer) {
        return this.withFade(this.outlineColor.getColor(), remotePlayer);
    }

    public float getYOffset(RemotePlayer remotePlayer) {
        if (this.yOffset.getValue().intValue() == 0) {
            return 0.0f;
        }
        Long l = this.ghosts.get(remotePlayer);
        if (l == null) {
            return 0.0f;
        }
        int n = this.duration.getValue().intValue();
        if (n <= 0) {
            return 0.0f;
        }
        float f = Easing.toDelta(l, n);
        return this.yOffset.getValue().floatValue() * f;
    }

    private Color withFade(Color color, RemotePlayer remotePlayer) {
        Long l = this.ghosts.get(remotePlayer);
        if (l == null) {
            return color;
        }
        float f = 1.0f - Easing.toDelta(l, this.duration.getValue().intValue());
        return ColorUtils.getColor(color, (int)((float)color.getAlpha() * f));
    }
}

