# LightningClient - Decompiled & Cracked

A shitty client with the code stolen from NightClient and Homovore, nice obf, security and brain cell ass.

Imagine pasting code from other clients, slapping a garbage authentication gate on top, and praying nobody reverses your clown-tier security in five minutes.

## "Uncrackable" Security in Action

Buddy thought his client was slick

![Fucked by FPG](menu.jpg)

## Credits

- **Original source dump:** Doom33
- **Reverse engineering & technical cleanup:** Fixed decompiled garbage variables, de-obfuscated CFR ternary messes, completely bypassed auth/heartbeat gates, resolved synthetic lambda format errors, and made this mess actually compile from clean source by 2c6r with da AI.
- **Public release:** Shoutout to FPG for making this client public!

## What Was Done

- Ripped out all remote authentication checks, license gates, and remote heartbeats. Client runs fully offline without checking with anyone's server.
- Renamed decompiled variables across crystal placement, combat aura, mining modules, and rotation managers so the code is actually readable instead of pure single-letter garbage.
- Fixed decompiled synthetic lambda method names that triggered JVM ClassFormatErrors on launch.
- Repaired broken CFR bytecode artifacts and invalid exception handling across classloaders.
- Added working standalone Gradle wrapper and desktop launcher script.

## Clown-Tier Code Highlights

A quick museum tour of the galaxy-brain security pasted into this client:

### 1. Free Render.com Tier License Server

Imagine marketing a paid client and hosting your license server on a free Render subdomain with a Swing popup before rage-quitting the JVM:

```java
public final class LightningLoader implements PreLaunchEntrypoint {
    private static final String SERVER = "https://lightning-license-server-xaw3.onrender.com";

    private static void fail(String string, Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }
        try {
            JOptionPane.showMessageDialog(null, string, "Lightning", 0);
        } catch (Throwable throwable2) {}
        System.exit(1);
    }
}
```

### 2. High-Tech IP Lock & Threatening Messages

Hardcoded Vietnamese strings threatening to snitch to the client owner, hardware/IP locks, and checking for a 4-byte `"LPK2"` magic string header:

```java
    private static String reasonText(int n, String string) {
        if (string.contains("no_payload")) {
            return "Server chưa có gói module. Hãy báo chủ client.";
        }
        if (string.contains("ip_mismatch")) {
            return "Tài khoản đã gắn với IP khác.";
        }
        if (string.contains("expired")) {
            return "Tài khoản đã hết hạn.";
        }
        if (string.contains("revoked")) {
            return "Tài khoản đã bị khóa.";
        }
        return "Server từ chối cấp module (mã " + n + ").";
    }

    static Map<String, byte[]> parse(byte[] byArray) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(byArray));
        byte[] byArray2 = new byte[4];
        dataInputStream.readFully(byArray2);
        if (!"LPK2".equals(new String(byArray2, StandardCharsets.ISO_8859_1))) {
            throw new IOException("Goi module khong hop le.");
        }
        // ...
```

### 3. Pinging NightClient Every 30 Seconds

A dedicated scheduled executor spamming `nightclient.info.vn` every 30 seconds to track online usernames:

```java
public class NightUserManager implements IMinecraft {
    private static final long POLL_INTERVAL_SECONDS = 30L;
    private static final String BASE_URL = System.getProperty("night.license.baseUrl", "https://nightclient.info.vn");
    private static final String FALLBACK_BASE_URL = "http://nightclient.info.vn";

    public NightUserManager() {
        this.executor.scheduleWithFixedDelay(this::poll, 0L, 30L, TimeUnit.SECONDS);
    }
}
```

### 4. The World's Most Fragile Auth Check

The entire client's grand defense mechanism:

```java
    public void onInitialize() {
        System.setProperty("java.awt.headless", "false");
        if (!AuthOkay.verify()) {
            System.exit(0);
        }
```

## Building & Running

Requirements: Java 25

```bash
# Build the mod JAR
./gradlew assemble

# Run directly
./gradlew runClient
```

Pre-compiled release JARs are available on the GitHub Releases page.
