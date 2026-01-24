package com.robsartin.graphs.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraphSavedEventTest {

    @Test
    @DisplayName("should create event with valid graphId")
    void shouldCreateEventWithValidGraphId() {
        UUID graphId = UUID.randomUUID();

        GraphSavedEvent event = new GraphSavedEvent(graphId);

        assertThat(event.graphId()).isEqualTo(graphId);
    }

    @Test
    @DisplayName("should throw exception for null graphId")
    void shouldThrowExceptionForNullGraphId() {
        assertThatThrownBy(() -> new GraphSavedEvent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("graphId must not be null");
    }
}
