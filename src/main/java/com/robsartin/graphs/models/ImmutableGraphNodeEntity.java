package com.robsartin.graphs.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

/**
 * Entity representing a node in an immutable graph.
 * The node is identified by the combination of graphId and graphNodeId.
 */
@Entity
@Table(name = "immutable_graph_nodes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"graph_id", "graphNodeId"}))
public class ImmutableGraphNodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer graphNodeId;

    private String label;

    @ManyToOne
    @JoinColumn(name = "graph_id")
    private ImmutableGraphEntity graph;

    protected ImmutableGraphNodeEntity() {
        // JPA requires a no-arg constructor
    }

    public ImmutableGraphNodeEntity(Integer graphNodeId, String label) {
        this.graphNodeId = graphNodeId;
        this.label = label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGraphNodeId() {
        return graphNodeId;
    }

    public void setGraphNodeId(Integer graphNodeId) {
        this.graphNodeId = graphNodeId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ImmutableGraphEntity getGraph() {
        return graph;
    }

    public void setGraph(ImmutableGraphEntity graph) {
        this.graph = graph;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableGraphNodeEntity that = (ImmutableGraphNodeEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ImmutableGraphNodeEntity{" +
                "id=" + id +
                ", graphNodeId=" + graphNodeId +
                ", label='" + label + '\'' +
                '}';
    }
}
