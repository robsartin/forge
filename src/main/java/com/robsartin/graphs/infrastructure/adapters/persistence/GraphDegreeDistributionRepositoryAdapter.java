package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.GraphDegreeDistribution;
import com.robsartin.graphs.ports.out.GraphDegreeDistributionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GraphDegreeDistributionRepositoryAdapter implements GraphDegreeDistributionRepository {

    private final JpaGraphDegreeDistributionRepository jpaRepository;

    public GraphDegreeDistributionRepositoryAdapter(JpaGraphDegreeDistributionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public GraphDegreeDistribution save(GraphDegreeDistribution distribution) {
        return jpaRepository.save(distribution);
    }

    @Override
    public List<GraphDegreeDistribution> saveAll(List<GraphDegreeDistribution> distributionList) {
        return jpaRepository.saveAll(distributionList);
    }

    @Override
    public List<GraphDegreeDistribution> findByGraphId(UUID graphId) {
        return jpaRepository.findByGraphId(graphId);
    }

    @Override
    public void deleteByGraphId(UUID graphId) {
        jpaRepository.deleteByGraphId(graphId);
    }
}
