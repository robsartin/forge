# Add pagination to graph and node list endpoints

**Labels:** enhancement, priority:high

## Type
Feature

## Priority
**High**

## Description
The `GET /graphs` and `GET /graphs/{id}/nodes` endpoints return all results without pagination. For large datasets, this causes performance issues and poor UX.

## Current Behavior
- `GET /graphs` returns all graphs in the system
- `GET /graphs/{id}/nodes` returns all nodes in a graph
- No limit on response size

## Proposed Solution
Add pagination support using Spring Data's `Pageable`:

### Request Parameters
- `page` (default: 0) - Page number (zero-indexed)
- `size` (default: 20, max: 100) - Items per page
- `sort` (optional) - Sort field and direction

### Response Format
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "hasNext": true,
  "hasPrevious": false
}
```

## Files Affected
- `src/main/java/com/robsartin/graphs/application/GraphController.java`
- `src/main/java/com/robsartin/graphs/ports/out/GraphRepository.java`
- Frontend files to handle pagination

## Acceptance Criteria
- [ ] `GET /graphs` supports pagination
- [ ] `GET /graphs/{id}/nodes` supports pagination
- [ ] Default page size is 20
- [ ] Maximum page size is 100
- [ ] Response includes pagination metadata
- [ ] Backward compatible (no pagination params = page 0, size 20)
- [ ] Tests cover pagination scenarios
