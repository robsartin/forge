# Upgrade Spring Boot to latest 3.5.x/3.6.x

**Labels:** dependencies, priority:medium

## Type
Dependency Update

## Priority
**Medium**

## Description
Current version: **Spring Boot 3.4.1** (December 2024)

As of July 2026, newer versions include security patches, performance improvements, and new features.

## Tasks

### 1. Research
- [ ] Check latest stable Spring Boot version
- [ ] Review release notes for breaking changes
- [ ] Identify deprecated APIs in use

### 2. Update Dependencies
- [ ] Update `spring-boot-starter-parent` version in `pom.xml`
- [ ] Update related dependencies if needed:
  - Resilience4j
  - SpringDoc OpenAPI
  - OpenTelemetry

### 3. Code Changes
- [ ] Fix any deprecated API usage
- [ ] Update configuration properties if changed
- [ ] Address any breaking changes

### 4. Testing
- [ ] Run full test suite
- [ ] Manual testing of all endpoints
- [ ] Verify OAuth2 flow still works
- [ ] Check observability (tracing, metrics)

## Files Affected
- `pom.xml`
- Potentially various Java files depending on deprecations

## Acceptance Criteria
- [ ] Spring Boot upgraded to latest stable version
- [ ] All tests pass
- [ ] No deprecated API warnings
- [ ] Application starts and functions correctly
- [ ] Documentation updated with new version
