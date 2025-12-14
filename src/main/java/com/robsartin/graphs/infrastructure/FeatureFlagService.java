package com.robsartin.graphs.infrastructure;

import com.robsartin.graphs.config.OpenFeatureConfiguration;
import dev.openfeature.sdk.Client;
import org.springframework.stereotype.Service;

/**
 * Service for accessing feature flags via OpenFeature.
 *
 * This service provides a clean interface for checking feature flags
 * throughout the application. It wraps the OpenFeature Client to provide
 * type-safe methods for specific feature flags.
 */
@Service
public class FeatureFlagService {

    private final Client client;

    public FeatureFlagService(Client client) {
        this.client = client;
    }

    /**
     * Checks if the graph delete functionality is enabled.
     *
     * @return true if graph deletion is enabled, false otherwise
     */
    public boolean isDeleteEnabled() {
        return client.getBooleanValue(OpenFeatureConfiguration.FLAG_DELETE_ENABLED, false);
    }
}
