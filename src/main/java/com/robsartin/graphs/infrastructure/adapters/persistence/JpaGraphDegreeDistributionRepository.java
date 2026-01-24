package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.GraphDegreeDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaGraphDegreeDistributionRepository extends JpaRepository<GraphDegreeDistribution, UUID> {

    List<GraphDegreeDistribution> findByGraphId(UUID graphId);

    @Modifying
    @Query("DELETE FROM GraphDegreeDistribution d WHERE d.graph.id = :graphId")
    void deleteByGraphId(UUID graphId);
}
