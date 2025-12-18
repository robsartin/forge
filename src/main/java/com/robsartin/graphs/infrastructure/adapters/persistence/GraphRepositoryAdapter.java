package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.config.CacheConfiguration;
import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.ports.out.GraphRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the GraphRepository port using Spring Data JPA.
 * This adapter translates between the domain port interface and the JPA repository.
 * All read operations are cached with LRU eviction policy.
 */
@Component
public class GraphRepositoryAdapter implements GraphRepository {

    private final JpaGraphRepository jpaGraphRepository;

    public GraphRepositoryAdapter(JpaGraphRepository jpaGraphRepository) {
        this.jpaGraphRepository = jpaGraphRepository;
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfiguration.GRAPHS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfiguration.GRAPH_BY_ID_CACHE, key = "#graph.id", condition = "#graph.id != null")
    })
    public Graph save(Graph graph) {
        return jpaGraphRepository.save(graph);
    }

    @Override
    @Cacheable(value = CacheConfiguration.GRAPH_BY_ID_CACHE, key = "#id")
    public Optional<Graph> findById(UUID id) {
        return jpaGraphRepository.findById(id);
    }

    @Override
    @Cacheable(value = CacheConfiguration.GRAPHS_CACHE)
    public List<Graph> findAll() {
        return jpaGraphRepository.findAll();
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfiguration.GRAPHS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfiguration.GRAPH_BY_ID_CACHE, key = "#id")
    })
    public void deleteById(UUID id) {
        jpaGraphRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaGraphRepository.existsById(id);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfiguration.GRAPHS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfiguration.GRAPH_BY_ID_CACHE, allEntries = true)
    })
    public void deleteAll() {
        jpaGraphRepository.deleteAll();
    }
}
