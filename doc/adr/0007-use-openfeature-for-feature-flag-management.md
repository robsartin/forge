# 7. Use OpenFeature for feature flag management

Date: 2025-12-20

## Status

Accepted

## Context

We need feature flag capabilities for progressive rollouts, A/B testing, and operational toggles. We want to avoid vendor lock-in while maintaining flexibility to choose or change flag providers.

## Decision

We will use OpenFeature as the feature flag abstraction layer.

Key factors:
- OpenFeature is a CNCF project providing a vendor-neutral API for feature flags
- Supports multiple providers (LaunchDarkly, Flagsmith, ConfigCat, file-based, etc.)
- Java SDK integrates well with Spring Boot
- Allows starting with simple file-based flags and migrating to a service later

## Consequences

**Benefits:**
- Vendor independence: switch providers without changing application code
- Standardized API across all feature flag operations
- Built-in support for evaluation context (user attributes, environment)
- Growing ecosystem with community providers

**Constraints:**
- Additional abstraction layer over direct provider SDKs
- Some provider-specific features may not be available through OpenFeature
- Must select and configure an initial provider implementation
- Team needs to establish conventions for flag naming and lifecycle
