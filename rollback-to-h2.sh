#!/usr/bin/env bash
# Rollback script: reverts test configuration from Docker PostgreSQL to H2 in-memory
# Usage: ./rollback-to-h2.sh
#
# This script reverts the test database configuration to use H2 in-memory
# instead of Docker PostgreSQL. It does NOT revert the main application
# (local/prod profiles remain on PostgreSQL).
#
# After running this script, you can remove the Flyway dependency from pom.xml
# and delete src/main/resources/db/migration/ if you want to revert completely.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Reverting test configuration to H2 in-memory database..."

# Restore H2 test configuration
cat > "$SCRIPT_DIR/src/test/resources/application.yml" << 'YAML'
# Test Configuration - Uses H2 in-memory database
spring:
  application:
    name: graph-experiment-test
  main:
    lazy-initialization: true
  threads:
    virtual:
      enabled: true

  # Disable OAuth2 for tests
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: test-client-id
            client-secret: test-client-secret
            scope:
              - email
              - profile

  # H2 In-Memory Database for Tests
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
    hikari:
      maximum-pool-size: 5
      minimum-idle: 1

  # JPA/Hibernate Configuration for Tests
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: false

  # Disable Flyway for H2 tests
  flyway:
    enabled: false

# Logging - quieter for tests
logging:
  level:
    root: WARN
    org.hibernate.SQL: WARN
    com.robsartin.graphs: INFO

# Actuator - match endpoints expected by tests
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,threaddump,env
  endpoint:
    health:
      show-details: always
    env:
      show-values: always
  tracing:
    enabled: false
  otlp:
    metrics:
      export:
        enabled: false

# Resilience4J - faster timeouts for tests
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: false
        sliding-window-size: 5
        minimum-number-of-calls: 2
        wait-duration-in-open-state: 1s

  retry:
    configs:
      default:
        max-attempts: 2
        wait-duration: 100ms

  timelimiter:
    configs:
      default:
        timeout-duration: 1s
YAML

echo "Restoring H2 dependency in pom.xml..."
echo ""
echo "MANUAL STEPS REQUIRED:"
echo "1. Add H2 dependency back to pom.xml under test scope:"
echo '   <dependency>'
echo '       <groupId>com.h2database</groupId>'
echo '       <artifactId>h2</artifactId>'
echo '       <scope>test</scope>'
echo '   </dependency>'
echo ""
echo "2. Optionally remove Flyway dependencies if not needed:"
echo '   - org.flywaydb:flyway-core'
echo '   - org.flywaydb:flyway-database-postgresql'
echo ""
echo "3. Optionally delete: src/main/resources/db/migration/"
echo ""
echo "4. Stop the Docker PostgreSQL container:"
echo "   docker rm -f pg-test"
echo ""
echo "Test configuration reverted to H2 in-memory database."
