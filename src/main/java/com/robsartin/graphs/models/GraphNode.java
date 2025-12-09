package com.robsartin.graphs.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "graph_nodes")
public class GraphNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer graphNodeId;

    @ManyToOne
    @JoinColumn(name = "graph_id")
    private Graph graph;

    protected GraphNode() {
        // JPA requires a no-arg constructor
    }

    public GraphNode(String name) {
        this.name = name;
    }

    public GraphNode(String name, Integer graphNodeId) {
        this.name = name;
        this.graphNodeId = graphNodeId;
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

    public Integer getGraphNodeId() {
        return graphNodeId;
    }

    public void setGraphNodeId(Integer graphNodeId) {
        this.graphNodeId = graphNodeId;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode graphNode = (GraphNode) o;
        return Objects.equals(id, graphNode.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GraphNode{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", graphNodeId=" + graphNodeId +
                '}';
    }
}
