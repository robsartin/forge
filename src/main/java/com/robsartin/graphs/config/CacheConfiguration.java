package com.robsartin.graphs.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for application caching using Caffeine.
 * All caches are configured with LRU (Least Recently Used) eviction policy.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    public static final String GRAPHS_CACHE = "graphs";
    public static final String GRAPH_BY_ID_CACHE = "graphById";
    public static final String FEATURE_FLAGS_CACHE = "featureFlags";

    private static final int DEFAULT_MAX_SIZE = 100;
    private static final int FEATURE_FLAGS_MAX_SIZE = 50;
    private static final long FEATURE_FLAGS_EXPIRE_MINUTES = 5;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCacheBuilder());
        cacheManager.registerCustomCache(GRAPHS_CACHE, graphsCacheBuilder().build());
        cacheManager.registerCustomCache(GRAPH_BY_ID_CACHE, graphByIdCacheBuilder().build());
        cacheManager.registerCustomCache(FEATURE_FLAGS_CACHE, featureFlagsCacheBuilder().build());
        return cacheManager;
    }

    private Caffeine<Object, Object> defaultCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(DEFAULT_MAX_SIZE)
                .recordStats();
    }

    private Caffeine<Object, Object> graphsCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(1) // Only one "all graphs" result
                .recordStats();
    }

    private Caffeine<Object, Object> graphByIdCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(DEFAULT_MAX_SIZE)
                .recordStats();
    }

    private Caffeine<Object, Object> featureFlagsCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(FEATURE_FLAGS_MAX_SIZE)
                .expireAfterWrite(FEATURE_FLAGS_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats();
    }
}
