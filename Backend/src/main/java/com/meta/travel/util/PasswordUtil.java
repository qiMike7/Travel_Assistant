package com.meta.travel.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 密码加密工具：SHA-256 + 随机盐，存储格式为 {@code salt$hash}。
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SEPARATOR = "$";

    private PasswordUtil() {
    }

    public static String encode(String rawPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        String hash = sha256(saltHex + rawPassword);
        return saltHex + SEPARATOR + hash;
    }

    public static boolean matches(String rawPassword, String stored) {
        if (stored == null || !stored.contains(SEPARATOR)) {
            return false;
        }
        String[] parts = stored.split("\\" + SEPARATOR, 2);
        String saltHex = parts[0];
        String expectedHash = parts[1];
        String actualHash = sha256(saltHex + rawPassword);
        return MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.UTF_8),
                actualHash.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }
}
