package com.example.stub.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomDataGenerator {
    private static final Random RANDOM = new SecureRandom();
    private static final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    public static byte[] generateRandomBytes(int size) {
        byte[] data = new byte[size];
        RANDOM.nextBytes(data);
        return data;
    }

    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }
}