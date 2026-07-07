# Add missing database indexes on foreign keys

**Labels:** bug, performance, priority:medium

## Type
Bug / Performance

## Priority
**Medium**

## Description
Foreign key columns in the database schema lack indexes, causing slow JOIN queries on large graphs.

## Current Schema
In `V1__initial_schema.sql`:
- `graph_nodes.graph_id` - no index
- `graph_edges.from_node_id` - no index
- `graph_edges.to_node_id` - no index

## Impact
- Slow `DELETE` cascade operations
- Slow graph loading with many nodes/edges
- Slow traversal queries (DFS/BFS)

## Proposed Solution
Create migration `V2__add_foreign_key_indexes.sql`:

```sql
-- Index for graph_nodes foreign key
CREATE INDEX idx_graph_nodes_graph_id ON graph_nodes(graph_id);

-- Indexes for graph_edges foreign keys
CREATE INDEX idx_graph_edges_from_node ON graph_edges(from_node_id);
CREATE INDEX idx_graph_edges_to_node ON graph_edges(to_node_id);

-- Optional: Composite index for edge lookups
CREATE INDEX idx_graph_edges_graph_from_to ON graph_edges(graph_id, from_node_id, to_node_id);
```

## Files Affected
- New: `src/main/resources/db/migration/V2__add_foreign_key_indexes.sql`

## Acceptance Criteria
- [ ] Migration file created
- [ ] Indexes created on all foreign key columns
- [ ] Query performance improved (measure with EXPLAIN)
- [ ] Migration tested on existing data
