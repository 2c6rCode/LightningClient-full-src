/*
 * Decompiled with CFR 0.152.
 */
package lightning.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public final class AuthGate {
    private static final String SERVER = "https://lightning-license-server-xaw3.onrender.com";
    private static final Path CRED_FILE = Paths.get(System.getProperty("user.home"), ".lightning", "credentials.properties");
    private static final Color BG = new Color(13, 18, 32);
    private static final Color PANEL = new Color(18, 26, 46);
    private static final Color FIELD_BG = new Color(10, 14, 26);
    private static final Color LINE = new Color(31, 42, 68);
    private static final Color CYAN = new Color(92, 225, 255);
    private static final Color VIOLET = new Color(139, 108, 255);
    private static final Color TEXT = new Color(230, 236, 255);
    private static final Color TEXT_DIM = new Color(123, 136, 168);
    private static final Color ERROR = new Color(255, 110, 110);
    private static final Font FONT_TITLE = new Font("Segoe UI", 1, 26);
    private static final Font FONT_LABEL = new Font("Segoe UI", 0, 13);
    private static final Font FONT_INPUT = new Font("Consolas", 0, 14);
    private static volatile String sessionToken;
    private static volatile boolean heartbeatRunning;

    private AuthGate() {
    }

    public static boolean verify() {
        sessionToken = "offline";
        return true;
    }

    private static void startHeartbeat() {
    }

    private static Properties loadSaved() {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(CRED_FILE, new OpenOption[0]);){
            properties.load(inputStream);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return properties;
    }

    private static void save(String string, String string2) {
        try {
            Files.createDirectories(CRED_FILE.getParent(), new FileAttribute[0]);
            Properties properties = new Properties();
            properties.setProperty("username", string);
            properties.setProperty("licenseKey", string2);
            try (OutputStream outputStream = Files.newOutputStream(CRED_FILE, new OpenOption[0]);){
                properties.store(outputStream, "Lightning saved login (khong luu mat khau)");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static String esc(String string) {
        return string.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static boolean jsonBool(String string, String string2) {
        Matcher matcher = Pattern.compile("\"" + string2 + "\"\\s*:\\s*(true|false)").matcher(string);
        return matcher.find() && matcher.group(1).equals("true");
    }

    private static String jsonString(String string, String string2) {
        Matcher matcher = Pattern.compile("\"" + string2 + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(string);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    static {
        heartbeatRunning = false;
    }

    private static final class AuthResult {
        boolean ok;
        String token;
        String message;

        private AuthResult() {
        }

        static AuthResult ok(String string) {
            AuthResult authResult = new AuthResult();
            authResult.ok = true;
            authResult.token = string;
            return authResult;
        }

        static AuthResult error(String string) {
            AuthResult authResult = new AuthResult();
            authResult.ok = false;
            authResult.message = string;
            return authResult;
        }
    }
}

