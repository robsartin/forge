package com.robsartin.graphs.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Graph Model")
class GraphModelTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("should create graph with name and generated ID")
        void shouldCreateGraphWithNameAndId() {
            Graph graph = new Graph("Test Graph");

            assertThat(graph.getId()).isNotNull();
            assertThat(graph.getName()).isEqualTo("Test Graph");
            assertThat(graph.getNodes()).isEmpty();
            assertThat(graph.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("should initialize immutable graph on creation")
        void shouldInitializeImmutableGraph() {
            Graph graph = new Graph("Test");

            assertThat(graph.getImmutableGraph()).isNotNull();
            assertThat(graph.getImmutableGraph().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Node Operations")
    class NodeOperations {

        @Test
        @DisplayName("should add node to graph")
        void shouldAddNode() {
            Graph graph = new Graph("Test");

            GraphNode node = graph.addNode("Node A");

            assertThat(node).isNotNull();
            assertThat(node.getName()).isEqualTo("Node A");
            assertThat(graph.getNodes()).hasSize(1);
            assertThat(graph.getImmutableGraph().nodeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should add multiple nodes")
        void shouldAddMultipleNodes() {
            Graph graph = new Graph("Test");

            graph.addNode("A");
            graph.addNode("B");
            graph.addNode("C");

            assertThat(graph.getNodes()).hasSize(3);
            assertThat(graph.getImmutableGraph().nodeCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("should find node by ID")
        void shouldFindNodeById() {
            Graph graph = new Graph("Test");
            GraphNode node = graph.addNode("Target");

            GraphNode found = graph.findNodeById(node.getId());

            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo("Target");
        }

        @Test
        @DisplayName("should return null for non-existent node ID")
        void shouldReturnNullForNonExistentNode() {
            Graph graph = new Graph("Test");
            graph.addNode("A");

            GraphNode found = graph.findNodeById(UUID.randomUUID());

            assertThat(found).isNull();
        }

        @Test
        @DisplayName("should remove node and its edges")
        void shouldRemoveNodeAndEdges() {
            Graph graph = new Graph("Test");
            GraphNode nodeA = graph.addNode("A");
            GraphNode nodeB = graph.addNode("B");
            graph.addEdge(nodeA.getId(), nodeB.getId());

            boolean removed = graph.removeNode(nodeA.getId());

            assertThat(removed).isTrue();
            assertThat(graph.getNodes()).hasSize(1);
            assertThat(graph.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("should return false when removing non-existent node")
        void shouldReturnFalseForNonExistentNodeRemoval() {
            Graph graph = new Graph("Test");

            boolean removed = graph.removeNode(UUID.randomUUID());

            assertThat(removed).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Operations")
    class EdgeOperations {

        @Test
        @DisplayName("should add edge between nodes")
        void shouldAddEdge() {
            Graph graph = new Graph("Test");
            GraphNode nodeA = graph.addNode("A");
            GraphNode nodeB = graph.addNode("B");

            graph.addEdge(nodeA.getId(), nodeB.getId());

            assertThat(graph.getEdges()).hasSize(1);
        }

        @Test
        @DisplayName("should remove edge between nodes")
        void shouldRemoveEdge() {
            Graph graph = new Graph("Test");
            GraphNode nodeA = graph.addNode("A");
            GraphNode nodeB = graph.addNode("B");
            graph.addEdge(nodeA.getId(), nodeB.getId());

            boolean removed = graph.removeEdge(nodeA.getId(), nodeB.getId());

            assertThat(removed).isTrue();
            assertThat(graph.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("should return false when removing non-existent edge")
        void shouldReturnFalseForNonExistentEdgeRemoval() {
            Graph graph = new Graph("Test");
            GraphNode nodeA = graph.addNode("A");
            GraphNode nodeB = graph.addNode("B");

            boolean removed = graph.removeEdge(nodeA.getId(), nodeB.getId());

            assertThat(removed).isFalse();
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityAndHashCode {

        @Test
        @DisplayName("should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Graph graph1 = new Graph("Graph 1");
            Graph graph2 = new Graph("Graph 2");
            graph2.setId(graph1.getId());

            assertThat(graph1).isEqualTo(graph2);
            assertThat(graph1.hashCode()).isEqualTo(graph2.hashCode());
        }

        @Test
        @DisplayName("should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            Graph graph1 = new Graph("Graph 1");
            Graph graph2 = new Graph("Graph 2");

            assertThat(graph1).isNotEqualTo(graph2);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            Graph graph = new Graph("Test");

            assertThat(graph).isNotEqualTo(null);
        }

        @Test
        @DisplayName("should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Graph graph = new Graph("Test");

            assertThat(graph).isNotEqualTo("not a graph");
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("should include name and node count")
        void shouldIncludeNameAndNodeCount() {
            Graph graph = new Graph("My Graph");
            graph.addNode("A");

            String result = graph.toString();

            assertThat(result).contains("My Graph");
            assertThat(result).contains("nodeCount=1");
        }
    }
}
