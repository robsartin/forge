# 2. Use Java 17 for builds

Date: 2025-12-20

## Status

Accepted

## Context

We need to choose a Java version for the Forge project that provides modern language features, good performance, and long-term support stability. The project uses Spring Boot 3.4.1, which requires Java 17 as a minimum version.

## Decision

We will use Java 17 (LTS) as our build and runtime target.

Key factors:
- Java 17 is a Long-Term Support (LTS) release with support until September 2029
- Spring Boot 3.x requires Java 17 as the minimum version
- Java 17 includes useful language features: records, sealed classes, pattern matching for instanceof, and text blocks
- Broad ecosystem support with stable tooling and library compatibility

## Consequences

**Benefits:**
- Access to modern Java features that improve code readability and reduce boilerplate
- LTS guarantees security patches and bug fixes for extended period
- Full compatibility with Spring Boot 3.x ecosystem
- Improved performance over older Java versions

**Constraints:**
- Cannot use Java 21+ features (virtual threads, pattern matching in switch) until a future upgrade
- All developers and CI/CD environments must have Java 17 installed
- Any third-party libraries must be compatible with Java 17
