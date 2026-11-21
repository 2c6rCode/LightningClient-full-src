/*
 * Decompiled with CFR 0.152.
 */
package lightning.core;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class AesCrypto {
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private AesCrypto() {
    }

    static byte[] decrypt(byte[] byArray, byte[] byArray2) {
        try {
            byte[] byArray3 = new byte[12];
            System.arraycopy(byArray, 0, byArray3, 0, 12);
            int n = byArray.length - 12;
            byte[] byArray4 = new byte[n];
            System.arraycopy(byArray, 12, byArray4, 0, n);
            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(2, (Key)new SecretKeySpec(byArray2, "AES"), new GCMParameterSpec(128, byArray3));
            return cipher.doFinal(byArray4);
        }
        catch (Exception exception) {
            throw new RuntimeException("AES decryption failed", exception);
        }
    }
}

