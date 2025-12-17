package com.robsartin.graphs.models;

import com.robsartin.graphs.infrastructure.ImmutableGraph;
import com.robsartin.graphs.infrastructure.UuidV7Generator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @OneToMany(mappedBy = "graph", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GraphNode> nodes = new ArrayList<>();

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
        for (GraphNode node : nodes) {
            if (node.getId() != null) {
                ImmutableGraph.GraphWithNode<String, String> result =
                    immutableGraph.addNodeWithId(node.getId(), node.getName());
                this.immutableGraph = result.getGraph();
            }
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
