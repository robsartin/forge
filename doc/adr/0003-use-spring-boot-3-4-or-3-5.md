# 3. Use Spring Boot 3.4 or 3.5

Date: 2025-12-20

## Status

Accepted

## Context

We need a stable, modern web framework for building the Forge backend. Spring Boot is the industry standard for Java-based web applications with excellent tooling, documentation, and community support.

## Decision

We will use Spring Boot 3.4.x or 3.5.x for the application framework.

Key factors:
- Spring Boot 3.x provides Java 17+ support (aligns with ADR-0002)
- Native compilation support via GraalVM for improved startup times
- Built-in observability with Micrometer and integration with modern tracing systems
- Mature ecosystem with well-tested starters for JPA, security, and web
- Active development with regular security patches

## Consequences

**Benefits:**
- Rapid development with auto-configuration and sensible defaults
- Strong ecosystem of starters and integrations
- Excellent testing support with MockMvc and test slices
- Built-in actuator endpoints for health checks and metrics

**Constraints:**
- Must track Spring Boot releases for security updates
- Some dependencies may lag behind Spring Boot versions
- Team must be familiar with Spring conventions and annotations
