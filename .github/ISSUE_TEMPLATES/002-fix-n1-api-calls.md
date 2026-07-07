# Fix N+1 API calls in frontend graph loading

**Labels:** bug, performance, priority:high

## Type
Bug / Performance

## Priority
**High**

## Description
The `loadGraphData` function in `graph-editor-app.jsx` (lines 191-210) makes one API call per node to fetch edges, causing severe performance issues for graphs with many nodes.

## Current Behavior
```javascript
// For each node, makes a separate API call
for (const node of nodeData) {
    const nodeWithLinks = await api.getNodeWithLinks(graphId, node.id);
    // ...
}
```

For a graph with 100 nodes, this results in 101 API calls (1 for nodes + 100 for edges).

## Proposed Solution

### Option A: Add batch endpoint (recommended)
Add `GET /graphs/{id}/full` returning complete graph structure:
```json
{
  "id": "...",
  "name": "...",
  "nodes": [...],
  "edges": [{"source": "...", "target": "..."}]
}
```

### Option B: Add edges-only endpoint
Add `GET /graphs/{id}/edges` to fetch all edges in one call.

## Files Affected
- `src/main/resources/static/js/graph-editor-app.jsx`
- `src/main/java/com/robsartin/graphs/application/GraphController.java` (new endpoint)

## Acceptance Criteria
- [ ] New endpoint returns full graph data in single call
- [ ] Frontend updated to use new endpoint
- [ ] Graph loading time reduced by ~90% for large graphs
- [ ] Tests added for new endpoint
