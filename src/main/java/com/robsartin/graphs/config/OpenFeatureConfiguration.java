package com.robsartin.graphs.config;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration for OpenFeature feature flag management.
 *
 * This configuration sets up the OpenFeature SDK with an in-memory provider
 * for feature flags. In production, this could be replaced with a provider
 * backed by LaunchDarkly, Split, Flagsmith, or other feature flag services.
 */
@Configuration
public class OpenFeatureConfiguration {

    public static final String FLAG_DELETE_ENABLED = "graph-delete-enabled";

    @Bean
    public OpenFeatureAPI openFeatureAPI() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();

        Map<String, Flag<?>> flags = Map.of(
                FLAG_DELETE_ENABLED, Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("off")
                        .build()
        );

        api.setProviderAndWait(new InMemoryProvider(flags));

        return api;
    }

    @Bean
    public Client openFeatureClient(OpenFeatureAPI api) {
        return api.getClient();
    }
}
