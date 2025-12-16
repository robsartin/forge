package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.ImmutableGraphEntity;
import com.robsartin.graphs.ports.out.ImmutableGraphRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements the ImmutableGraphRepository port using Spring Data JPA.
 * This adapter translates between the domain port interface and the JPA repository.
 */
@Component
public class ImmutableGraphRepositoryAdapter implements ImmutableGraphRepository {

    private final JpaImmutableGraphRepository jpaImmutableGraphRepository;

    public ImmutableGraphRepositoryAdapter(JpaImmutableGraphRepository jpaImmutableGraphRepository) {
        this.jpaImmutableGraphRepository = jpaImmutableGraphRepository;
    }

    @Override
    public ImmutableGraphEntity save(ImmutableGraphEntity graph) {
        return jpaImmutableGraphRepository.save(graph);
    }

    @Override
    public Optional<ImmutableGraphEntity> findById(Integer graphId) {
        return jpaImmutableGraphRepository.findById(graphId);
    }

    @Override
    public List<ImmutableGraphEntity> findAll() {
        return jpaImmutableGraphRepository.findAll();
    }

    @Override
    public void deleteById(Integer graphId) {
        jpaImmutableGraphRepository.deleteById(graphId);
    }

    @Override
    public boolean existsById(Integer graphId) {
        return jpaImmutableGraphRepository.existsById(graphId);
    }

    @Override
    public void deleteAll() {
        jpaImmutableGraphRepository.deleteAll();
    }
}
