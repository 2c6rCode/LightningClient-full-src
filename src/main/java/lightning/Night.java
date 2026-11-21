/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.api.ModInitializer
 *  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
 *  net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
 *  net.fabricmc.loader.api.FabricLoader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lightning.auth.AuthOkay;
import lightning.commands.CommandManager;
import lightning.events.EventHandler;
import lightning.gui.ClickGuiScreen;
import lightning.managers.AccountManager;
import lightning.managers.BoostManager;
import lightning.managers.ChatManager;
import lightning.managers.ConfigManager;
import lightning.managers.FontManager;
import lightning.managers.FriendManager;
import lightning.managers.ImageManager;
import lightning.managers.MacroManager;
import lightning.managers.NightUserManager;
import lightning.managers.PositionManager;
import lightning.managers.RenderManager;
import lightning.managers.RotationManager;
import lightning.managers.ServerManager;
import lightning.managers.TargetManager;
import lightning.managers.TaskManager;
import lightning.managers.WaypointManager;
import lightning.managers.WorldManager;
import lightning.modules.ModuleManager;
import lightning.pingbypass.PingBypassConfig;
import lightning.pingbypass.modules.PbModuleManager;
import lightning.pingbypass.modules.submodules.crystal.ServerAutoCrystal;
import lightning.pingbypass.modules.submodules.totem.ServerAutoTotem;
import lightning.pingbypass.protocol.PbCustomPayload;
import lightning.pingbypass.server.ProxyServer;
import lightning.pingbypass.server.ProxyServerTickListener;
import lightning.pingbypass.server.TransferRehook;
import lightning.utils.BlockEntityInstanceTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Night
implements ModInitializer {
    public static final String MOD_NAME = "Lightning";
    public static final String MOD_ID = "lightning";
    public static final String MOD_VERSION = "Beta";
    public static final String MINECRAFT_VERSION = "26.2";
    public static final String GIT_HASH = "eeeb439b78";
    public static final String GIT_REVISION = "436";
    public static final String BUILD_DATE = "2026-09-17";
    public static final long UPTIME = System.currentTimeMillis();
    public static final EventHandler EVENT_HANDLER = new EventHandler();
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"Lightning");
    private static final ExecutorService UPDATE_EXECUTOR = Executors.newSingleThreadExecutor();
    public static String UPDATE_STATUS = "none";
    private static final String VERSION_URL = "";
    private static final String SECRET_KEY = "";
    public static ChatManager CHAT_MANAGER;
    public static FontManager FONT_MANAGER;
    public static FriendManager FRIEND_MANAGER;
    public static WorldManager WORLD_MANAGER;
    public static PositionManager POSITION_MANAGER;
    public static RotationManager ROTATION_MANAGER;
    public static ServerManager SERVER_MANAGER;
    public static RenderManager RENDER_MANAGER;
    public static TargetManager TARGET_MANAGER;
    public static MacroManager MACRO_MANAGER;
    public static TaskManager TASK_MANAGER;
    public static WaypointManager WAYPOINT_MANAGER;
    public static BoostManager BOOST_MANAGER;
    public static NightUserManager NIGHT_USER_MANAGER;
    public static ImageManager IMAGE_MANAGER;
    public static ModuleManager MODULE_MANAGER;
    public static CommandManager COMMAND_MANAGER;
    public static ConfigManager CONFIG_MANAGER;
    public static AccountManager ACCOUNT_MANAGER;
    public static ClickGuiScreen CLICK_GUI;
    public static PingBypassConfig PINGBYPASS_CONFIG;
    public static ProxyServer PROXY_SERVER;
    public static final PbModuleManager PB_MODULE_MANAGER;

    public Night() {
        BlockEntityInstanceTracker.initialize();
    }

    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");
        if (!AuthOkay.verify()) {
            System.exit(0);
        }
        try {
            PayloadTypeRegistry.serverboundPlay().register(PbCustomPayload.ID, PbCustomPayload.CODEC);
            PayloadTypeRegistry.clientboundPlay().register(PbCustomPayload.ID, PbCustomPayload.CODEC);
            ClientPlayNetworking.registerGlobalReceiver(PbCustomPayload.ID, (pbCustomPayload, context) -> {});
        }
        catch (Throwable throwable) {
            LOGGER.warn("Fabric Networking API not available or optional: {}", (Object)throwable.getMessage());
        }
        try {
            PINGBYPASS_CONFIG = new PingBypassConfig(FabricLoader.getInstance().getGameDir());
            PINGBYPASS_CONFIG.load();
            LOGGER.info("PingBypass mode: {}", (Object)(PINGBYPASS_CONFIG.isServer() ? "server" : "client"));
            if (PINGBYPASS_CONFIG.isServer()) {
                try {
                    PROXY_SERVER = new ProxyServer(PINGBYPASS_CONFIG);
                    PROXY_SERVER.bind(InetAddress.getByName(PINGBYPASS_CONFIG.getIp()), PINGBYPASS_CONFIG.getPort());
                    EVENT_HANDLER.subscribe(new ProxyServerTickListener(PROXY_SERVER));
                    new TransferRehook();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (PROXY_SERVER != null) {
                            PROXY_SERVER.shutdown();
                        }
                    }, "PingBypass-Shutdown"));
                    LOGGER.info("PingBypass proxy server started on {}:{}", (Object)PINGBYPASS_CONFIG.getIp(), (Object)PINGBYPASS_CONFIG.getPort());
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to start PingBypass proxy server", (Throwable)iOException);
                }
            }
        }
        catch (Throwable throwable) {
            LOGGER.error("Failed to initialize PingBypass config/server", throwable);
        }
        Night.initManagers();
    }

    public static synchronized void initManagers() {
        if (CHAT_MANAGER != null) {
            return;
        }
        CHAT_MANAGER = new ChatManager();
        FONT_MANAGER = new FontManager();
        FRIEND_MANAGER = new FriendManager();
        WORLD_MANAGER = new WorldManager();
        POSITION_MANAGER = new PositionManager();
        ROTATION_MANAGER = new RotationManager();
        SERVER_MANAGER = new ServerManager();
        RENDER_MANAGER = new RenderManager();
        TARGET_MANAGER = new TargetManager();
        MACRO_MANAGER = new MacroManager();
        TASK_MANAGER = new TaskManager();
        WAYPOINT_MANAGER = new WaypointManager();
        BOOST_MANAGER = new BoostManager();
        NIGHT_USER_MANAGER = new NightUserManager();
        IMAGE_MANAGER = new ImageManager();
        MODULE_MANAGER = new ModuleManager();
        COMMAND_MANAGER = new CommandManager();
        ACCOUNT_MANAGER = new AccountManager();
        if (PROXY_SERVER != null) {
            PB_MODULE_MANAGER.register(new ServerAutoCrystal());
            PB_MODULE_MANAGER.register(new ServerAutoTotem());
        }
    }

    public static void onPostInitialize() {
        String string;
        Night.initManagers();
        CONFIG_MANAGER = new ConfigManager();
        CLICK_GUI = new ClickGuiScreen();
        if (IMAGE_MANAGER != null && (string = ImageManager.findSavedActiveImage()) != null && !string.equalsIgnoreCase("None") && !string.isEmpty()) {
            IMAGE_MANAGER.setCurrentActiveImage(string);
        }
        LOGGER.info("{} {} has been initialized.", (Object)MOD_NAME, (Object)MOD_VERSION);
    }

    public static void checkForUpdates() {
        UPDATE_EXECUTOR.submit(() -> {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                return;
            }
            if (!FabricLoader.getInstance().isModLoaded("night-updater")) {
                return;
            }
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection)new URL("").openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                if (httpURLConnection.getResponseCode() == 200) {
                    InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                    JsonObject jsonObject = JsonParser.parseReader((Reader)inputStreamReader).getAsJsonObject();
                    if (!jsonObject.has("version")) {
                        return;
                    }
                    if (!MOD_VERSION.equalsIgnoreCase(jsonObject.get("version").getAsString())) {
                        UPDATE_STATUS = "update-available";
                    }
                } else {
                    UPDATE_STATUS = "failed-connection";
                }
            }
            catch (IOException iOException) {
                UPDATE_STATUS = "failed";
            }
            if (UPDATE_STATUS.equalsIgnoreCase("none")) {
                UPDATE_STATUS = "up-to-date";
            }
        });
    }

    static {
        PB_MODULE_MANAGER = new PbModuleManager();
    }
}

