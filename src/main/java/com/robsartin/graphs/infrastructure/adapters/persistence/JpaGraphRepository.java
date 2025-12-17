package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.Graph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaGraphRepository extends JpaRepository<Graph, UUID> {
}
