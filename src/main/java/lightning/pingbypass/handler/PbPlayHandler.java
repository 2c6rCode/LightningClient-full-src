/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  lightning.mixins.accessors.ClientPlayerEntityAccessor
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.Connection
 *  net.minecraft.network.DisconnectionDetails
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.HashedStack
 *  net.minecraft.network.TickablePacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundClientInformationPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket
 *  net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
 *  net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
 *  net.minecraft.network.protocol.common.ServerboundPongPacket
 *  net.minecraft.network.protocol.common.ServerboundResourcePackPacket
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket
 *  net.minecraft.network.protocol.game.ServerGamePacketListener
 *  net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
 *  net.minecraft.network.protocol.game.ServerboundAttackPacket
 *  net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket
 *  net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket
 *  net.minecraft.network.protocol.game.ServerboundChangeGameModePacket
 *  net.minecraft.network.protocol.game.ServerboundChatAckPacket
 *  net.minecraft.network.protocol.game.ServerboundChatCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket
 *  net.minecraft.network.protocol.game.ServerboundChatPacket
 *  net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket
 *  net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket
 *  net.minecraft.network.protocol.game.ServerboundClientCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundClientTickEndPacket
 *  net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket
 *  net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClickPacket
 *  net.minecraft.network.protocol.game.ServerboundContainerClosePacket
 *  net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket
 *  net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket
 *  net.minecraft.network.protocol.game.ServerboundEditBookPacket
 *  net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket
 *  net.minecraft.network.protocol.game.ServerboundInteractPacket
 *  net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket
 *  net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket
 *  net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket
 *  net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerInputPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket
 *  net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket
 *  net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket
 *  net.minecraft.network.protocol.game.ServerboundRenameItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket
 *  net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSelectTradePacket
 *  net.minecraft.network.protocol.game.ServerboundSetBeaconPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket
 *  net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
 *  net.minecraft.network.protocol.game.ServerboundSetGameRulePacket
 *  net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket
 *  net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
 *  net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket
 *  net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.network.protocol.ping.ServerboundPingRequestPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass.handler;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.awt.Color;
