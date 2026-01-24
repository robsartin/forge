package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.GraphNodeMetrics;
import com.robsartin.graphs.ports.out.GraphNodeMetricsRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GraphNodeMetricsRepositoryAdapter implements GraphNodeMetricsRepository {

    private final JpaGraphNodeMetricsRepository jpaRepository;

    public GraphNodeMetricsRepositoryAdapter(JpaGraphNodeMetricsRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GraphNodeMetrics save(GraphNodeMetrics metrics) {
        return jpaRepository.save(metrics);
    }

    @Override
    public List<GraphNodeMetrics> saveAll(List<GraphNodeMetrics> metricsList) {
        return jpaRepository.saveAll(metricsList);
    }

    @Override
    public List<GraphNodeMetrics> findByGraphId(UUID graphId) {
        return jpaRepository.findByGraphId(graphId);
    }

    @Override
    public void deleteByGraphId(UUID graphId) {
        jpaRepository.deleteByGraphId(graphId);
    }
}
