/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
 */
package lightning.loader;

import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.JOptionPane;
import lightning.auth.AuthGate;
import lightning.loader.Injector;
import lightning.loader.Payload;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class LightningLoader
implements PreLaunchEntrypoint {
    private static final String SERVER = "https://lightning-license-server-xaw3.onrender.com";

    public void onPreLaunch() {
    }

    private static String readToken() {
        try {
            Field field = AuthGate.class.getDeclaredField("sessionToken");
            field.setAccessible(true);
            Object object = field.get(null);
            return object == null ? null : object.toString();
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private static void fail(String string, Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }
        try {
            JOptionPane.showMessageDialog(null, string, "Lightning", 0);
        }
        catch (Throwable throwable2) {
            // empty catch block
        }
        System.exit(1);
    }
}

