-- V1: Initial schema for graph editor application
-- Generated from JPA entity definitions

CREATE TABLE graphs (
    id UUID PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE graph_nodes (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    graph_id UUID,
    CONSTRAINT fk_graph_nodes_graph FOREIGN KEY (graph_id) REFERENCES graphs(id)
);

CREATE TABLE graph_edges (
    id UUID PRIMARY KEY,
    graph_id UUID NOT NULL,
    from_node_id UUID NOT NULL,
    to_node_id UUID NOT NULL,
    CONSTRAINT fk_graph_edges_graph FOREIGN KEY (graph_id) REFERENCES graphs(id)
);

CREATE TABLE graph_metrics (
    id UUID PRIMARY KEY,
    graph_id UUID NOT NULL UNIQUE,
    node_count INTEGER NOT NULL,
    edge_count INTEGER NOT NULL,
    density DOUBLE PRECISION NOT NULL,
    average_degree DOUBLE PRECISION NOT NULL,
    is_connected BOOLEAN NOT NULL,
    component_count INTEGER NOT NULL,
    diameter INTEGER,
    average_path_length DOUBLE PRECISION,
    average_clustering_coefficient DOUBLE PRECISION,
    computed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_graph_metrics_graph FOREIGN KEY (graph_id) REFERENCES graphs(id)
);

CREATE TABLE graph_node_metrics (
    id UUID PRIMARY KEY,
    graph_id UUID NOT NULL,
    node_id UUID NOT NULL,
    degree_centrality DOUBLE PRECISION NOT NULL,
    in_degree INTEGER,
    out_degree INTEGER,
    betweenness_centrality DOUBLE PRECISION,
    closeness_centrality DOUBLE PRECISION,
    clustering_coefficient DOUBLE PRECISION,
    CONSTRAINT fk_graph_node_metrics_graph FOREIGN KEY (graph_id) REFERENCES graphs(id)
);

CREATE INDEX idx_node_metrics_graph ON graph_node_metrics(graph_id);

CREATE TABLE graph_degree_distribution (
    id UUID PRIMARY KEY,
    graph_id UUID NOT NULL,
    degree_value INTEGER NOT NULL,
    node_count INTEGER NOT NULL,
    CONSTRAINT fk_graph_degree_dist_graph FOREIGN KEY (graph_id) REFERENCES graphs(id)
);

CREATE INDEX idx_degree_dist_graph ON graph_degree_distribution(graph_id);
