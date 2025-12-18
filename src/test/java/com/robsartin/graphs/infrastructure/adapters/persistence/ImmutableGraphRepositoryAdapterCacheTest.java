package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.config.CacheConfiguration;
import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import com.robsartin.graphs.models.ImmutableGraphEntity;
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
class ImmutableGraphRepositoryAdapterCacheTest {

    @Autowired
    private ImmutableGraphRepositoryAdapter immutableGraphRepositoryAdapter;

    @SpyBean
    private JpaImmutableGraphRepository jpaImmutableGraphRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        Objects.requireNonNull(cacheManager.getCache(CacheConfiguration.IMMUTABLE_GRAPHS_CACHE)).clear();
        Objects.requireNonNull(cacheManager.getCache(CacheConfiguration.IMMUTABLE_GRAPH_BY_ID_CACHE)).clear();
        jpaImmutableGraphRepository.deleteAll();
        reset(jpaImmutableGraphRepository);
    }

    @Test
    void findAllShouldCacheResult() {
        // Given
        ImmutableGraphEntity graph = new ImmutableGraphEntity();
        graph.setName("Test Graph");
        jpaImmutableGraphRepository.save(graph);
        reset(jpaImmutableGraphRepository);

        // When - call findAll twice
        immutableGraphRepositoryAdapter.findAll();
        immutableGraphRepositoryAdapter.findAll();

        // Then - JPA repository should only be called once
        verify(jpaImmutableGraphRepository, times(1)).findAll();
    }

    @Test
    void findByIdShouldCacheResult() {
        // Given
        ImmutableGraphEntity graph = new ImmutableGraphEntity();
        graph.setName("Test Graph");
        ImmutableGraphEntity savedGraph = jpaImmutableGraphRepository.save(graph);
        UUID graphId = savedGraph.getId();
        reset(jpaImmutableGraphRepository);

        // When - call findById twice with same ID
        immutableGraphRepositoryAdapter.findById(graphId);
        immutableGraphRepositoryAdapter.findById(graphId);

        // Then - JPA repository should only be called once
        verify(jpaImmutableGraphRepository, times(1)).findById(graphId);
    }

    @Test
    void findByIdShouldCacheEmptyResult() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When - call findById twice for non-existent ID
        Optional<ImmutableGraphEntity> result1 = immutableGraphRepositoryAdapter.findById(nonExistentId);
        Optional<ImmutableGraphEntity> result2 = immutableGraphRepositoryAdapter.findById(nonExistentId);

        // Then - JPA repository should only be called once
        verify(jpaImmutableGraphRepository, times(1)).findById(nonExistentId);
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
    }

    @Test
    void saveShouldEvictAllCaches() {
        // Given - populate caches
        ImmutableGraphEntity graph1 = new ImmutableGraphEntity();
        graph1.setName("Graph 1");
        jpaImmutableGraphRepository.save(graph1);
        immutableGraphRepositoryAdapter.findAll();
        immutableGraphRepositoryAdapter.findById(graph1.getId());
        reset(jpaImmutableGraphRepository);

        // When - save a new graph
        ImmutableGraphEntity graph2 = new ImmutableGraphEntity();
        graph2.setName("Graph 2");
        immutableGraphRepositoryAdapter.save(graph2);

        // Then - subsequent findAll should hit the database
        immutableGraphRepositoryAdapter.findAll();
        verify(jpaImmutableGraphRepository, times(1)).findAll();
    }

    @Test
    void saveShouldEvictFindByIdCache() {
        // Given - populate cache
        ImmutableGraphEntity graph = new ImmutableGraphEntity();
        graph.setName("Test Graph");
        ImmutableGraphEntity savedGraph = jpaImmutableGraphRepository.save(graph);
        immutableGraphRepositoryAdapter.findById(savedGraph.getId());
        reset(jpaImmutableGraphRepository);

        // When - save the graph again (update)
        savedGraph.setName("Updated Graph");
        immutableGraphRepositoryAdapter.save(savedGraph);

        // Then - subsequent findById should hit the database
        immutableGraphRepositoryAdapter.findById(savedGraph.getId());
        verify(jpaImmutableGraphRepository, times(1)).findById(savedGraph.getId());
    }

    @Test
    void deleteByIdShouldEvictAllCaches() {
        // Given - populate caches
        ImmutableGraphEntity graph = new ImmutableGraphEntity();
        graph.setName("Test Graph");
        ImmutableGraphEntity savedGraph = jpaImmutableGraphRepository.save(graph);
        immutableGraphRepositoryAdapter.findAll();
        immutableGraphRepositoryAdapter.findById(savedGraph.getId());
        reset(jpaImmutableGraphRepository);

        // When - delete the graph
        immutableGraphRepositoryAdapter.deleteById(savedGraph.getId());

        // Then - subsequent findAll and findById should hit the database
        immutableGraphRepositoryAdapter.findAll();
        immutableGraphRepositoryAdapter.findById(savedGraph.getId());

        verify(jpaImmutableGraphRepository, times(1)).findAll();
        verify(jpaImmutableGraphRepository, times(1)).findById(savedGraph.getId());
    }

    @Test
    void deleteAllShouldEvictAllCaches() {
        // Given - populate caches
        ImmutableGraphEntity graph = new ImmutableGraphEntity();
        graph.setName("Test Graph");
        ImmutableGraphEntity savedGraph = jpaImmutableGraphRepository.save(graph);
        immutableGraphRepositoryAdapter.findAll();
        immutableGraphRepositoryAdapter.findById(savedGraph.getId());
        reset(jpaImmutableGraphRepository);

        // When - delete all
        immutableGraphRepositoryAdapter.deleteAll();

        // Then - subsequent findAll should hit the database
        immutableGraphRepositoryAdapter.findAll();
        verify(jpaImmutableGraphRepository, times(1)).findAll();
    }

    @Test
    void differentIdsShouldBeCachedSeparately() {
        // Given
        ImmutableGraphEntity graph1 = new ImmutableGraphEntity();
        graph1.setName("Graph 1");
        ImmutableGraphEntity graph2 = new ImmutableGraphEntity();
        graph2.setName("Graph 2");
        ImmutableGraphEntity saved1 = jpaImmutableGraphRepository.save(graph1);
        ImmutableGraphEntity saved2 = jpaImmutableGraphRepository.save(graph2);
        reset(jpaImmutableGraphRepository);

        // When - call findById for both IDs multiple times
        immutableGraphRepositoryAdapter.findById(saved1.getId());
        immutableGraphRepositoryAdapter.findById(saved2.getId());
        immutableGraphRepositoryAdapter.findById(saved1.getId());
        immutableGraphRepositoryAdapter.findById(saved2.getId());

        // Then - each ID should only hit the database once
        verify(jpaImmutableGraphRepository, times(1)).findById(saved1.getId());
        verify(jpaImmutableGraphRepository, times(1)).findById(saved2.getId());
    }
}
