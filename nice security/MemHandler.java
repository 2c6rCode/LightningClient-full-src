/*
 * Decompiled with CFR 0.152.
 */
package lightning.loader;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lightning.loader.MemScan;

final class MemHandler
extends URLStreamHandler {
    private final Map<String, byte[]> files;

    MemHandler(Map<String, byte[]> map) {
        this.files = map;
        MemScan.register(map);
    }

    @Override
    protected URLConnection openConnection(URL uRL) throws IOException {
        byte[] byArray;
        String string = uRL.getPath();
        if (string.startsWith("/")) {
            string = string.substring(1);
        }
        if ((byArray = this.files.get(string = URLDecoder.decode(string, StandardCharsets.UTF_8))) == null) {
            throw new FileNotFoundException(string);
        }
        return new URLConnection(uRL){

            @Override
            public void connect() {
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(byArray);
            }

            @Override
            public int getContentLength() {
                return byArray.length;
            }

            @Override
            public long getContentLengthLong() {
                return byArray.length;
            }
        };
    }
}

