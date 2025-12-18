package com.robsartin.graphs.infrastructure;

import com.robsartin.graphs.config.CacheConfiguration;
import com.robsartin.graphs.config.OpenFeatureConfiguration;
import dev.openfeature.sdk.Client;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for accessing feature flags via OpenFeature.
 *
 * This service provides a clean interface for checking feature flags
 * throughout the application. It wraps the OpenFeature Client to provide
 * type-safe methods for specific feature flags.
 * Feature flag results are cached with LRU eviction and time-based expiration.
 */
@Service
public class FeatureFlagService {

    private final Client client;

    public FeatureFlagService(Client client) {
        this.client = client;
    }

    /**
     * Checks if the graph delete functionality is enabled.
     * Results are cached to reduce external calls.
     *
     * @return true if graph deletion is enabled, false otherwise
     */
    @Cacheable(value = CacheConfiguration.FEATURE_FLAGS_CACHE, key = "'" + OpenFeatureConfiguration.FLAG_DELETE_ENABLED + "'")
    public boolean isDeleteEnabled() {
        return client.getBooleanValue(OpenFeatureConfiguration.FLAG_DELETE_ENABLED, false);
    }
}
