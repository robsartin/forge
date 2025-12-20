# 6. Log controller invocations with arguments and returns

Date: 2025-12-20

## Status

Accepted

## Context

For debugging, auditing, and observability, we need consistent logging of all API controller interactions. Manual logging is error-prone and leads to inconsistent coverage.

## Decision

We will automatically log all controller invocations including:
- Method name and endpoint path
- Input arguments (request parameters, path variables, request bodies)
- Return values or response status
- Execution duration

Implementation approach:
- Use Spring AOP with an @Aspect to intercept all @RestController methods
- Log at INFO level for successful calls, WARN/ERROR for failures
- Integrate with Correlation-ID tracing (see ADR-0008)

## Consequences

**Benefits:**
- Consistent, complete audit trail of all API interactions
- Simplified debugging with full request/response context
- No manual logging code required in controllers
- Metrics can be derived from structured logs

**Constraints:**
- Must sanitize sensitive data (passwords, tokens, PII) before logging
- Large request/response bodies may need truncation
- Slight performance overhead from reflection and serialization
- Log storage costs increase with verbose logging
