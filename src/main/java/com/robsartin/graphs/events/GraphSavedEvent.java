package com.robsartin.graphs.events;

import java.util.UUID;

/**
 * Domain event published when a graph is saved.
 * Triggers async computation of graph metrics.
 */
public record GraphSavedEvent(UUID graphId) {

    public GraphSavedEvent {
        if (graphId == null) {
            throw new IllegalArgumentException("graphId must not be null");
        }
    }
}
