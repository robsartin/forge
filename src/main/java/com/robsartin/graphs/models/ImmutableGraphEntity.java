package com.robsartin.graphs.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing an immutable graph that can be saved and loaded as a whole.
 * The graph is identified by its graphId and contains nodes and edges.
 */
@Entity
@Table(name = "immutable_graphs")
public class ImmutableGraphEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer graphId;

    private String name;

    @OneToMany(mappedBy = "graph", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImmutableGraphNodeEntity> nodes = new ArrayList<>();

    @OneToMany(mappedBy = "graph", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImmutableGraphEdgeEntity> edges = new ArrayList<>();

    protected ImmutableGraphEntity() {
        // JPA requires a no-arg constructor
    }

    public ImmutableGraphEntity(String name) {
        this.name = name;
    }

    public Integer getGraphId() {
        return graphId;
    }

    public void setGraphId(Integer graphId) {
        this.graphId = graphId;
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
        return Objects.equals(graphId, that.graphId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(graphId);
    }

    @Override
    public String toString() {
        return "ImmutableGraphEntity{" +
                "graphId=" + graphId +
                ", name='" + name + '\'' +
                ", nodeCount=" + nodes.size() +
                ", edgeCount=" + edges.size() +
                '}';
    }
}
