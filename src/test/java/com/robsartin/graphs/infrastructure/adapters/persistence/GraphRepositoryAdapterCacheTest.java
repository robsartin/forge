package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.config.CacheConfiguration;
import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import com.robsartin.graphs.models.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestOpenFeatureConfiguration.class)
@Transactional
class GraphRepositoryAdapterCacheTest {

    @Autowired
    private GraphRepositoryAdapter graphRepositoryAdapter;

    @SpyBean
    private JpaGraphRepository jpaGraphRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        Objects.requireNonNull(cacheManager.getCache(CacheConfiguration.GRAPHS_CACHE)).clear();
        Objects.requireNonNull(cacheManager.getCache(CacheConfiguration.GRAPH_BY_ID_CACHE)).clear();
        jpaGraphRepository.deleteAll();
        reset(jpaGraphRepository);
    }

    @Test
    void findAllShouldCacheResult() {
        // Given
        Graph graph = new Graph("Test Graph");
        jpaGraphRepository.save(graph);
        reset(jpaGraphRepository); // Reset to count only subsequent calls

        // When - call findAll twice
        graphRepositoryAdapter.findAll();
        graphRepositoryAdapter.findAll();

        // Then - JPA repository should only be called once (second call uses cache)
        verify(jpaGraphRepository, times(1)).findAll();
    }

    @Test
    void findByIdShouldCacheResult() {
        // Given
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = jpaGraphRepository.save(graph);
        UUID graphId = savedGraph.getId();
        reset(jpaGraphRepository);

        // When - call findById twice with same ID
        graphRepositoryAdapter.findById(graphId);
        graphRepositoryAdapter.findById(graphId);

        // Then - JPA repository should only be called once
        verify(jpaGraphRepository, times(1)).findById(graphId);
    }

    @Test
    void findByIdShouldCacheEmptyResult() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When - call findById twice for non-existent ID
        Optional<Graph> result1 = graphRepositoryAdapter.findById(nonExistentId);
        Optional<Graph> result2 = graphRepositoryAdapter.findById(nonExistentId);

        // Then - JPA repository should only be called once
        verify(jpaGraphRepository, times(1)).findById(nonExistentId);
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
    }

    @Test
    void saveShouldEvictAllCaches() {
        // Given - populate caches
        Graph graph1 = new Graph("Graph 1");
        jpaGraphRepository.save(graph1);
        graphRepositoryAdapter.findAll(); // Populate findAll cache
        graphRepositoryAdapter.findById(graph1.getId()); // Populate findById cache
        reset(jpaGraphRepository);

        // When - save a new graph
        Graph graph2 = new Graph("Graph 2");
        graphRepositoryAdapter.save(graph2);

        // Then - subsequent findAll should hit the database (cache was evicted)
        graphRepositoryAdapter.findAll();
        verify(jpaGraphRepository, times(1)).findAll();
    }

    @Test
    void saveShouldEvictFindByIdCache() {
        // Given - populate cache
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = jpaGraphRepository.save(graph);
        graphRepositoryAdapter.findById(savedGraph.getId());
        reset(jpaGraphRepository);

        // When - save the graph again (update)
        savedGraph.setName("Updated Graph");
        graphRepositoryAdapter.save(savedGraph);

        // Then - subsequent findById should hit the database
        graphRepositoryAdapter.findById(savedGraph.getId());
        verify(jpaGraphRepository, times(1)).findById(savedGraph.getId());
    }

    @Test
    void deleteByIdShouldEvictAllCaches() {
        // Given - populate caches
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = jpaGraphRepository.save(graph);
        graphRepositoryAdapter.findAll();
        graphRepositoryAdapter.findById(savedGraph.getId());
        reset(jpaGraphRepository);

        // When - delete the graph
        graphRepositoryAdapter.deleteById(savedGraph.getId());

        // Then - subsequent findAll and findById should hit the database
        graphRepositoryAdapter.findAll();
        graphRepositoryAdapter.findById(savedGraph.getId());

        verify(jpaGraphRepository, times(1)).findAll();
        verify(jpaGraphRepository, times(1)).findById(savedGraph.getId());
    }

    @Test
    void deleteAllShouldEvictAllCaches() {
        // Given - populate caches
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = jpaGraphRepository.save(graph);
        graphRepositoryAdapter.findAll();
        graphRepositoryAdapter.findById(savedGraph.getId());
        reset(jpaGraphRepository);

        // When - delete all
        graphRepositoryAdapter.deleteAll();

        // Then - subsequent findAll should hit the database
        graphRepositoryAdapter.findAll();
        verify(jpaGraphRepository, times(1)).findAll();
    }

    @Test
    void differentIdsShouldBeCachedSeparately() {
        // Given
        Graph graph1 = new Graph("Graph 1");
        Graph graph2 = new Graph("Graph 2");
        Graph saved1 = jpaGraphRepository.save(graph1);
        Graph saved2 = jpaGraphRepository.save(graph2);
        reset(jpaGraphRepository);

        // When - call findById for both IDs multiple times
        graphRepositoryAdapter.findById(saved1.getId());
        graphRepositoryAdapter.findById(saved2.getId());
        graphRepositoryAdapter.findById(saved1.getId());
        graphRepositoryAdapter.findById(saved2.getId());

        // Then - each ID should only hit the database once
        verify(jpaGraphRepository, times(1)).findById(saved1.getId());
        verify(jpaGraphRepository, times(1)).findById(saved2.getId());
    }
}
