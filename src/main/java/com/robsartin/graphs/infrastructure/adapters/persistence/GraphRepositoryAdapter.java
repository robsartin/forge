package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.config.CacheConfiguration;
import com.robsartin.graphs.events.GraphSavedEvent;
import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.ports.out.GraphRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
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

    private static final Logger log = LoggerFactory.getLogger(GraphRepositoryAdapter.class);

    private final JpaGraphRepository jpaGraphRepository;
    private final ApplicationEventPublisher eventPublisher;

    public GraphRepositoryAdapter(JpaGraphRepository jpaGraphRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.jpaGraphRepository = jpaGraphRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfiguration.GRAPHS_CACHE, allEntries = true),
        @CacheEvict(value = CacheConfiguration.GRAPH_BY_ID_CACHE, key = "#graph.id", condition = "#graph.id != null")
    })
    public Graph save(Graph graph) {
        Graph savedGraph = jpaGraphRepository.save(graph);
        log.info("Publishing GraphSavedEvent for graph {}", savedGraph.getId());
        eventPublisher.publishEvent(new GraphSavedEvent(savedGraph.getId()));
        return savedGraph;
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
