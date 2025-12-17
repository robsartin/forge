package com.robsartin.graphs.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a node in an immutable graph.
 * The node is identified by its UUID which is the same as the ImmutableGraph nodeId.
 */
@Entity
@Table(name = "immutable_graph_nodes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"graph_id", "id"}))
public class ImmutableGraphNodeEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String label;

    @ManyToOne
    @JoinColumn(name = "graph_id")
    private ImmutableGraphEntity graph;

    protected ImmutableGraphNodeEntity() {
        // JPA requires a no-arg constructor
    }

    public ImmutableGraphNodeEntity(UUID id, String label) {
        this.id = id;
        this.label = label;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
                ", label='" + label + '\'' +
                '}';
    }
}
