package com.robsartin.graphs.models;

import com.robsartin.graphs.infrastructure.ImmutableGraph;
import com.robsartin.graphs.infrastructure.UuidV7Generator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "graphs")
public class Graph {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String name;

    @OneToMany(mappedBy = "graph", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GraphNode> nodes = new ArrayList<>();

    @OneToMany(mappedBy = "graph", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GraphEdge> edges = new ArrayList<>();

    @Transient
    private ImmutableGraph<String, String> immutableGraph = new ImmutableGraph<>();

    protected Graph() {
        // JPA requires a no-arg constructor
    }

    public Graph(String name) {
        this.id = UuidV7Generator.generate();
        this.name = name;
        this.immutableGraph = new ImmutableGraph<>();
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

    public List<GraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<GraphNode> nodes) {
        this.nodes = nodes;
    }

    public ImmutableGraph<String, String> getImmutableGraph() {
        return immutableGraph;
    }

    public void setImmutableGraph(ImmutableGraph<String, String> immutableGraph) {
        this.immutableGraph = immutableGraph;
    }

    public GraphNode addNode(String nodeName) {
        ImmutableGraph.GraphWithNode<String, String> result = immutableGraph.addNode(nodeName);
        this.immutableGraph = result.getGraph();
        GraphNode node = new GraphNode(nodeName, result.getNodeId());
        node.setGraph(this);
        this.nodes.add(node);
        return node;
    }

    public void addEdge(UUID fromNodeId, UUID toNodeId) {
        this.immutableGraph = this.immutableGraph.addEdge(fromNodeId, toNodeId, "edge");
        GraphEdge edge = new GraphEdge(this, fromNodeId, toNodeId);
        this.edges.add(edge);
    }

    public boolean removeNode(UUID nodeId) {
        GraphNode nodeToRemove = findNodeById(nodeId);
        if (nodeToRemove == null) {
            return false;
        }
        // Remove all edges incident on this node
        edges.removeIf(edge ->
            edge.getFromNodeId().equals(nodeId) || edge.getToNodeId().equals(nodeId));
        // Remove node from list
        nodes.remove(nodeToRemove);
        // Update immutable graph using match (which removes node and its edges)
        ImmutableGraph.Decomposition<String, String> decomposition = immutableGraph.match(nodeId);
        this.immutableGraph = decomposition.getGraph();
        return true;
    }

    public boolean removeEdge(UUID fromNodeId, UUID toNodeId) {
        boolean removed = edges.removeIf(edge ->
            edge.getFromNodeId().equals(fromNodeId) && edge.getToNodeId().equals(toNodeId));
        if (removed) {
            this.immutableGraph = this.immutableGraph.removeEdge(fromNodeId, toNodeId);
        }
        return removed;
    }

    public List<GraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdge> edges) {
        this.edges = edges;
    }

    public GraphNode findNodeById(UUID nodeId) {
        return nodes.stream()
                .filter(n -> n.getId() != null && n.getId().equals(nodeId))
                .findFirst()
                .orElse(null);
    }

    @PostLoad
    private void reconstructImmutableGraph() {
        this.immutableGraph = new ImmutableGraph<>();
        // Reconstruct nodes
        for (GraphNode node : nodes) {
            if (node.getId() != null) {
                ImmutableGraph.GraphWithNode<String, String> result =
                    immutableGraph.addNodeWithId(node.getId(), node.getName());
                this.immutableGraph = result.getGraph();
            }
        }
        // Reconstruct edges
        for (GraphEdge edge : edges) {
            this.immutableGraph = this.immutableGraph.addEdge(
                edge.getFromNodeId(), edge.getToNodeId(), "edge");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Graph graph = (Graph) o;
        return Objects.equals(id, graph.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Graph{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nodeCount=" + nodes.size() +
                '}';
    }
}
