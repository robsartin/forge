package com.robsartin.graphs.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UuidV7Generator")
class UuidV7GeneratorTest {

    @Test
    @DisplayName("should generate valid UUID")
    void shouldGenerateValidUuid() {
        UUID uuid = UuidV7Generator.generate();

        assertThat(uuid).isNotNull();
        assertThat(uuid.toString()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("should generate UUID with version 7")
    void shouldGenerateVersion7() {
        UUID uuid = UuidV7Generator.generate();

        assertThat(uuid.version()).isEqualTo(7);
    }

    @Test
    @DisplayName("should generate UUID with correct variant")
    void shouldGenerateCorrectVariant() {
        UUID uuid = UuidV7Generator.generate();

        // Variant 2 (RFC 4122) — variant() returns 2 for standard UUIDs
        assertThat(uuid.variant()).isEqualTo(2);
    }

    @Test
    @DisplayName("should generate unique UUIDs")
    void shouldGenerateUniqueUuids() {
        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            uuids.add(UuidV7Generator.generate());
        }

        assertThat(uuids).hasSize(1000);
    }

    @Test
    @DisplayName("should generate time-ordered UUIDs")
    void shouldGenerateTimeOrderedUuids() throws InterruptedException {
        UUID first = UuidV7Generator.generate();
        Thread.sleep(2);
        UUID second = UuidV7Generator.generate();

        // UUID v7 embeds timestamp in the most significant bits,
        // so later UUIDs should compare greater
        assertThat(first.getMostSignificantBits()).isLessThan(second.getMostSignificantBits());
    }

    @Test
    @DisplayName("should generate valid UUID string")
    void shouldGenerateValidUuidString() {
        String uuidStr = UuidV7Generator.generateString();

        assertThat(uuidStr).isNotNull();
        assertThat(UUID.fromString(uuidStr)).isNotNull();
    }

    @Test
    @DisplayName("should validate valid UUID strings")
    void shouldValidateValidUuidStrings() {
        assertThat(UuidV7Generator.isValidUuid(UuidV7Generator.generateString())).isTrue();
        assertThat(UuidV7Generator.isValidUuid("550e8400-e29b-41d4-a716-446655440000")).isTrue();
    }

    @Test
    @DisplayName("should reject invalid UUID strings")
    void shouldRejectInvalidUuidStrings() {
        assertThat(UuidV7Generator.isValidUuid(null)).isFalse();
        assertThat(UuidV7Generator.isValidUuid("")).isFalse();
        assertThat(UuidV7Generator.isValidUuid("   ")).isFalse();
        assertThat(UuidV7Generator.isValidUuid("not-a-uuid")).isFalse();
        assertThat(UuidV7Generator.isValidUuid("550e8400-e29b-41d4-a716")).isFalse();
    }
}
