package com.robsartin.graphs.infrastructure;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utility class for generating UUID v7 (time-ordered UUID).
 * UUID v7 structure:
 * - 48 bits: Unix timestamp in milliseconds
 * - 4 bits: Version (7)
 * - 12 bits: Random
 * - 2 bits: Variant (10)
 * - 62 bits: Random
 */
public final class UuidV7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7Generator() {
    }

    /**
     * Generates a UUID v7 (time-ordered UUID).
     *
     * @return a new UUID v7
     */
    public static UUID generate() {
        long timestamp = System.currentTimeMillis();

        // Generate random bytes for the random portion
        byte[] randomBytes = new byte[10];
        RANDOM.nextBytes(randomBytes);

        // Build the most significant bits (48 bits timestamp + 4 bits version + 12 bits random)
        long msb = (timestamp << 16) | (7L << 12) | ((randomBytes[0] & 0xFF) << 4) | ((randomBytes[1] & 0xF0) >> 4);

        // Build the least significant bits (2 bits variant + 62 bits random)
        long lsb = (2L << 62) // Variant bits (10)
                | ((randomBytes[1] & 0x0FL) << 58)
                | ((randomBytes[2] & 0xFFL) << 50)
                | ((randomBytes[3] & 0xFFL) << 42)
                | ((randomBytes[4] & 0xFFL) << 34)
                | ((randomBytes[5] & 0xFFL) << 26)
                | ((randomBytes[6] & 0xFFL) << 18)
                | ((randomBytes[7] & 0xFFL) << 10)
                | ((randomBytes[8] & 0xFFL) << 2)
                | ((randomBytes[9] & 0xC0L) >> 6);

        return new UUID(msb, lsb);
    }

    /**
     * Generates a UUID v7 as a string.
     *
     * @return a new UUID v7 as a string
     */
    public static String generateString() {
        return generate().toString();
    }

    /**
     * Validates if a string is a valid UUID format.
     *
     * @param uuid the string to validate
     * @return true if valid UUID format, false otherwise
     */
    public static boolean isValidUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
