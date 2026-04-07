package org.app.core.utils;

import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility helpers for encryption and HMAC operations.
 * Important change: use AES/GCM/NoPadding (AHEAD) instead of AES/CBC/PKCS5Padding.
 * GCM provides authenticated encryption (integrity + confidentiality) and does not use padding.
 */
// can extend to develop more methods
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public abstract class EncryptionUtils {
    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final String ALGORITHM = "AES";
    // Use GCM mode which is AEAD and avoids padding issues
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final SecureRandom RANDOM = new SecureRandom();
    // 12 bytes (96 bits) is the recommended nonce size for GCM
    private static final int GCM_NONCE_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /**
     * Compute HMAC-SHA512 hex digest for input with the provided key.
     */
    public static String hmacSHA512(String inputData, String key) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            byte[] inputBytes = inputData.getBytes(StandardCharsets.UTF_8);

            Mac mac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, HMAC_SHA512);
            mac.init(secretKeySpec);

            byte[] hashValue = mac.doFinal(inputBytes);
            StringBuilder hash = new StringBuilder();
            for (byte b : hashValue) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new CryptoException("Failed to calculate HMAC-SHA512 hash", e);
        }
    }

    /**
     * Generate a random AES-256 key.
     */
    public static SecretKey generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to generate AES key", e);
        }
    }

    /**
     * Create a SecretKey from raw bytes (useful when loading keys from secure storage).
     */
    public static SecretKey keyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Encrypt a UTF-8 string using AES-GCM. Returns a base64 encoded string that contains
     * nonce || ciphertext || tag. The caller only needs to store the returned string.
     */
    public static String encrypt(String value, SecretKey key) {
        try {
            byte[] iv = new byte[GCM_NONCE_LENGTH];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] plain = value.getBytes(StandardCharsets.UTF_8);
            byte[] cipherText = cipher.doFinal(plain);

            // Prepend IV to ciphertext for transport: [IV || ciphertext]
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new CryptoException("Encryption failed", e);
        }
    }

    /**
     * Decrypt a base64 encoded payload produced by {@link #encrypt(String, SecretKey)}.
     * Expects the payload to contain [IV || ciphertext].
     */
    public static String decrypt(String base64Payload, SecretKey key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Payload);
            if (decoded.length < GCM_NONCE_LENGTH) {
                throw new CryptoException("Invalid payload: too short");
            }

            byte[] iv = new byte[GCM_NONCE_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);
            int cipherTextLength = decoded.length - iv.length;
            byte[] cipherText = new byte[cipherTextLength];
            System.arraycopy(decoded, iv.length, cipherText, 0, cipherTextLength);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (CryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptoException("Decryption failed", e);
        }
    }

    /**
     * Convenience method to encrypt with an externally provided raw IV (not recommended unless you
     * manage IV uniqueness). This is kept for compatibility with callers that previously passed IvParameterSpec.
     */
    public static String encrypt(String value, SecretKey key, IvParameterSpec ivSpec) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, ivSpec.getIV());
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] cipherText = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(ivSpec.getIV().length + cipherText.length);
            buffer.put(ivSpec.getIV());
            buffer.put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new CryptoException("Encryption failed", e);
        }
    }

    /**
     * Compatibility helper: decrypt when caller previously provided IvParameterSpec in encrypt.
     */
    public static String decrypt(String base64Payload, SecretKey key, IvParameterSpec ivSpec) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Payload);
            // If the encoded payload starts with the same IV provided, strip it; otherwise expect the payload to have IV as first bytes
            byte[] iv = ivSpec.getIV();
            byte[] cipherText;
            if (decoded.length > iv.length) {
                // if payload starts with IV, skip it
                boolean startsWithIv = true;
                for (int i = 0; i < iv.length; i++) {
                    if (decoded[i] != iv[i]) {
                        startsWithIv = false;
                        break;
                    }
                }
                if (startsWithIv) {
                    cipherText = new byte[decoded.length - iv.length];
                    System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);
                } else {
                    cipherText = decoded; // assume payload contains its own IV
                }
            } else {
                cipherText = new byte[0];
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("Decryption failed", e);
        }
    }

    public static String genId(String prefix, int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = CHARS[RANDOM.nextInt(CHARS.length)];
        }
        String id = new String(buf);
        return StringUtils.hasText(prefix)
            ? prefix + "_" + id
            : id;
    }

    public static class CryptoException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CryptoException(String message) {
            super(message);
        }

        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }

        public CryptoException(Throwable cause) {
            super(cause);
        }
    }
}
