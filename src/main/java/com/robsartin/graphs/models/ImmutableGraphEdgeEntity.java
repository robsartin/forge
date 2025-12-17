package com.robsartin.graphs.models;

import com.robsartin.graphs.infrastructure.UuidV7Generator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an edge in an immutable graph.
 * The edge connects two nodes identified by fromId and toId (which are node UUIDs).
 */
@Entity
@Table(name = "immutable_graph_edges")
public class ImmutableGraphEdgeEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(columnDefinition = "uuid")
    private UUID fromId;

    @Column(columnDefinition = "uuid")
    private UUID toId;

    @ManyToOne
    @JoinColumn(name = "graph_id")
    private ImmutableGraphEntity graph;

    protected ImmutableGraphEdgeEntity() {
        // JPA requires a no-arg constructor
    }

    public ImmutableGraphEdgeEntity(UUID fromId, UUID toId) {
        this.id = UuidV7Generator.generate();
        this.fromId = fromId;
        this.toId = toId;
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

    public UUID getFromId() {
        return fromId;
    }

    public void setFromId(UUID fromId) {
        this.fromId = fromId;
    }

    public UUID getToId() {
        return toId;
    }

    public void setToId(UUID toId) {
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
