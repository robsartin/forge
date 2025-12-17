package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.ImmutableGraphEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaImmutableGraphRepository extends JpaRepository<ImmutableGraphEntity, UUID> {
}
