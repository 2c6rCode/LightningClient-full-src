/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.KeyboardHandlerAccessor
 *  lightning.mixins.accessors.MouseAccessor
 *  lombok.Generated
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonInfo
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.miscellaneous;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.ChangePitchEvent;
import lightning.events.impl.ChangeYawEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.events.impl.TickEvent;
import lightning.events.impl.ToggleModuleEvent;
import lightning.events.impl.UnfilteredKeyInputEvent;
import lightning.events.impl.UnfilteredMouseInputEvent;
import lightning.mixins.accessors.KeyboardHandlerAccessor;
import lightning.mixins.accessors.MouseAccessor;
import lightning.modules.Module;
import lightning.modules.impl.movement.HitboxDesyncModule;
import lightning.modules.impl.movement.HoleSnapModule;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.MovementUtils;
import lombok.Generated;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EURoboticsModule
extends Module {
    public ModeSetting side = new ModeSetting("Side", "The side that this Minecraft instance is on.", "Client", new String[]{"Client", "Server"});
    public StringSetting port = new StringSetting("Port", "The port that will be used for communication.", "4311");
    private String target = "";
    private final Client client = new Client();
    private final Server server = new Server();

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (EURoboticsModule.mc.player == null || EURoboticsModule.mc.level == null) {
            return;
        }
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("update;" + EURoboticsModule.mc.player.getName().getString());
        }
        catch (IOException iOException) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)iOException);
        }
    }

    @SubscribeEvent
    public void onToggleModule(ToggleModuleEvent toggleModuleEvent) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("module;" + toggleModuleEvent.getModule().getName() + ";" + toggleModuleEvent.getModule().isToggled());
        }
        catch (IOException iOException) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)iOException);
        }
    }

    @SubscribeEvent
    public void onUnfilteredKeyInput(UnfilteredKeyInputEvent unfilteredKeyInputEvent) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("key;" + unfilteredKeyInputEvent.getKey() + ";" + unfilteredKeyInputEvent.getScancode() + ";" + unfilteredKeyInputEvent.getAction() + ";" + unfilteredKeyInputEvent.getModifiers());
        }
        catch (IOException iOException) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)iOException);
        }
    }

    @SubscribeEvent
    public void onUnfilteredMouseInput(UnfilteredMouseInputEvent unfilteredMouseInputEvent) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("mouse;" + unfilteredMouseInputEvent.getButton() + ";" + unfilteredMouseInputEvent.getAction() + ";" + unfilteredMouseInputEvent.getMods());
        }
        catch (IOException iOException) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)iOException);
        }
    }

    @SubscribeEvent
    public void onChangeYaw(ChangeYawEvent changeYawEvent) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("yaw;" + changeYawEvent.getYaw());
        }
        catch (IOException iOException) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)iOException);
        }
    }

    @SubscribeEvent
    public void onChangePitch(ChangePitchEvent changePitchEvent) {
        if (this.side.getValue().equalsIgnoreCase("Server") || this.client.getPrimarySocket() == null || !this.client.getPrimarySocket().isConnected()) {
            return;
        }
        try {
            this.client.sendMessage("pitch;" + changePitchEvent.getPitch());
        }
        catch (IOException iOException) {
            Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)iOException);
        }
    }

    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        if (this.getNull() || EURoboticsModule.mc.player.fallDistance >= 5.0) {
            return;
        }
        if (this.side.getValue().equalsIgnoreCase("Client")) {
            return;
        }
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        if (EURoboticsModule.mc.player.distanceToSqr(player.position()) <= 0.25) {
            return;
        }
        if (EURoboticsModule.mc.player.distanceToSqr(player.position()) <= Mth.square((double)1.5) && (EURoboticsModule.mc.options.keyUp.isDown() || EURoboticsModule.mc.options.keyDown.isDown() || EURoboticsModule.mc.options.keyLeft.isDown() || EURoboticsModule.mc.options.keyRight.isDown())) {
            return;
        }
        if (Night.MODULE_MANAGER.getModule(HoleSnapModule.class).isToggled()) {
            return;
        }
        if (Night.MODULE_MANAGER.getModule(HitboxDesyncModule.class).isToggled()) {
            return;
        }
        MovementUtils.moveTowards(playerMoveEvent, Vec3.upFromBottomCenterOf((Vec3i)player.blockPosition(), (double)0.0), MovementUtils.getPotionSpeed(MovementUtils.DEFAULT_SPEED));
    }

    @Override
    @SubscribeEvent
    public void onEnable() {
        if (this.side.getValue().equalsIgnoreCase("Client")) {
            try {
                if (EURoboticsModule.mc.player == null || EURoboticsModule.mc.level == null) {
                    if (this.client.getPrimarySocket() != null) {
                        this.client.stopConnection();
                    }
                    return;
                }
                this.client.startConnection("127.0.0.1", Integer.parseInt(this.port.getValue()));
            }
            catch (IOException iOException) {
                Night.LOGGER.error("An exception has been thrown by clientside EU Robotics!", (Throwable)iOException);
                Night.CHAT_MANAGER.error("Failed to establish a connection to the EURobotics server.");
                this.setToggled(false);
            }
        } else {
            try {
                if (EURoboticsModule.mc.player == null || EURoboticsModule.mc.level == null) {
                    if (this.server.getServerSocket() != null) {
                        this.server.stopConnection();
                    }
                    return;
                }
                new Thread(() -> {
                    try {
                        this.server.startConnection(Integer.parseInt(this.port.getValue()));
                    }
                    catch (IOException iOException) {
                        throw new RuntimeException(iOException);
                    }
                }).start();
            }
            catch (IOException iOException) {
                Night.LOGGER.error("An exception has been thrown by serverside EU Robotics!", (Throwable)iOException);
                Night.CHAT_MANAGER.error("Failed to start the EURobotics server.");
                this.setToggled(false);
            }
        }
    }

    public boolean shouldStep() {
        if (!this.isToggled()) {
            return false;
        }
        Player player = this.getPlayer();
        if (player == null) {
            return false;
        }
        if (EURoboticsModule.mc.player.distanceToSqr(player.position()) <= 0.25) {
            return false;
        }
        return !(EURoboticsModule.mc.player.distanceToSqr(player.position()) <= Mth.square((double)1.5)) || !EURoboticsModule.mc.options.keyUp.isDown() && !EURoboticsModule.mc.options.keyDown.isDown() && !EURoboticsModule.mc.options.keyLeft.isDown() && !EURoboticsModule.mc.options.keyRight.isDown();
    }

    private Player getPlayer() {
        if (this.target.isEmpty()) {
            return null;
        }
        for (Player player : EURoboticsModule.mc.level.players()) {
            if (player == EURoboticsModule.mc.player || !player.getName().getString().equals(this.target)) continue;
            return player;
        }
        return null;
    }

    @Generated
    public void setTarget(String target) {
        this.target = target;
    }

    public static class Client {
        private Socket primarySocket;
        private PrintWriter primaryOut;
        private Socket secondarySocket;
        private PrintWriter secondaryOut;
        private Socket tertiarySocket;
        private PrintWriter tertiaryOut;

        public void startConnection(String host, int basePort) throws IOException {
            this.primarySocket = new Socket(host, basePort);
            this.primaryOut = new PrintWriter(this.primarySocket.getOutputStream(), true);
            this.secondarySocket = new Socket(host, basePort + 1);
            this.secondaryOut = new PrintWriter(this.secondarySocket.getOutputStream(), true);
            this.tertiarySocket = new Socket(host, basePort + 2);
            this.tertiaryOut = new PrintWriter(this.tertiarySocket.getOutputStream(), true);
        }

        public void stopConnection() throws IOException {
            this.primaryOut.close();
            this.primarySocket.close();
        }

        public void sendMessage(String message) throws IOException {
            this.primaryOut.println(message);
            this.secondaryOut.println(message);
            this.tertiaryOut.println(message);
        }

        @Generated
        public Socket getPrimarySocket() {
            return this.primarySocket;
        }

        @Generated
        public PrintWriter getPrimaryOut() {
            return this.primaryOut;
        }

        @Generated
        public Socket getSecondarySocket() {
            return this.secondarySocket;
        }

        @Generated
        public PrintWriter getSecondaryOut() {
            return this.secondaryOut;
        }

        @Generated
        public Socket getTertiarySocket() {
            return this.tertiarySocket;
        }

        @Generated
        public PrintWriter getTertiaryOut() {
            return this.tertiaryOut;
        }
    }

    public static class Server {
        private ServerSocket serverSocket;
        private Socket clientSocket;
        private BufferedReader in;

        public void startConnection(int port) throws IOException {
            String line;
            this.serverSocket = new ServerSocket(port);
            this.clientSocket = this.serverSocket.accept();
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            while ((line = this.in.readLine()) != null) {
                Module module;
                String[] parts = line.split(";");
                if (parts[0].equalsIgnoreCase("module") && parts.length == 3 && (module = Night.MODULE_MANAGER.getModule(parts[1])) != null) {
                    Night.TASK_MANAGER.submit(() -> module.setToggled(Boolean.parseBoolean(parts[2])));
                }
                if (parts[0].equalsIgnoreCase("update") && parts.length == 2) {
                    Night.MODULE_MANAGER.getModule(EURoboticsModule.class).setTarget(parts[1]);
                }
                if (parts[0].equalsIgnoreCase("key") && parts.length == 5 && IMinecraft.mc.keyboardHandler != null) {
                    final int key = Integer.parseInt(parts[1]);
                    final int scancode = Integer.parseInt(parts[2]);
                    final int action = Integer.parseInt(parts[3]);
                    final int modifiers = Integer.parseInt(parts[4]);
                    Night.TASK_MANAGER.submit(() -> ((KeyboardHandlerAccessor)IMinecraft.mc.keyboardHandler).invokeKeyPress(IMinecraft.mc.getWindow().handle(), action, new KeyEvent(key, scancode, modifiers)));
                }
                if (parts[0].equalsIgnoreCase("mouse") && parts.length == 4 && IMinecraft.mc.mouseHandler != null) {
                    final int button = Integer.parseInt(parts[1]);
                    final int action = Integer.parseInt(parts[2]);
                    final int mods = Integer.parseInt(parts[3]);
                    Night.TASK_MANAGER.submit(() -> ((MouseAccessor)IMinecraft.mc.mouseHandler).invokeOnButton(IMinecraft.mc.getWindow().handle(), new MouseButtonInfo(button, action), mods));
                }
                if (parts[0].equalsIgnoreCase("yaw") && parts.length == 2 && IMinecraft.mc.player != null) {
                    IMinecraft.mc.player.setYRot(Float.parseFloat(parts[1]));
                }
                if (parts[0].equalsIgnoreCase("pitch") && parts.length == 2 && IMinecraft.mc.player != null) {
                    IMinecraft.mc.player.setXRot(Float.parseFloat(parts[1]));
                }
                if (Night.MODULE_MANAGER.getModule(EURoboticsModule.class).isToggled()) continue;
                this.stopConnection();
                break;
            }
        }

        public void stopConnection() throws IOException {
            this.in.close();
            this.clientSocket.close();
            this.serverSocket.close();
        }

        @Generated
        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }

        @Generated
        public Socket getClientSocket() {
            return this.clientSocket;
        }

        @Generated
        public BufferedReader getIn() {
            return this.in;
        }
    }
}

