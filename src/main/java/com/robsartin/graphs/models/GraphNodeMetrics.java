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
 * Per-node metrics for centrality and clustering analysis.
 */
@Entity
@Table(name = "graph_node_metrics",
        indexes = @Index(name = "idx_node_metrics_graph", columnList = "graph_id"))
public class GraphNodeMetrics {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graph_id", nullable = false)
    private Graph graph;

    @Column(name = "node_id", nullable = false, columnDefinition = "uuid")
    private UUID nodeId;

    @Column(name = "degree_centrality", nullable = false)
    private double degreeCentrality;

    @Column(name = "in_degree")
    private Integer inDegree;

    @Column(name = "out_degree")
    private Integer outDegree;

    @Column(name = "betweenness_centrality")
    private Double betweennessCentrality;

    @Column(name = "closeness_centrality")
    private Double closenessCentrality;

    @Column(name = "clustering_coefficient")
    private Double clusteringCoefficient;

    protected GraphNodeMetrics() {
    }

    public GraphNodeMetrics(Graph graph, UUID nodeId) {
        this.id = UuidV7Generator.generate();
        this.graph = graph;
        this.nodeId = nodeId;
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

    public UUID getNodeId() {
        return nodeId;
    }

    public double getDegreeCentrality() {
        return degreeCentrality;
    }

    public void setDegreeCentrality(double degreeCentrality) {
        this.degreeCentrality = degreeCentrality;
    }

    public Integer getInDegree() {
        return inDegree;
    }

    public void setInDegree(Integer inDegree) {
        this.inDegree = inDegree;
    }

    public Integer getOutDegree() {
        return outDegree;
    }

    public void setOutDegree(Integer outDegree) {
        this.outDegree = outDegree;
    }

    public Double getBetweennessCentrality() {
        return betweennessCentrality;
    }

    public void setBetweennessCentrality(Double betweennessCentrality) {
        this.betweennessCentrality = betweennessCentrality;
    }

    public Double getClosenessCentrality() {
        return closenessCentrality;
    }

    public void setClosenessCentrality(Double closenessCentrality) {
        this.closenessCentrality = closenessCentrality;
    }

    public Double getClusteringCoefficient() {
        return clusteringCoefficient;
    }

    public void setClusteringCoefficient(Double clusteringCoefficient) {
        this.clusteringCoefficient = clusteringCoefficient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNodeMetrics that = (GraphNodeMetrics) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
