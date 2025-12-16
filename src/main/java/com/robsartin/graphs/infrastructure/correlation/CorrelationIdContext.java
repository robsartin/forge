package com.robsartin.graphs.infrastructure.correlation;

import org.slf4j.MDC;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Holds the correlation ID for the current request context.
 * Uses ThreadLocal for thread-safe storage and MDC for logging integration.
 */
public final class CorrelationIdContext {

    public static final String CORRELATION_ID_HEADER = "Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    private CorrelationIdContext() {
    }

    /**
     * Sets the correlation ID for the current thread and adds it to MDC.
     *
     * @param correlationId the correlation ID to set
     */
    public static void setCorrelationId(String correlationId) {
        CORRELATION_ID.set(correlationId);
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    }

    /**
     * Gets the correlation ID for the current thread.
     *
     * @return the correlation ID, or null if not set
     */
    public static String getCorrelationId() {
        return CORRELATION_ID.get();
    }

    /**
     * Clears the correlation ID from the current thread and MDC.
     */
    public static void clear() {
        CORRELATION_ID.remove();
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }

    /**
     * Generates a UUID v7 (time-ordered UUID).
     * UUID v7 structure:
     * - 48 bits: Unix timestamp in milliseconds
     * - 4 bits: Version (7)
     * - 12 bits: Random
     * - 2 bits: Variant (10)
     * - 62 bits: Random
     *
     * @return a new UUID v7 as a string
     */
    public static String generateCorrelationId() {
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

        return new UUID(msb, lsb).toString();
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
