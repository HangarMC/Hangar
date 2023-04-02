package io.papermc.hangar.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jetbrains.annotations.NotNull;
import org.postgresql.shaded.com.ongres.scram.common.util.Preconditions;

public final class CryptoUtils {

    private CryptoUtils() {
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static byte[] hmac(final String algo, final byte[] secret, final byte[] data) {
        Preconditions.checkArgument(secret.length != 0, "empty secret");
        Preconditions.checkArgument(data.length != 0, "nothing to hash!");
        try {
            final Mac hmac = Mac.getInstance(algo);
            final SecretKeySpec keySpec = new SecretKeySpec(secret, algo);
            hmac.init(keySpec);
            return hmac.doFinal(data);
        } catch (final InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String hmacSha256(final String secret, final byte[] data) {
        return bytesToHex(hmac("HmacSHA256", secret.getBytes(StandardCharsets.UTF_8), data));
    }

    public static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        int j = 0;
        while (j < bytes.length) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];

            j++;
        }
        return new String(hexChars);
    }

    public static String sha256ToHex(final byte[] bytes) {
        try {
            return bytesToHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String sha1ToHex(final byte[] bytes) {
        try {
            return bytesToHex(MessageDigest.getInstance("SHA-1").digest(bytes));
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull String md5ToHex(final byte[] bytes) {
        try {
            return bytesToHex(MessageDigest.getInstance("MD5").digest(bytes));
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
