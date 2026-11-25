/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package lightning.pingbypass;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingBypassConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PingBypassConfig.class);
    public static final String DEFAULT_SERVER = "false";
    public static final String DEFAULT_IP = "0.0.0.0";
    public static final String DEFAULT_PORT = "25565";
    public static final String DEFAULT_PASSWORD = "";
    private static final String KEY_SERVER = "pb.server";
    private static final String KEY_IP = "pb.ip";
    private static final String KEY_PORT = "pb.port";
    private static final String KEY_PASSWORD = "pb.password";
    private final Properties properties = new Properties();
    private final Path configPath;
    private boolean loaded;

    public PingBypassConfig(Path path) {
        this.configPath = path.resolve(Paths.get("lightning", "pingbypass.properties"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void load() {
        try {
            Path parentDir = this.configPath.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir, new FileAttribute[0]);
            }
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                Path legacyConfigPath = this.configPath.getParent().getParent().resolve(Paths.get("euclient", "pingbypass.properties"));
                if (Files.exists(legacyConfigPath, new LinkOption[0])) {
                    Files.copy(legacyConfigPath, this.configPath, StandardCopyOption.COPY_ATTRIBUTES);
                } else {
                    this.createDefaults();
                }
            }
            InputStream inputStream = Files.newInputStream(this.configPath, new OpenOption[0]);
            try {
                this.properties.load(inputStream);
            }
            finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to load PingBypass config from {}", (Object)this.configPath, (Object)iOException);
        }
        finally {
            this.loaded = true;
        }
    }

    private void createDefaults() throws IOException {
        Properties properties = new Properties();
        properties.setProperty(KEY_SERVER, DEFAULT_SERVER);
        properties.setProperty(KEY_IP, DEFAULT_IP);
        properties.setProperty(KEY_PORT, DEFAULT_PORT);
        properties.setProperty(KEY_PASSWORD, DEFAULT_PASSWORD);
        try (OutputStream outputStream = Files.newOutputStream(this.configPath, new OpenOption[0]);){
            properties.store(outputStream, "PingBypass configuration");
        }
    }

    public boolean isServer() {
        return Boolean.parseBoolean(this.getProperty(KEY_SERVER, DEFAULT_SERVER));
    }

    public String getIp() {
        return this.getProperty(KEY_IP, DEFAULT_IP);
    }

    public int getPort() {
        String portValue = this.getProperty(KEY_PORT, DEFAULT_PORT);
        try {
            int port = Integer.parseInt(portValue);
            if (port < 1 || port > 65535) {
                LOGGER.error("PingBypass port out of range (1-65535): {}, falling back to default 25565", (Object)port);
                return 25565;
            }
            return port;
        }
        catch (NumberFormatException numberFormatException) {
            LOGGER.error("PingBypass port is not a valid integer: '{}', falling back to default 25565", (Object)portValue);
            return 25565;
        }
    }

    public String getPassword() {
        return this.getProperty(KEY_PASSWORD, DEFAULT_PASSWORD);
    }

    public boolean hasPassword() {
        String password = this.getPassword();
        return password != null && !password.isEmpty();
    }

    public String getProperty(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        return this.properties.getProperty(key, defaultValue);
    }

    public Properties toProperties() {
        Properties properties = new Properties();
        properties.setProperty(KEY_SERVER, String.valueOf(this.isServer()));
        properties.setProperty(KEY_IP, this.getIp());
        properties.setProperty(KEY_PORT, String.valueOf(this.getPort()));
        properties.setProperty(KEY_PASSWORD, this.getPassword());
        return properties;
    }

    public boolean isLoaded() {
        return this.loaded;
    }
}

