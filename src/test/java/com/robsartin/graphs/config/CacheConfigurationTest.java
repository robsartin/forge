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
    void shouldCreateImmutableGraphsCache() {
        var cache = cacheManager.getCache(CacheConfiguration.IMMUTABLE_GRAPHS_CACHE);
        assertNotNull(cache, "Immutable graphs cache should exist");
    }

    @Test
    void shouldCreateImmutableGraphByIdCache() {
        var cache = cacheManager.getCache(CacheConfiguration.IMMUTABLE_GRAPH_BY_ID_CACHE);
        assertNotNull(cache, "Immutable graph by ID cache should exist");
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
    void shouldEvictLeastRecentlyUsedEntries() {
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

        // Access key0 to make it recently used
        nativeCache.getIfPresent("key0");

        // Add more entries to trigger eviction
        for (int i = 100; i < 150; i++) {
            nativeCache.put("key" + i, "value" + i);
        }

        // Trigger cleanup
        nativeCache.cleanUp();

        // key0 should still be present (recently used)
        assertNotNull(nativeCache.getIfPresent("key0"),
            "Recently accessed entry should not be evicted");
    }
}
