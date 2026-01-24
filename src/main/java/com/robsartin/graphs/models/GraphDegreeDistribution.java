package com.robsartin.graphs.models;

import com.robsartin.graphs.infrastructure.UuidV7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Degree distribution entry for a graph.
 * Stores how many nodes have each degree value.
 */
@Entity
@Table(name = "graph_degree_distribution",
        indexes = @Index(name = "idx_degree_dist_graph", columnList = "graph_id"))
public class GraphDegreeDistribution {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graph_id", nullable = false)
    private Graph graph;

    @Column(name = "degree_value", nullable = false)
    private int degreeValue;

    @Column(name = "node_count", nullable = false)
    private int nodeCount;

    protected GraphDegreeDistribution() {
    }

    public GraphDegreeDistribution(Graph graph, int degreeValue, int nodeCount) {
        this.id = UuidV7Generator.generate();
        this.graph = graph;
        this.degreeValue = degreeValue;
        this.nodeCount = nodeCount;
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

    public Graph getGraph() {
        return graph;
    }

    public int getDegreeValue() {
        return degreeValue;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphDegreeDistribution that = (GraphDegreeDistribution) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