import lightning.Night;
import lightning.events.impl.AttackBlockEvent;
import lightning.events.impl.PlayerJumpEvent;
import lightning.mixins.accessors.ClientPlayerEntityAccessor;
import lightning.modules.Module;
import lightning.modules.impl.player.SpeedMineModule;
import lightning.pingbypass.PingBypassFlags;
import lightning.pingbypass.modules.PbModule;
import lightning.pingbypass.modules.SyncModule;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.protocol.packets.C2SFriendSyncPacket;
import lightning.pingbypass.protocol.packets.C2SModuleTogglePacket;
import lightning.pingbypass.protocol.packets.C2SOpenInventoryPacket;
import lightning.pingbypass.protocol.packets.C2SSettingChangePacket;
import lightning.pingbypass.server.ProxyServer;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.pingbypass.server.S2CForwarder;
import lightning.settings.Setting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.HashedStack;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetGameRulePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSpectatorActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PbPlayHandler
implements ServerGamePacketListener,
TickablePacketListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PbPlayHandler.class);
    private static final long KEEP_ALIVE_INTERVAL_MS = 15000L;
    private final ProxyServer proxyServer;
    private final Connection clientConnection;
    private final GameProfile profile;
    private final S2CForwarder s2cForwarder;
    private boolean wasOnGround = true;
    private long lastKeepAliveSent;

    public PbPlayHandler(ProxyServer proxyServer, Connection connection, GameProfile gameProfile, S2CForwarder s2CForwarder, int teleportId) {
        this.proxyServer = proxyServer;
        this.clientConnection = connection;
        this.profile = gameProfile;
        this.s2cForwarder = s2CForwarder;
        LOGGER.info("Play handler initialized for {} \u2014 dumb pipe mode", (Object)gameProfile.name());
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (now - this.lastKeepAliveSent >= 15000L) {
            this.lastKeepAliveSent = now;
            this.clientConnection.send((Packet)new ClientboundKeepAlivePacket(now));
        }
    }

    public void onDisconnect(DisconnectionDetails disconnectionDetails) {
        LOGGER.info("Client {} disconnected: {}", (Object)this.profile.name(), (Object)disconnectionDetails.reason());
        this.s2cForwarder.stop();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.level != null) {
                minecraft.level.disconnect((Component)Component.literal((String)"Client disconnected"));
            }
            if (minecraft.getConnection() != null) {
                minecraft.getConnection().getConnection().disconnect((Component)Component.literal((String)"Client disconnected"));
            }
        });
    }

    public boolean isAcceptingMessages() {
        return this.clientConnection.isConnected();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void syncSlotForInteract() {
        Minecraft minecraft;
        InventoryUtils.flushPendingRestores();
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        if (speedMineModule != null && speedMineModule.isToggled() && speedMineModule.isRunningOnProxy()) {
            minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                synchronized (speedMineModule.interactSyncLock) {
                    int selectedSlot = minecraft.player.getInventory().getSelectedSlot();
                    this.forward((Packet<?>)new ServerboundSetCarriedItemPacket(selectedSlot));
                    speedMineModule.setInteractPaused(true);
                    return;
                }
            }
        }
        minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        int selectedSlot = minecraft.player.getInventory().getSelectedSlot();
        if (selectedSlot != Night.POSITION_MANAGER.getServerSlot()) {
            this.forward((Packet<?>)new ServerboundSetCarriedItemPacket(selectedSlot));
        }
    }

    private void forward(Packet<?> packet) {
        Connection connection = this.proxyServer.getServerConnection();
        if (connection != null && connection.isConnected()) {
            ProxyServerTickListener.allowSend(() -> connection.send(packet));
        }
    }

    public void handleMovePlayer(ServerboundMovePlayerPacket serverboundMovePlayerPacket) {
        Minecraft.getInstance().execute(() -> this.handleMovePlayer0(serverboundMovePlayerPacket));
    }

    private void handleMovePlayer0(ServerboundMovePlayerPacket serverboundMovePlayerPacket) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            double previousY = minecraft.player.getY();
            if (serverboundMovePlayerPacket.hasPosition()) {
                minecraft.player.setPos(serverboundMovePlayerPacket.getX(minecraft.player.getX()), serverboundMovePlayerPacket.getY(minecraft.player.getY()), serverboundMovePlayerPacket.getZ(minecraft.player.getZ()));
                double currentY = minecraft.player.getY();
                if (currentY < previousY && !serverboundMovePlayerPacket.isOnGround()) {
                    minecraft.player.fallDistance += (double)((float)(previousY - currentY));
                } else if (serverboundMovePlayerPacket.isOnGround()) {
                    minecraft.player.fallDistance = 0.0;
                }
                if (currentY > previousY && !serverboundMovePlayerPacket.isOnGround() && this.wasOnGround) {
                    Night.EVENT_HANDLER.post(new PlayerJumpEvent());
                }
                this.wasOnGround = serverboundMovePlayerPacket.isOnGround();
            }
            if (serverboundMovePlayerPacket.hasRotation()) {
                minecraft.player.setYRot(serverboundMovePlayerPacket.getYRot(minecraft.player.getYRot()));
                minecraft.player.setXRot(serverboundMovePlayerPacket.getXRot(minecraft.player.getXRot()));
            }
            minecraft.player.setOnGround(serverboundMovePlayerPacket.isOnGround());
            minecraft.player.horizontalCollision = serverboundMovePlayerPacket.horizontalCollision();
            ProxyServerTickListener.allowSend(() -> ((ClientPlayerEntityAccessor)minecraft.player).invokeSendMovementPackets());
        }
        PingBypassFlags.clientOnGround = serverboundMovePlayerPacket.isOnGround();
        PingBypassFlags.clientHorizontalCollision = serverboundMovePlayerPacket.horizontalCollision();
    }

    public void handleUseItemOn(ServerboundUseItemOnPacket serverboundUseItemOnPacket) {
        this.syncSlotForInteract();
        this.mirrorStartUsingItem(serverboundUseItemOnPacket.getHand());
        this.forward((Packet<?>)serverboundUseItemOnPacket);
    }

    public void handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket) {
        this.syncSlotForInteract();
        this.mirrorStartUsingItem(serverboundUseItemPacket.getHand());
        this.forward((Packet<?>)serverboundUseItemPacket);
    }

    private void mirrorStartUsingItem(InteractionHand interactionHand) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        ItemStack itemStack = minecraft.player.getItemInHand(interactionHand);
        if (itemStack.getUseDuration((LivingEntity)minecraft.player) > 0) {
            minecraft.player.startUsingItem(interactionHand);
        }
    }

    public void handleInteract(ServerboundInteractPacket serverboundInteractPacket) {
        this.forward((Packet<?>)serverboundInteractPacket);
    }

    public void handlePlayerAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket) {
        SpeedMineModule speedMineModule;
        if (serverboundPlayerActionPacket.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
            SpeedMineModule speedMineModule2;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.stopUsingItem();
            }
            if ((speedMineModule2 = Night.MODULE_MANAGER.getModule(SpeedMineModule.class)) != null && speedMineModule2.isInteractPaused()) {
                speedMineModule2.setInteractPaused(false);
            }
            this.forward((Packet<?>)serverboundPlayerActionPacket);
            return;
        }
        if (serverboundPlayerActionPacket.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK && (speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class)) != null && speedMineModule.isToggled()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.execute(() -> {
                    AttackBlockEvent attackBlockEvent = new AttackBlockEvent(serverboundPlayerActionPacket.getPos(), serverboundPlayerActionPacket.getDirection());
                    Night.EVENT_HANDLER.post(attackBlockEvent);
                    if (!attackBlockEvent.isCancelled()) {
                        this.forward((Packet<?>)serverboundPlayerActionPacket);
                    }
                });
                return;
            }
        }
        this.forward((Packet<?>)serverboundPlayerActionPacket);
    }

    public void handleAnimate(ServerboundSwingPacket serverboundSwingPacket) {
        this.forward((Packet<?>)serverboundSwingPacket);
    }

    public void handleContainerClick(ServerboundContainerClickPacket serverboundContainerClickPacket) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.execute(() -> {
                try {
                    AbstractContainerMenu abstractContainerMenu = serverboundContainerClickPacket.containerId() == minecraft.player.containerMenu.containerId ? minecraft.player.containerMenu : minecraft.player.inventoryMenu;
                    abstractContainerMenu.clicked((int)serverboundContainerClickPacket.slotNum(), (int)serverboundContainerClickPacket.buttonNum(), serverboundContainerClickPacket.containerInput(), (Player)minecraft.player);
                    ServerboundContainerClickPacket serverboundContainerClickPacket2 = new ServerboundContainerClickPacket(serverboundContainerClickPacket.containerId(), abstractContainerMenu.getStateId(), serverboundContainerClickPacket.slotNum(), serverboundContainerClickPacket.buttonNum(), serverboundContainerClickPacket.containerInput(), Int2ObjectMaps.emptyMap(), HashedStack.EMPTY);
                    this.forward((Packet<?>)serverboundContainerClickPacket2);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            });
        }
    }

    public void handleContainerClose(ServerboundContainerClosePacket serverboundContainerClosePacket) {
        this.forward((Packet<?>)serverboundContainerClosePacket);
    }

    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket serverboundSetCarriedItemPacket) {
        Minecraft minecraft = Minecraft.getInstance();
        SpeedMineModule speedMineModule = Night.MODULE_MANAGER.getModule(SpeedMineModule.class);
        boolean speedMineActive = speedMineModule != null && speedMineModule.isToggled() && speedMineModule.isRunningOnProxy() && (speedMineModule.getPrimary() != null || speedMineModule.getSecondary() != null);
        if (minecraft.player != null) {
            minecraft.player.getInventory().setSelectedSlot(serverboundSetCarriedItemPacket.getSlot());
        }
        if (speedMineActive || InventoryUtils.hasActiveSilentSwitch()) {
            return;
        }
        this.forward((Packet<?>)serverboundSetCarriedItemPacket);
    }

    public void handleClientCommand(ServerboundClientCommandPacket serverboundClientCommandPacket) {
        this.forward((Packet<?>)serverboundClientCommandPacket);
    }

    public void handleChat(ServerboundChatPacket serverboundChatPacket) {
        this.forward((Packet<?>)serverboundChatPacket);
    }

    public void handleChatCommand(ServerboundChatCommandPacket serverboundChatCommandPacket) {
        this.forward((Packet<?>)serverboundChatCommandPacket);
    }

    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket serverboundChatCommandSignedPacket) {
        this.forward((Packet<?>)serverboundChatCommandSignedPacket);
    }

    public void handleChatAck(ServerboundChatAckPacket serverboundChatAckPacket) {
        this.forward((Packet<?>)serverboundChatAckPacket);
    }

    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundChatSessionUpdatePacket) {
        this.forward((Packet<?>)serverboundChatSessionUpdatePacket);
    }

    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket serverboundCommandSuggestionPacket) {
        this.forward((Packet<?>)serverboundCommandSuggestionPacket);
    }

    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket serverboundSeenAdvancementsPacket) {
        this.forward((Packet<?>)serverboundSeenAdvancementsPacket);
    }

    public void handleSpectatorAction(ServerboundSpectatorActionPacket serverboundSpectatorActionPacket) {
        this.forward((Packet<?>)serverboundSpectatorActionPacket);
    }

    public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket) {
        this.forward((Packet<?>)serverboundResourcePackPacket);
    }

    public void handleCookieResponse(ServerboundCookieResponsePacket serverboundCookieResponsePacket) {
        this.forward((Packet<?>)serverboundCookieResponsePacket);
    }

    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket serverboundAcceptTeleportationPacket) {
        this.forward((Packet<?>)serverboundAcceptTeleportationPacket);
    }

    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket serverboundChunkBatchReceivedPacket) {
        this.forward((Packet<?>)serverboundChunkBatchReceivedPacket);
    }

    public void handleClientTickEnd(ServerboundClientTickEndPacket serverboundClientTickEndPacket) {
        this.forward((Packet<?>)serverboundClientTickEndPacket);
    }

    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket serverboundConfigurationAcknowledgedPacket) {
        this.forward((Packet<?>)serverboundConfigurationAcknowledgedPacket);
    }

    public void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket) {
        this.forward((Packet<?>)serverboundClientInformationPacket);
    }

    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket serverboundBlockEntityTagQueryPacket) {
        this.forward((Packet<?>)serverboundBlockEntityTagQueryPacket);
    }

    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket serverboundChangeDifficultyPacket) {
        this.forward((Packet<?>)serverboundChangeDifficultyPacket);
    }

    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket serverboundContainerSlotStateChangedPacket) {
        this.forward((Packet<?>)serverboundContainerSlotStateChangedPacket);
    }

    public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket serverboundDebugSubscriptionRequestPacket) {
        this.forward((Packet<?>)serverboundDebugSubscriptionRequestPacket);
    }

    public void handleEditBook(ServerboundEditBookPacket serverboundEditBookPacket) {
        this.forward((Packet<?>)serverboundEditBookPacket);
    }

    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket serverboundEntityTagQueryPacket) {
        this.forward((Packet<?>)serverboundEntityTagQueryPacket);
    }

    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket serverboundJigsawGeneratePacket) {
        this.forward((Packet<?>)serverboundJigsawGeneratePacket);
    }

    public void handleLockDifficulty(ServerboundLockDifficultyPacket serverboundLockDifficultyPacket) {
        this.forward((Packet<?>)serverboundLockDifficultyPacket);
    }

    public void handleMoveVehicle(ServerboundMoveVehiclePacket serverboundMoveVehiclePacket) {
        this.forward((Packet<?>)serverboundMoveVehiclePacket);
    }

    public void handlePaddleBoat(ServerboundPaddleBoatPacket serverboundPaddleBoatPacket) {
        this.forward((Packet<?>)serverboundPaddleBoatPacket);
    }

    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket serverboundPickItemFromBlockPacket) {
        this.forward((Packet<?>)serverboundPickItemFromBlockPacket);
    }

    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket serverboundPickItemFromEntityPacket) {
        this.forward((Packet<?>)serverboundPickItemFromEntityPacket);
    }

    public void handlePlaceRecipe(ServerboundPlaceRecipePacket serverboundPlaceRecipePacket) {
        this.forward((Packet<?>)serverboundPlaceRecipePacket);
    }

    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket serverboundPlayerAbilitiesPacket) {
        this.forward((Packet<?>)serverboundPlayerAbilitiesPacket);
    }

    public void handlePlayerCommand(ServerboundPlayerCommandPacket serverboundPlayerCommandPacket) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            switch (serverboundPlayerCommandPacket.getAction()) {
                case START_SPRINTING: {
                    minecraft.player.setSprinting(true);
                    break;
                }
                case STOP_SPRINTING: {
                    minecraft.player.setSprinting(false);
                }
            }
        }
        this.forward((Packet<?>)serverboundPlayerCommandPacket);
    }

    public void handlePlayerInput(ServerboundPlayerInputPacket serverboundPlayerInputPacket) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.setShiftKeyDown(serverboundPlayerInputPacket.input().shift());
        }
        this.forward((Packet<?>)serverboundPlayerInputPacket);
    }

    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket serverboundPlayerLoadedPacket) {
        this.forward((Packet<?>)serverboundPlayerLoadedPacket);
    }

    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket serverboundRecipeBookSeenRecipePacket) {
        this.forward((Packet<?>)serverboundRecipeBookSeenRecipePacket);
    }

    public void handleRenameItem(ServerboundRenameItemPacket serverboundRenameItemPacket) {
        this.forward((Packet<?>)serverboundRenameItemPacket);
    }

    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket serverboundSelectBundleItemPacket) {
        this.forward((Packet<?>)serverboundSelectBundleItemPacket);
    }

    public void handleSelectTrade(ServerboundSelectTradePacket serverboundSelectTradePacket) {
        this.forward((Packet<?>)serverboundSelectTradePacket);
    }

    public void handleSetBeaconPacket(ServerboundSetBeaconPacket serverboundSetBeaconPacket) {
        this.forward((Packet<?>)serverboundSetBeaconPacket);
    }

    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket serverboundSetCommandBlockPacket) {
        this.forward((Packet<?>)serverboundSetCommandBlockPacket);
    }

    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket serverboundSetCommandMinecartPacket) {
        this.forward((Packet<?>)serverboundSetCommandMinecartPacket);
    }

    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundSetCreativeModeSlotPacket) {
        this.forward((Packet<?>)serverboundSetCreativeModeSlotPacket);
    }

    public void handleSetGameRule(ServerboundSetGameRulePacket serverboundSetGameRulePacket) {
        this.forward((Packet<?>)serverboundSetGameRulePacket);
    }

    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket serverboundSetJigsawBlockPacket) {
        this.forward((Packet<?>)serverboundSetJigsawBlockPacket);
    }

    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket serverboundSetStructureBlockPacket) {
        this.forward((Packet<?>)serverboundSetStructureBlockPacket);
    }

    public void handleSetTestBlock(ServerboundSetTestBlockPacket serverboundSetTestBlockPacket) {
        this.forward((Packet<?>)serverboundSetTestBlockPacket);
    }

    public void handleSignUpdate(ServerboundSignUpdatePacket serverboundSignUpdatePacket) {
        this.forward((Packet<?>)serverboundSignUpdatePacket);
    }

    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket serverboundTeleportToEntityPacket) {
        this.forward((Packet<?>)serverboundTeleportToEntityPacket);
    }

    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket serverboundTestInstanceBlockActionPacket) {
        this.forward((Packet<?>)serverboundTestInstanceBlockActionPacket);
    }

    public void handleChangeGameMode(ServerboundChangeGameModePacket serverboundChangeGameModePacket) {
        this.forward((Packet<?>)serverboundChangeGameModePacket);
    }

    public void handleAttack(ServerboundAttackPacket serverboundAttackPacket) {
        this.forward((Packet<?>)serverboundAttackPacket);
    }

    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundContainerButtonClickPacket) {
        this.forward((Packet<?>)serverboundContainerButtonClickPacket);
    }

    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket serverboundRecipeBookChangeSettingsPacket) {
        this.forward((Packet<?>)serverboundRecipeBookChangeSettingsPacket);
    }

    public void handleCustomClickAction(ServerboundCustomClickActionPacket serverboundCustomClickActionPacket) {
        this.forward((Packet<?>)serverboundCustomClickActionPacket);
    }

    public void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket) {
    }

    public void handlePong(ServerboundPongPacket serverboundPongPacket) {
        this.forward((Packet<?>)serverboundPongPacket);
    }

    public void handlePingRequest(ServerboundPingRequestPacket serverboundPingRequestPacket) {
    }

    public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket) {
        CustomPacketPayload customPacketPayload = serverboundCustomPayloadPacket.payload();
        if (customPacketPayload instanceof PbCustomPayload) {
            PbCustomPayload pbCustomPayload = (PbCustomPayload)customPacketPayload;
            this.handlePbPayload(pbCustomPayload);
            return;
        }
        this.forward((Packet<?>)serverboundCustomPayloadPacket);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handlePbPayload(PbCustomPayload pbCustomPayload) {
        FriendlyByteBuf friendlyByteBuf = pbCustomPayload.toBuf();
        try {
            int packetId = friendlyByteBuf.readVarInt();
            switch (packetId) {
                case 3: {
                    C2SModuleTogglePacket c2SModuleTogglePacket = new C2SModuleTogglePacket(friendlyByteBuf);
                    PbModule pbModule = Night.PB_MODULE_MANAGER.getModule(c2SModuleTogglePacket.getModuleName());
                    if (pbModule != null) {
                        Minecraft.getInstance().execute(() -> {
                            SyncModule.applyToggle(pbModule, c2SModuleTogglePacket.isEnabled());
                            LOGGER.info("[PB] PbModule {} toggled to {}", (Object)c2SModuleTogglePacket.getModuleName(), (Object)c2SModuleTogglePacket.isEnabled());
                        });
                        return;
                    }
                    Module module = Night.MODULE_MANAGER.getModule(c2SModuleTogglePacket.getModuleName());
                    if (module == null) {
                        return;
                    }
                    Minecraft.getInstance().execute(() -> {
                        module.setToggled(c2SModuleTogglePacket.isEnabled(), false);
                        LOGGER.info("[PB] Module {} toggled to {}", (Object)c2SModuleTogglePacket.getModuleName(), (Object)c2SModuleTogglePacket.isEnabled());
                    });
                    return;
                }
                case 4: {
                    C2SSettingChangePacket c2SSettingChangePacket = new C2SSettingChangePacket(friendlyByteBuf);
                    PbModule pbModule = Night.PB_MODULE_MANAGER.getModule(c2SSettingChangePacket.getModuleName());
                    if (pbModule != null) {
                        Minecraft.getInstance().execute(() -> SyncModule.applySetting(pbModule, c2SSettingChangePacket.getSettingName(), c2SSettingChangePacket.getValue()));
                        return;
                    }
                    this.handleSettingChange(c2SSettingChangePacket);
                    return;
                }
                case 15: {
                    C2SOpenInventoryPacket c2SOpenInventoryPacket = new C2SOpenInventoryPacket(friendlyByteBuf);
                    Minecraft.getInstance().execute(() -> {
                        Minecraft minecraft = Minecraft.getInstance();
                        if (minecraft.player == null) {
                            return;
                        }
                        if (c2SOpenInventoryPacket.isOpen()) {
                            minecraft.gui.setScreen((Screen)new InventoryScreen((Player)minecraft.player));
                        } else {
                            InventoryScreen inventoryScreen;
                            Screen screen = minecraft.gui.screen();
                            if (screen instanceof InventoryScreen && (inventoryScreen = (InventoryScreen)screen).getMenu() == minecraft.player.inventoryMenu) {
                                minecraft.gui.setScreen(null);
                            }
                        }
                    });
                    return;
                }
                case 14: {
                    C2SFriendSyncPacket c2SFriendSyncPacket = new C2SFriendSyncPacket(friendlyByteBuf);
                    Minecraft.getInstance().execute(() -> {
                        Night.FRIEND_MANAGER.clear();
                        for (String friend : c2SFriendSyncPacket.getFriends()) {
                            Night.FRIEND_MANAGER.add(friend);
                        }
                        LOGGER.info("[PB] Synced {} friend(s) from client", (Object)c2SFriendSyncPacket.getFriends().size());
                    });
                    return;
                }
            }
            LOGGER.debug("[PB] Unknown packet ID: {}", (Object)packetId);
            return;
        }
        catch (Exception exception) {
            LOGGER.warn("[PB] Failed to handle payload", (Throwable)exception);
            return;
        }
        finally {
            friendlyByteBuf.release();
        }
    }

    private void handleSettingChange(C2SSettingChangePacket c2SSettingChangePacket) {
        Module module = Night.MODULE_MANAGER.getModule(c2SSettingChangePacket.getModuleName());
        if (module == null) {
            return;
        }
        Setting setting = module.getSetting(c2SSettingChangePacket.getSettingName());
        if (setting == null) {
            return;
        }
        Minecraft.getInstance().execute(() -> {
            try {
                String value = c2SSettingChangePacket.getValue();
                if (setting instanceof BooleanSetting) {
                    BooleanSetting booleanSetting = (BooleanSetting)setting;
                    booleanSetting.setValue(Boolean.parseBoolean(value));
                } else if (setting instanceof NumberSetting) {
                    NumberSetting numberSetting = (NumberSetting)setting;
                    switch (numberSetting.getType()) {
                        case INTEGER: {
                            numberSetting.setValue(Integer.parseInt(value));
                            break;
                        }
                        case LONG: {
                            numberSetting.setValue(Long.parseLong(value));
                            break;
                        }
                        case FLOAT: {
                            numberSetting.setValue(Float.valueOf(Float.parseFloat(value)));
                            break;
                        }
                        case DOUBLE: {
                            numberSetting.setValue(Double.parseDouble(value));
                        }
                    }
                } else if (setting instanceof ModeSetting) {
                    ModeSetting modeSetting = (ModeSetting)setting;
                    modeSetting.setValue(value);
                } else if (setting instanceof StringSetting) {
                    StringSetting stringSetting = (StringSetting)setting;
                    stringSetting.setValue(value);
                } else if (setting instanceof ColorSetting) {
                    ColorSetting colorSetting = (ColorSetting)setting;
                    String[] stringArray = value.split(",");
                    if (stringArray.length >= 4) {
                        colorSetting.setColor(new Color(Integer.parseInt(stringArray[0]), Integer.parseInt(stringArray[1]), Integer.parseInt(stringArray[2]), Integer.parseInt(stringArray[3])));
                        if (stringArray.length >= 5) {
                            colorSetting.setSync(Boolean.parseBoolean(stringArray[4]));
                        }
                        if (stringArray.length >= 6) {
                            colorSetting.setRainbow(Boolean.parseBoolean(stringArray[5]));
                        }
                    }
                }
                LOGGER.info("[PB] Setting {}.{} = {}", new Object[]{c2SSettingChangePacket.getModuleName(), c2SSettingChangePacket.getSettingName(), value});
            }
            catch (Exception exception) {
                LOGGER.warn("[PB] Failed to apply setting {}.{}", new Object[]{c2SSettingChangePacket.getModuleName(), c2SSettingChangePacket.getSettingName(), exception});
            }
        });
    }
}

