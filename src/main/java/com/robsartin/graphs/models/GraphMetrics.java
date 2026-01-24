package com.robsartin.graphs.models;

import com.robsartin.graphs.infrastructure.UuidV7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate metrics for a graph, computed asynchronously after graph save.
 */
@Entity
@Table(name = "graph_metrics")
public class GraphMetrics {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graph_id", nullable = false, unique = true)
    private Graph graph;

    @Column(name = "node_count", nullable = false)
    private int nodeCount;

    @Column(name = "edge_count", nullable = false)
    private int edgeCount;

    @Column(name = "density", nullable = false)
    private double density;

    @Column(name = "average_degree", nullable = false)
    private double averageDegree;

    @Column(name = "is_connected", nullable = false)
    private boolean connected;

    @Column(name = "component_count", nullable = false)
    private int componentCount;

    @Column(name = "diameter")
    private Integer diameter;

    @Column(name = "average_path_length")
    private Double averagePathLength;

    @Column(name = "average_clustering_coefficient")
    private Double averageClusteringCoefficient;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    protected GraphMetrics() {
    }

    public GraphMetrics(Graph graph) {
        this.id = UuidV7Generator.generate();
        this.graph = graph;
        this.computedAt = Instant.now();
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

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(int edgeCount) {
        this.edgeCount = edgeCount;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double getAverageDegree() {
        return averageDegree;
    }

    public void setAverageDegree(double averageDegree) {
        this.averageDegree = averageDegree;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public int getComponentCount() {
        return componentCount;
    }

    public void setComponentCount(int componentCount) {
        this.componentCount = componentCount;
    }

    public Integer getDiameter() {
        return diameter;
    }

    public void setDiameter(Integer diameter) {
        this.diameter = diameter;
    }

    public Double getAveragePathLength() {
        return averagePathLength;
    }

    public void setAveragePathLength(Double averagePathLength) {
        this.averagePathLength = averagePathLength;
    }

    public Double getAverageClusteringCoefficient() {
        return averageClusteringCoefficient;
    }

    public void setAverageClusteringCoefficient(Double averageClusteringCoefficient) {
        this.averageClusteringCoefficient = averageClusteringCoefficient;
    }

    public Instant getComputedAt() {
        return computedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphMetrics that = (GraphMetrics) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
