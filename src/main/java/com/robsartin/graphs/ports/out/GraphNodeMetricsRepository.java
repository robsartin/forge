package com.robsartin.graphs.ports.out;

import com.robsartin.graphs.models.GraphNodeMetrics;

import java.util.List;
import java.util.UUID;

/**
 * Port interface for per-node metrics persistence.
 */
public interface GraphNodeMetricsRepository {

    GraphNodeMetrics save(GraphNodeMetrics metrics);

    List<GraphNodeMetrics> saveAll(List<GraphNodeMetrics> metricsList);

    List<GraphNodeMetrics> findByGraphId(UUID graphId);

    void deleteByGraphId(UUID graphId);
}
