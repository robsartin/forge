package com.robsartin.graphs.models;

import com.robsartin.graphs.infrastructure.UuidV7Generator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an immutable graph that can be saved and loaded as a whole.
 * The graph is identified by its UUID id and contains nodes and edges.
 */
@Entity
@Table(name = "immutable_graphs")
public class ImmutableGraphEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String name;

    @OneToMany(mappedBy = "graph", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImmutableGraphNodeEntity> nodes = new ArrayList<>();

    @OneToMany(mappedBy = "graph", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImmutableGraphEdgeEntity> edges = new ArrayList<>();

    public ImmutableGraphEntity() {
        // JPA requires a no-arg constructor
    }

    public ImmutableGraphEntity(String name) {
        this.id = UuidV7Generator.generate();
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ImmutableGraphNodeEntity> getNodes() {
        return nodes;
    }

    public void setNodes(List<ImmutableGraphNodeEntity> nodes) {
        this.nodes = nodes;
    }

    public List<ImmutableGraphEdgeEntity> getEdges() {
        return edges;
    }

    public void setEdges(List<ImmutableGraphEdgeEntity> edges) {
        this.edges = edges;
    }

    public void addNode(ImmutableGraphNodeEntity node) {
        nodes.add(node);
        node.setGraph(this);
    }

    public void addEdge(ImmutableGraphEdgeEntity edge) {
        edges.add(edge);
        edge.setGraph(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableGraphEntity that = (ImmutableGraphEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ImmutableGraphEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nodeCount=" + nodes.size() +
                ", edgeCount=" + edges.size() +
                '}';
    }
}
