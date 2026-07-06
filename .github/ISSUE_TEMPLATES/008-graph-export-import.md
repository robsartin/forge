# Add graph export/import functionality (JSON format)

**Labels:** enhancement, priority:medium

## Type
Feature

## Priority
**Medium**

## Description
Users cannot backup, share, or migrate graphs. Add endpoints to export graphs as JSON and import them.

## Proposed Solution

### Export Endpoint
`GET /graphs/{id}/export`

Response:
```json
{
  "version": "1.0",
  "exportedAt": "2026-07-06T10:30:00Z",
  "graph": {
    "name": "My Graph",
    "nodes": [
      {"id": "uuid-1", "name": "Node A"},
      {"id": "uuid-2", "name": "Node B"}
    ],
    "edges": [
      {"from": "uuid-1", "to": "uuid-2"}
    ]
  }
}
```

### Import Endpoint
`POST /graphs/import`

Request body: Same format as export response

Response: Created graph with new UUIDs

### Features
- Full graph structure in single file
- Version field for future compatibility
- New UUIDs generated on import (prevents conflicts)
- Optional: Import into existing graph (merge)

## Files Affected
- `src/main/java/com/robsartin/graphs/application/GraphController.java`
- New: `src/main/java/com/robsartin/graphs/application/dto/GraphExportDto.java`
- New: `src/main/java/com/robsartin/graphs/application/dto/GraphImportDto.java`

## Acceptance Criteria
- [ ] Export endpoint returns full graph as JSON
- [ ] Import endpoint creates graph from JSON
- [ ] Import generates new UUIDs
- [ ] Export includes version and timestamp
- [ ] Tests cover export/import round-trip
- [ ] Frontend has export/import buttons
