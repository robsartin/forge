package com.robsartin.graphs.infrastructure.correlation;

import com.robsartin.graphs.infrastructure.UuidV7Generator;
import org.slf4j.MDC;

/**
 * Holds the correlation ID for the current request context.
 * Uses ThreadLocal for thread-safe storage and MDC for logging integration.
 */
public final class CorrelationIdContext {

    public static final String CORRELATION_ID_HEADER = "Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

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
     * Generates a UUID v7 (time-ordered UUID) as a correlation ID.
     *
     * @return a new UUID v7 as a string
     */
    public static String generateCorrelationId() {
        return UuidV7Generator.generateString();
    }

    /**
     * Validates if a string is a valid UUID format.
     *
     * @param uuid the string to validate
     * @return true if valid UUID format, false otherwise
     */
    public static boolean isValidUuid(String uuid) {
        return UuidV7Generator.isValidUuid(uuid);
    }
}
