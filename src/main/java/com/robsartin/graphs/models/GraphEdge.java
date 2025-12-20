package com.robsartin.graphs.models;

import com.robsartin.graphs.infrastructure.UuidV7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "graph_edges")
public class GraphEdge {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graph_id", nullable = false)
    private Graph graph;

    @Column(name = "from_node_id", nullable = false)
    private UUID fromNodeId;

    @Column(name = "to_node_id", nullable = false)
    private UUID toNodeId;

    protected GraphEdge() {
        // JPA requires a no-arg constructor
    }

    public GraphEdge(Graph graph, UUID fromNodeId, UUID toNodeId) {
        this.id = UuidV7Generator.generate();
        this.graph = graph;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    @PrePersist
    private void ensureId() {
        if (this.id == null) {
            this.id = UuidV7Generator.generate();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public UUID getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(UUID fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public UUID getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(UUID toNodeId) {
        this.toNodeId = toNodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphEdge graphEdge = (GraphEdge) o;
        return Objects.equals(id, graphEdge.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GraphEdge{" +
                "id=" + id +
                ", fromNodeId=" + fromNodeId +
                ", toNodeId=" + toNodeId +
                '}';
    }
}
