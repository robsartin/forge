# Add global exception handler with structured error responses

**Labels:** enhancement, priority:high

## Type
Feature

## Priority
**High**

## Description
Currently, exceptions like `IllegalArgumentException` return HTTP 500 with no body. API consumers need consistent, structured error responses.

## Current Behavior
- `IllegalArgumentException` in `GraphController.java:403` returns HTTP 500
- No error body in responses
- Inconsistent error formats across endpoints

## Proposed Solution
Create a `@ControllerAdvice` class with `@ExceptionHandler` methods:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return new ErrorResponse("BAD_REQUEST", ex.getMessage());
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(EntityNotFoundException ex) {
        return new ErrorResponse("NOT_FOUND", ex.getMessage());
    }
}
```

### Error Response Format
```json
{
  "error": "BAD_REQUEST",
  "message": "Both nodes must exist in the graph",
  "timestamp": "2026-07-06T10:30:00Z",
  "path": "/graphs/123/nodes/456/789"
}
```

## Files Affected
- New: `src/main/java/com/robsartin/graphs/application/GlobalExceptionHandler.java`
- New: `src/main/java/com/robsartin/graphs/application/ErrorResponse.java`

## Acceptance Criteria
- [ ] Global exception handler created
- [ ] `IllegalArgumentException` -> 400 Bad Request
- [ ] `EntityNotFoundException` -> 404 Not Found
- [ ] All errors include timestamp and path
- [ ] Correlation ID included in error responses
- [ ] Tests verify error response format
