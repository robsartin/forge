package com.robsartin.graphs.config;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * Test configuration for OpenFeature that enables all feature flags.
 *
 * This configuration is used in integration tests to enable features
 * that are disabled by default in production.
 */
@TestConfiguration
public class TestOpenFeatureConfiguration {

    @Bean
    @Primary
    public OpenFeatureAPI testOpenFeatureAPI() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();

        Map<String, Flag<?>> flags = Map.of(
                OpenFeatureConfiguration.FLAG_DELETE_ENABLED, Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("on")
                        .build());

        api.setProviderAndWait(new InMemoryProvider(flags));

        return api;
    }

    @Bean
    @Primary
    public Client testOpenFeatureClient(OpenFeatureAPI api) {
        return api.getClient();
    }
}
