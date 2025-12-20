# 8. Trace traffic with Correlation-ID using UUID V7

Date: 2025-12-20

## Status

Accepted

## Context

For distributed tracing and log correlation, every request needs a unique identifier that follows it through all services and log entries. This enables end-to-end debugging and request tracking.

## Decision

We will trace all traffic using a Correlation-ID header with UUID V7 format.

Implementation:
- Header name: `Correlation-ID` (or `X-Correlation-ID` for legacy compatibility)
- If client provides a valid Correlation-ID, use it; otherwise generate one at the controller
- Use UUID V7 format: time-ordered UUIDs that are sortable and include timestamp
- Propagate Correlation-ID to all downstream service calls
- Include Correlation-ID in all log entries via MDC (Mapped Diagnostic Context)

UUID V7 benefits:
- Time-ordered: IDs generated later sort after earlier ones
- Embedded timestamp: can extract approximate request time from the ID
- Database-friendly: better index locality than random UUIDs

## Consequences

**Benefits:**
- Complete request traceability across services and logs
- Client can provide their own ID for end-to-end tracking
- UUID V7 enables chronological sorting without additional timestamp fields
- MDC integration makes correlation automatic in all log statements

**Constraints:**
- All services must propagate the header correctly
- Requires filter/interceptor setup in each service
- Client-provided IDs should be validated (format, length)
- Java's built-in UUID class doesn't support V7 natively (need library or custom generator)
