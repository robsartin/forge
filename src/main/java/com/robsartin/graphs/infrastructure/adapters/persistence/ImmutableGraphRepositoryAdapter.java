package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.config.CacheConfiguration;
import com.robsartin.graphs.models.ImmutableGraphEntity;
import com.robsartin.graphs.ports.out.ImmutableGraphRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the ImmutableGraphRepository port using Spring Data JPA.
 * This adapter translates between the domain port interface and the JPA repository.
 * All read operations are cached with LRU eviction policy.
 */
@Component
public class ImmutableGraphRepositoryAdapter implements ImmutableGraphRepository {

    private final JpaImmutableGraphRepository jpaImmutableGraphRepository;

    public ImmutableGraphRepositoryAdapter(JpaImmutableGraphRepository jpaImmutableGraphRepository) {
        this.jpaImmutableGraphRepository = jpaImmutableGraphRepository;
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfiguration.IMMUTABLE_GRAPHS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfiguration.IMMUTABLE_GRAPH_BY_ID_CACHE, key = "#graph.id", condition = "#graph.id != null")
    })
    public ImmutableGraphEntity save(ImmutableGraphEntity graph) {
        return jpaImmutableGraphRepository.save(graph);
    }

    @Override
    @Cacheable(value = CacheConfiguration.IMMUTABLE_GRAPH_BY_ID_CACHE, key = "#id")
    public Optional<ImmutableGraphEntity> findById(UUID id) {
        return jpaImmutableGraphRepository.findById(id);
    }

    @Override
    @Cacheable(value = CacheConfiguration.IMMUTABLE_GRAPHS_CACHE)
    public List<ImmutableGraphEntity> findAll() {
        return jpaImmutableGraphRepository.findAll();
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfiguration.IMMUTABLE_GRAPHS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfiguration.IMMUTABLE_GRAPH_BY_ID_CACHE, key = "#id")
    })
    public void deleteById(UUID id) {
        jpaImmutableGraphRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaImmutableGraphRepository.existsById(id);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfiguration.IMMUTABLE_GRAPHS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfiguration.IMMUTABLE_GRAPH_BY_ID_CACHE, allEntries = true)
    })
    public void deleteAll() {
        jpaImmutableGraphRepository.deleteAll();
    }
}
