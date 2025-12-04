package com.robsartin.graphs.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration for jMolecules DDD and CQRS patterns.
 *
 * jMolecules provides compile-time and runtime verification of DDD principles
 * through annotations and ArchUnit rules. This configuration ensures proper
 * initialization of jMolecules integrations with Spring.
 *
 * Key features enabled:
 * - DDD annotations (@Aggregate, @Entity, @ValueObject, etc.)
 * - CQRS architecture support
 * - Spring stereotype mapping for jMolecules types
 */
@Configuration
public class JMoleculesConfiguration {
    // Configuration hooks can be added here as needed for custom jMolecules behavior
}
