/*
 * Decompiled with CFR 0.152.
 */
package lightning.core;

import java.io.IOException;
import java.io.InputStream;
import lightning.core.AesCrypto;

final class PayloadClassLoader
extends ClassLoader {
    private final ClassLoader jarResourceLoader;
    private final byte[] key;

    PayloadClassLoader(ClassLoader classLoader, ClassLoader classLoader2, byte[] byArray) {
        super(classLoader);
        this.jarResourceLoader = classLoader2;
        this.key = byArray;
    }

    @Override
    protected Class<?> findClass(String string) throws ClassNotFoundException {
        String string2 = string.replace('.', '/') + ".enc";
        try (InputStream inputStream = this.jarResourceLoader.getResourceAsStream(string2)) {
            if (inputStream == null) {
                throw new ClassNotFoundException(string);
            }
            byte[] byArray = inputStream.readAllBytes();
            byte[] byArray2 = AesCrypto.decrypt(byArray, this.key);
            return this.defineClass(string, byArray2, 0, byArray2.length);
        } catch (IOException iOException) {
            throw new ClassNotFoundException(string, iOException);
        }
    }
}

