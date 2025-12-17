package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.models.ImmutableGraphEntity;
import com.robsartin.graphs.ports.out.ImmutableGraphRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<ImmutableGraphEntity> findById(UUID id) {
        return jpaImmutableGraphRepository.findById(id);
    }

    @Override
    public List<ImmutableGraphEntity> findAll() {
        return jpaImmutableGraphRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaImmutableGraphRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaImmutableGraphRepository.existsById(id);
    }

    @Override
    public void deleteAll() {
        jpaImmutableGraphRepository.deleteAll();
    }
}
