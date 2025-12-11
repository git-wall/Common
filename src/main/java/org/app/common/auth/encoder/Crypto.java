package org.app.common.auth.encoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class Crypto implements PasswordEncoder {

    private static final String HMAC_SHA512 = "HmacSHA512";
    private static final String SHA_256 = "SHA-256";

    private final PasswordEncoder passwordEncoder;
    private final BCryptPasswordEncoder bcryptEncoder;

    public Crypto() {
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        bcryptEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public String encode(CharSequence rawPassword) {
       return passwordEncoder.encode(rawPassword);
    }

    public String encodeBCrypt(CharSequence rawPassword) {
        return bcryptEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean matchesBCrypt(CharSequence rawPassword, String encodedPassword) {
        return bcryptEncoder.matches(rawPassword, encodedPassword);
    }

    public static String hash(String input) {
        return DigestUtils.sha256Hex(input);
    }

    public static String hash(Object input) {
        return DigestUtils.sha256Hex(input.toString());
    }

    public static String encodeMD5(CharSequence input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.toString().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ignored) {
            // do nothing
        }

        return null;
    }

    public static String hmacSha512(String key, String data) {
        try {
            byte[] rawHmac = getBytes(key, data);

            // Return Base64 string (you can change to Hex if needed)
            return Base64.getEncoder().encodeToString(rawHmac);

        } catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
            // do nothing
        }

        return null;
    }

    public static String hmacSha512Append02X(String data, String key) {
        try {
            // Convert key and input data to bytes
            byte[] hashValue = getBytes(key, data);

            // Convert bytes to hex string
            StringBuilder hash = new StringBuilder();
            for (byte b : hashValue) {
                hash.append(String.format("%02x", b));
            }

            return hash.toString();

        } catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
            // do nothing
        }

        return null;
    }

    private static byte[] getBytes(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        // Convert key to bytes
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        // Create HMAC-SHA512 instance
        Mac mac = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, HMAC_SHA512);
        mac.init(secretKeySpec);

        // Compute HMAC on data
        return mac.doFinal(dataBytes);
    }

    public static String sha256(CharSequence input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(input.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ignored) {
            // do nothing
        }

        return null;
    }
}
