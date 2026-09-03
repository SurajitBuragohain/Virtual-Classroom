package com.vc.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public final class Password {
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private Password() {
    }

    public static String hash(String password) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt);
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }

    public static boolean verify(String inputPassword, String storedHash) {
        if (inputPassword == null || storedHash == null) {
            return false;
        }
        if (storedHash.contains(":")) {
            return verifyPbkdf2(inputPassword, storedHash);
        }
        return verifyLegacySha256(inputPassword, storedHash);
    }

    public static boolean isLegacyHash(String storedHash) {
        return storedHash != null && !storedHash.contains(":") && storedHash.matches("(?i)[0-9a-f]{64}");
    }

    private static boolean verifyPbkdf2(String inputPassword, String storedHash) {
        try {
            String[] parts = storedHash.split(":", -1);
            if (parts.length != 2) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] originalHash = Base64.getDecoder().decode(parts[1]);
            byte[] verificationHash = pbkdf2(inputPassword.toCharArray(), salt);
            return MessageDigest.isEqual(originalHash, verificationHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean verifyLegacySha256(String inputPassword, String storedHash) {
        if (!isLegacyHash(storedHash)) {
            return false;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(inputPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return MessageDigest.isEqual(hex.toString().getBytes(StandardCharsets.US_ASCII), storedHash.toLowerCase().getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
