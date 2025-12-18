package com.robsartin.graphs.infrastructure;

import com.robsartin.graphs.config.CacheConfiguration;
import com.robsartin.graphs.config.OpenFeatureConfiguration;
import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import dev.openfeature.sdk.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestOpenFeatureConfiguration.class)
class FeatureFlagServiceCacheTest {

    @Autowired
    private FeatureFlagService featureFlagService;

    @SpyBean
    private Client openFeatureClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        Objects.requireNonNull(cacheManager.getCache(CacheConfiguration.FEATURE_FLAGS_CACHE)).clear();
        reset(openFeatureClient);
    }

    @Test
    void isDeleteEnabledShouldCacheResult() {
        // When - call isDeleteEnabled multiple times
        featureFlagService.isDeleteEnabled();
        featureFlagService.isDeleteEnabled();
        featureFlagService.isDeleteEnabled();

        // Then - OpenFeature client should only be called once
        verify(openFeatureClient, times(1))
            .getBooleanValue(eq(OpenFeatureConfiguration.FLAG_DELETE_ENABLED), anyBoolean());
    }

    @Test
    void isDeleteEnabledShouldReturnCachedValue() {
        // When - call isDeleteEnabled multiple times
        boolean result1 = featureFlagService.isDeleteEnabled();
        boolean result2 = featureFlagService.isDeleteEnabled();
        boolean result3 = featureFlagService.isDeleteEnabled();

        // Then - all results should be the same
        assertEquals(result1, result2);
        assertEquals(result2, result3);
    }

    @Test
    void cachedValueShouldBePresentInCache() {
        // When - call isDeleteEnabled
        boolean result = featureFlagService.isDeleteEnabled();

        // Then - value should be in cache
        var cache = cacheManager.getCache(CacheConfiguration.FEATURE_FLAGS_CACHE);
        assertNotNull(cache);

        var cachedValue = cache.get(OpenFeatureConfiguration.FLAG_DELETE_ENABLED);
        assertNotNull(cachedValue, "Cached value should be present");
        assertEquals(result, cachedValue.get());
    }
}
