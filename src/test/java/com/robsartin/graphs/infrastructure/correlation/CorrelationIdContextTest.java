package com.robsartin.graphs.infrastructure.correlation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorrelationIdContext")
class CorrelationIdContextTest {

    @AfterEach
    void tearDown() {
        CorrelationIdContext.clear();
    }

    @Test
    @DisplayName("should set and get correlation ID")
    void shouldSetAndGetCorrelationId() {
        String id = "test-correlation-id";

        CorrelationIdContext.setCorrelationId(id);

        assertThat(CorrelationIdContext.getCorrelationId()).isEqualTo(id);
    }

    @Test
    @DisplayName("should add correlation ID to MDC")
    void shouldAddToMdc() {
        String id = "mdc-test-id";

        CorrelationIdContext.setCorrelationId(id);

        assertThat(MDC.get(CorrelationIdContext.CORRELATION_ID_MDC_KEY)).isEqualTo(id);
    }

    @Test
    @DisplayName("should clear correlation ID and MDC")
    void shouldClearCorrelationId() {
        CorrelationIdContext.setCorrelationId("to-be-cleared");

        CorrelationIdContext.clear();

        assertThat(CorrelationIdContext.getCorrelationId()).isNull();
        assertThat(MDC.get(CorrelationIdContext.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("should return null when no correlation ID set")
    void shouldReturnNullWhenNotSet() {
        assertThat(CorrelationIdContext.getCorrelationId()).isNull();
    }

    @Test
    @DisplayName("should generate valid UUID correlation ID")
    void shouldGenerateValidCorrelationId() {
        String id = CorrelationIdContext.generateCorrelationId();

        assertThat(id).isNotNull();
        assertThat(CorrelationIdContext.isValidUuid(id)).isTrue();
    }

    @Test
    @DisplayName("should validate valid UUID strings")
    void shouldValidateValidUuids() {
        assertThat(CorrelationIdContext.isValidUuid("550e8400-e29b-41d4-a716-446655440000")).isTrue();
    }

    @Test
    @DisplayName("should reject invalid UUID strings")
    void shouldRejectInvalidUuids() {
        assertThat(CorrelationIdContext.isValidUuid(null)).isFalse();
        assertThat(CorrelationIdContext.isValidUuid("")).isFalse();
        assertThat(CorrelationIdContext.isValidUuid("invalid")).isFalse();
    }

    @Test
    @DisplayName("should isolate correlation IDs between threads")
    void shouldIsolateBetweenThreads() throws InterruptedException {
        CorrelationIdContext.setCorrelationId("main-thread-id");

        Thread otherThread = new Thread(() -> {
            assertThat(CorrelationIdContext.getCorrelationId()).isNull();
            CorrelationIdContext.setCorrelationId("other-thread-id");
            assertThat(CorrelationIdContext.getCorrelationId()).isEqualTo("other-thread-id");
            CorrelationIdContext.clear();
        });
        otherThread.start();
        otherThread.join();

        assertThat(CorrelationIdContext.getCorrelationId()).isEqualTo("main-thread-id");
    }
}
