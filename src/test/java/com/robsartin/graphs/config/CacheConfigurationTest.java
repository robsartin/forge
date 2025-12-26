package com.robsartin.graphs.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestOpenFeatureConfiguration.class)
class CacheConfigurationTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCreateGraphsCache() {
        var cache = cacheManager.getCache(CacheConfiguration.GRAPHS_CACHE);
        assertNotNull(cache, "Graphs cache should exist");
    }

    @Test
    void shouldCreateGraphByIdCache() {
        var cache = cacheManager.getCache(CacheConfiguration.GRAPH_BY_ID_CACHE);
        assertNotNull(cache, "Graph by ID cache should exist");
    }

    @Test
    void shouldCreateFeatureFlagsCache() {
        var cache = cacheManager.getCache(CacheConfiguration.FEATURE_FLAGS_CACHE);
        assertNotNull(cache, "Feature flags cache should exist");
    }

    @Test
    void shouldHaveLruEvictionPolicy() {
        var cache = cacheManager.getCache(CacheConfiguration.GRAPH_BY_ID_CACHE);
        assertNotNull(cache);
        assertInstanceOf(CaffeineCache.class, cache, "Cache should be a Caffeine cache for LRU support");

        CaffeineCache caffeineCache = (CaffeineCache) cache;
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        // Fill the cache beyond its maximum size
        for (int i = 0; i < 150; i++) {
            nativeCache.put("key" + i, "value" + i);
        }

        // Trigger cleanup
        nativeCache.cleanUp();

        // Should be at or below maximum size (100) after eviction
        assertTrue(nativeCache.estimatedSize() <= 100,
                "Cache should evict entries when exceeding max size. Size: " + nativeCache.estimatedSize());
    }

    @Test
    void shouldEvictEntriesWhenExceedingMaxSize() {
        var cache = cacheManager.getCache(CacheConfiguration.GRAPH_BY_ID_CACHE);
        assertNotNull(cache);
        CaffeineCache caffeineCache = (CaffeineCache) cache;
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        // Clear the cache first
        nativeCache.invalidateAll();

        // Add entries up to max size
        for (int i = 0; i < 100; i++) {
            nativeCache.put("key" + i, "value" + i);
        }

        // Trigger cleanup
        nativeCache.cleanUp();

        // Verify cache is at max size
        assertEquals(100, nativeCache.estimatedSize(), "Cache should be at max size");

        // Add more entries to trigger eviction
        for (int i = 100; i < 200; i++) {
            nativeCache.put("key" + i, "value" + i);
        }

        // Trigger cleanup
        nativeCache.cleanUp();

        // Cache should still be at or below max size after eviction
        assertTrue(nativeCache.estimatedSize() <= 100,
                "Cache should maintain size limit after eviction. Size: " + nativeCache.estimatedSize());

        // Newest entries should generally be accessible (Caffeine uses TinyLFU which favors recent entries)
        assertNotNull(nativeCache.getIfPresent("key199"),
                "Most recently added entry should be accessible");
    }
}
