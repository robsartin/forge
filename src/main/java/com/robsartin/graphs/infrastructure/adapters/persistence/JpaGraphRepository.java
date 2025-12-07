package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.domain.models.Graph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaGraphRepository extends JpaRepository<Graph, Integer> {
}
