package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.GraphNodeMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaGraphNodeMetricsRepository extends JpaRepository<GraphNodeMetrics, UUID> {

    List<GraphNodeMetrics> findByGraphId(UUID graphId);

    @Modifying
    @Query("DELETE FROM GraphNodeMetrics m WHERE m.graph.id = :graphId")
    void deleteByGraphId(UUID graphId);
}
