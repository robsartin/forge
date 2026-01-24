package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.GraphMetrics;
import com.robsartin.graphs.ports.out.GraphMetricsRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GraphMetricsRepositoryAdapter implements GraphMetricsRepository {

    private final JpaGraphMetricsRepository jpaRepository;

    public GraphMetricsRepositoryAdapter(JpaGraphMetricsRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GraphMetrics save(GraphMetrics metrics) {
        return jpaRepository.save(metrics);
    }

    @Override
    public Optional<GraphMetrics> findByGraphId(UUID graphId) {
        return jpaRepository.findByGraphId(graphId);
    }

    @Override
    public void deleteByGraphId(UUID graphId) {
        jpaRepository.deleteByGraphId(graphId);
    }
}
