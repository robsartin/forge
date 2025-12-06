package com.robsartin.graphs.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "graphs")
public class Graph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String graphData;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private String nodeType;

    @Column(nullable = false)
    private String edgeType;

    protected Graph() {
        // JPA requires a no-arg constructor
    }

    public Graph(String name, String description, String nodeType, String edgeType) {
        this.name = name;
        this.description = description;
        this.version = 1;
        this.graphData = "{}";
        this.createdAt = Instant.now();
        this.nodeType = nodeType;
        this.edgeType = edgeType;
    }

    public Graph(String name, String description, Integer version, Long parentId,
                 String graphData, String nodeType, String edgeType) {
        this.name = name;
        this.description = description;
        this.version = version;
        this.parentId = parentId;
        this.graphData = graphData;
        this.createdAt = Instant.now();
        this.nodeType = nodeType;
        this.edgeType = edgeType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getGraphData() {
        return graphData;
    }

    public void setGraphData(String graphData) {
        this.graphData = graphData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
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
                ", version=" + version +
                ", parentId=" + parentId +
                ", nodeType='" + nodeType + '\'' +
                ", edgeType='" + edgeType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
