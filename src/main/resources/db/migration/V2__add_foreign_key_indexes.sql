-- V2: Add indexes on foreign keys for better query performance
-- These indexes improve JOIN operations and CASCADE deletes

-- Index for graph_nodes.graph_id (speeds up node lookups by graph)
CREATE INDEX idx_graph_nodes_graph_id ON graph_nodes(graph_id);

-- Indexes for graph_edges foreign keys (speeds up edge lookups and traversals)
CREATE INDEX idx_graph_edges_graph_id ON graph_edges(graph_id);
CREATE INDEX idx_graph_edges_from_node ON graph_edges(from_node_id);
CREATE INDEX idx_graph_edges_to_node ON graph_edges(to_node_id);

-- Composite index for efficient edge queries within a graph
CREATE INDEX idx_graph_edges_graph_from_to ON graph_edges(graph_id, from_node_id, to_node_id);
