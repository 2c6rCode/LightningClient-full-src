/*
 * Decompiled with CFR 0.152.
 */
package lightning.loader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.InflaterInputStream;

final class Payload {
    private Payload() {
    }

    static Map<String, byte[]> fetch(String string, String string2) throws Exception {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30L)).build();
        Exception exception = null;
        for (int i = 1; i <= 3; ++i) {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(string + "/module/payload")).timeout(Duration.ofSeconds(120L)).header("Authorization", "Bearer " + string2).GET().build();
                HttpResponse<byte[]> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
                int n = httpResponse.statusCode();
                if (n == 200) {
                    return Payload.parse(httpResponse.body());
                }
                String string3 = new String(httpResponse.body(), StandardCharsets.UTF_8);
                if (n == 401 || n == 403 || n == 404) {
                    throw new FetchException(Payload.reasonText(n, string3), true);
                }
                exception = new FetchException("Server tra ve ma " + n, false);
            }
            catch (FetchException fetchException) {
                if (fetchException.fatal) {
                    throw fetchException;
                }
                exception = fetchException;
            }
            catch (Exception exception2) {
                exception = exception2;
            }
            if (i >= 3) continue;
            Thread.sleep(3000L * (long)i);
        }
        throw exception;
    }

    private static String reasonText(int n, String string) {
        if (string.contains("no_payload")) {
            return "Server ch\u01b0a c\u00f3 g\u00f3i module. H\u00e3y b\u00e1o ch\u1ee7 client.";
        }
        if (string.contains("ip_mismatch")) {
            return "T\u00e0i kho\u1ea3n \u0111\u00e3 g\u1eafn v\u1edbi IP kh\u00e1c.";
        }
        if (string.contains("expired")) {
            return "T\u00e0i kho\u1ea3n \u0111\u00e3 h\u1ebft h\u1ea1n.";
        }
        if (string.contains("revoked")) {
            return "T\u00e0i kho\u1ea3n \u0111\u00e3 b\u1ecb kh\u00f3a.";
        }
        return "Server t\u1eeb ch\u1ed1i c\u1ea5p module (m\u00e3 " + n + ").";
    }

    static Map<String, byte[]> parse(byte[] byArray) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(byArray));
        byte[] byArray2 = new byte[4];
        dataInputStream.readFully(byArray2);
        if (!"LPK2".equals(new String(byArray2, StandardCharsets.ISO_8859_1))) {
            throw new IOException("Goi module khong hop le.");
        }
        int n = dataInputStream.readInt();
        if (n < 0 || n > 100000) {
            throw new IOException("Goi module khong hop le.");
        }
        HashMap<String, byte[]> hashMap = new HashMap<String, byte[]>(n * 2);
        for (int i = 0; i < n; ++i) {
            int n2 = dataInputStream.readUnsignedShort();
            byte[] byArray3 = new byte[n2];
            dataInputStream.readFully(byArray3);
            String string = new String(byArray3, StandardCharsets.UTF_8);
            int n3 = dataInputStream.readUnsignedByte();
            int n4 = dataInputStream.readInt();
            int n5 = dataInputStream.readInt();
            if (n4 < 0 || n5 < 0 || n4 > 64000000 || n5 > 64000000) {
                throw new IOException("Goi module khong hop le.");
            }
            byte[] byArray4 = new byte[n5];
            dataInputStream.readFully(byArray4);
            if (n3 == 1) {
                byte[] byArray5 = new byte[n4];
                try (DataInputStream dataInputStream2 = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(byArray4)));){
                    dataInputStream2.readFully(byArray5);
                }
                byArray4 = byArray5;
            }
            hashMap.put(string, byArray4);
        }
        return hashMap;
    }

    static final class FetchException
    extends Exception {
        final boolean fatal;

        FetchException(String string, boolean bl) {
            super(string);
            this.fatal = bl;
        }
    }
}

