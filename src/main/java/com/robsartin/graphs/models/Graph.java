package com.robsartin.graphs.models;

import com.robsartin.graphs.infrastructure.ImmutableGraph;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "graphs")
public class Graph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @OneToMany(mappedBy = "graph", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GraphNode> nodes = new ArrayList<>();

    @Transient
    private ImmutableGraph<String, String> immutableGraph = new ImmutableGraph<>();

    protected Graph() {
        // JPA requires a no-arg constructor
    }

    public Graph(String name) {
        this.name = name;
        this.immutableGraph = new ImmutableGraph<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public void addEdge(int fromNodeId, int toNodeId) {
        this.immutableGraph = this.immutableGraph.addEdge(fromNodeId, toNodeId, "edge");
    }

    public GraphNode findNodeByGraphNodeId(int graphNodeId) {
        return nodes.stream()
                .filter(n -> n.getGraphNodeId() != null && n.getGraphNodeId() == graphNodeId)
                .findFirst()
                .orElse(null);
    }

    @PostLoad
    private void reconstructImmutableGraph() {
        this.immutableGraph = new ImmutableGraph<>();
        nodes.stream()
                .filter(n -> n.getGraphNodeId() != null)
                .sorted(Comparator.comparing(GraphNode::getGraphNodeId))
                .forEach(node -> {
                    ImmutableGraph.GraphWithNode<String, String> result = immutableGraph.addNode(node.getName());
                    this.immutableGraph = result.getGraph();
                });
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
