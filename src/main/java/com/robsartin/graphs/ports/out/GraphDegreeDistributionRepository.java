package com.robsartin.graphs.ports.out;

import com.robsartin.graphs.models.GraphDegreeDistribution;

import java.util.List;
import java.util.UUID;

/**
 * Port interface for degree distribution persistence.
 */
public interface GraphDegreeDistributionRepository {

    GraphDegreeDistribution save(GraphDegreeDistribution distribution);

    List<GraphDegreeDistribution> saveAll(List<GraphDegreeDistribution> distributionList);

    List<GraphDegreeDistribution> findByGraphId(UUID graphId);

    void deleteByGraphId(UUID graphId);
}
