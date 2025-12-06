package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.domain.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository interface for Item entity.
 * This interface extends JpaRepository to provide CRUD operations.
 */
@Repository
public interface JpaItemRepository extends JpaRepository<Item, Integer> {
}
