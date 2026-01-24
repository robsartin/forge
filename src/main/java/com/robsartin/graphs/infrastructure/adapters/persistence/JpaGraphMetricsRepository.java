package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.GraphMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaGraphMetricsRepository extends JpaRepository<GraphMetrics, UUID> {

    Optional<GraphMetrics> findByGraphId(UUID graphId);

    @Modifying
    @Query("DELETE FROM GraphMetrics m WHERE m.graph.id = :graphId")
    void deleteByGraphId(UUID graphId);
}
