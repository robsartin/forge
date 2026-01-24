package com.robsartin.graphs.ports.out;

import com.robsartin.graphs.models.GraphMetrics;

import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for graph metrics persistence.
 */
public interface GraphMetricsRepository {

    GraphMetrics save(GraphMetrics metrics);

    Optional<GraphMetrics> findByGraphId(UUID graphId);

    void deleteByGraphId(UUID graphId);
}
