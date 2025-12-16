package com.robsartin.graphs.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Entity representing an edge in an immutable graph.
 * The edge connects two nodes identified by fromId and toId.
 */
@Entity
@Table(name = "immutable_graph_edges")
public class ImmutableGraphEdgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer fromId;

    private Integer toId;

    @ManyToOne
    @JoinColumn(name = "graph_id")
    private ImmutableGraphEntity graph;

    protected ImmutableGraphEdgeEntity() {
        // JPA requires a no-arg constructor
    }

    public ImmutableGraphEdgeEntity(Integer fromId, Integer toId) {
        this.fromId = fromId;
        this.toId = toId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public void setFromId(Integer fromId) {
        this.fromId = fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public void setToId(Integer toId) {
        this.toId = toId;
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
        ImmutableGraphEdgeEntity that = (ImmutableGraphEdgeEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ImmutableGraphEdgeEntity{" +
                "id=" + id +
                ", fromId=" + fromId +
                ", toId=" + toId +
                '}';
    }
}
