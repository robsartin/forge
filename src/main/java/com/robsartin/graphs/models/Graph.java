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

    /**
     * Add a node to the graph. The node is added to the nodes list but NOT
     * to the ImmutableGraph yet, since the database ID is not available until
     * after persistence. Call syncImmutableGraph() after saving to add persisted
     * nodes to the ImmutableGraph.
     */
    public GraphNode addNode(String nodeName) {
        GraphNode node = new GraphNode(nodeName);
        node.setGraph(this);
        this.nodes.add(node);
        return node;
    }

    /**
     * Synchronize the ImmutableGraph with persisted nodes.
     * This adds any nodes that have database IDs but are not yet in the ImmutableGraph.
     */
    public void syncImmutableGraph() {
        for (GraphNode node : nodes) {
            if (node.getId() != null && !immutableGraph.containsNode(node.getId())) {
                this.immutableGraph = this.immutableGraph.addNodeWithId(node.getId(), node.getName());
            }
        }
    }

    /**
     * Add an edge between two nodes using their database IDs.
     * Automatically syncs the ImmutableGraph before adding the edge.
     */
    public void addEdge(int fromNodeId, int toNodeId) {
        syncImmutableGraph();
        this.immutableGraph = this.immutableGraph.addEdge(fromNodeId, toNodeId, "edge");
    }

    /**
     * Find a node by its database ID.
     */
    public GraphNode findNodeById(int nodeId) {
        return nodes.stream()
                .filter(n -> n.getId() != null && n.getId() == nodeId)
                .findFirst()
                .orElse(null);
    }

    @PostLoad
    private void reconstructImmutableGraph() {
        this.immutableGraph = new ImmutableGraph<>();
        for (GraphNode node : nodes) {
            if (node.getId() != null) {
                this.immutableGraph = this.immutableGraph.addNodeWithId(node.getId(), node.getName());
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
