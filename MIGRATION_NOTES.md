# PostgreSQL Migration Notes

**Date:** 2026-02-07
**Migration:** H2 (test) -> PostgreSQL (all environments)

## Summary

Migrated from H2 in-memory database for tests to Docker PostgreSQL, achieving full database parity across all environments. Added Flyway for schema migration management.

## Changes Made

### 1. Flyway Database Migrations
- Added `flyway-core` and `flyway-database-postgresql` dependencies
- Created `V1__initial_schema.sql` with DDL for all 6 tables
- Schema is now managed by Flyway, not Hibernate `ddl-auto: update`
- All profiles use `ddl-auto: validate` — Hibernate validates but never modifies schema

### 2. Profile Configuration

| Profile | Database | Port | Flyway | DDL-Auto |
|---------|----------|------|--------|----------|
| Base/Default | PostgreSQL (env vars) | 5432 | enabled, baseline-on-migrate | validate |
| Local | Docker PostgreSQL | 5433 | enabled, clean allowed | validate |
| Test | Docker PostgreSQL | 5433 | enabled, clean-on-validation-error | validate |
| Prod | PostgreSQL (env vars) | env var | enabled | validate |

### 3. Production Hardening
- Added SSL settings to prod datasource (`sslmode: require`)
- Credentials via environment variables only (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- Flyway enabled in prod for managed migrations

### 4. Test Infrastructure
- Removed H2 dependency
- Tests now run against Docker PostgreSQL on port 5433
- Same database engine as production (PostgreSQL 16)

## Assumptions

1. **Docker required for local dev and tests**: A PostgreSQL container must be running on port 5433. Start it with:
   ```bash
   docker run -d --name pg-test -e POSTGRES_PASSWORD=test -e POSTGRES_DB=graphdb -p 5433:5432 postgres:16
   ```

2. **Testcontainers not used**: Docker Desktop 4.55+ (API v1.52) is incompatible with Testcontainers 1.20.4 (returns 400 on Docker info endpoint). Once Testcontainers releases a compatible version, consider switching tests to use `@ServiceConnection` for automatic container lifecycle management.

3. **Flyway baseline-on-migrate**: Enabled in base config to support existing databases that already have tables but no Flyway history. On first run against an existing database, Flyway will baseline at version 0 and then apply V1 migration.

4. **SSL in production**: Configured via HikariCP data source properties (`ssl=true`, `sslmode=require`). The actual SSL certificate configuration depends on the cloud provider (RDS, Cloud SQL, etc.).

5. **No data migration**: This migration assumes no existing data needs to be preserved. The Flyway V1 script creates tables from scratch.

## Running Tests

```bash
# Ensure Docker PG is running
docker run -d --name pg-test -e POSTGRES_PASSWORD=test -e POSTGRES_DB=graphdb -p 5433:5432 postgres:16

# Run tests
mvn test
```

## Rollback

To revert tests to H2 in-memory:
```bash
./rollback-to-h2.sh
```

See the script for manual steps required after running it.

## Future Considerations

- **Testcontainers**: Re-evaluate when a version compatible with Docker Desktop API v1.52 is released
- **CI/CD**: Configure PostgreSQL service in CI pipeline (GitHub Actions service container, etc.)
- **Additional migrations**: Add new files as `V2__description.sql`, `V3__description.sql`, etc.
- **Flyway callbacks**: Consider adding `afterMigrate` callbacks for seed data in dev/test
