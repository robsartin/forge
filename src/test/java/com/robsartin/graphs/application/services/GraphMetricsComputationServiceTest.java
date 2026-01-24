package com.robsartin.graphs.application.services;

import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.models.GraphDegreeDistribution;
import com.robsartin.graphs.models.GraphMetrics;
import com.robsartin.graphs.models.GraphNode;
import com.robsartin.graphs.models.GraphNodeMetrics;
import com.robsartin.graphs.ports.out.GraphDegreeDistributionRepository;
import com.robsartin.graphs.ports.out.GraphMetricsRepository;
import com.robsartin.graphs.ports.out.GraphNodeMetricsRepository;
import com.robsartin.graphs.ports.out.GraphRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestOpenFeatureConfiguration.class)
@Transactional
class GraphMetricsComputationServiceTest {

    @Autowired
    private GraphMetricsComputationService metricsService;

    @Autowired
    private GraphRepository graphRepository;

    @Autowired
    private GraphMetricsRepository metricsRepository;

    @Autowired
    private GraphNodeMetricsRepository nodeMetricsRepository;

    @Autowired
    private GraphDegreeDistributionRepository degreeDistRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        graphRepository.deleteAll();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("should compute metrics for empty graph")
    void shouldComputeMetricsForEmptyGraph() {
        Graph graph = new Graph("Empty Graph");
        graph = graphRepository.save(graph);
        UUID graphId = graph.getId();
        flushAndClear();

        metricsService.computeAndSaveMetrics(graphId);

        Optional<GraphMetrics> metricsOpt = metricsRepository.findByGraphId(graphId);
        assertThat(metricsOpt).isPresent();

        GraphMetrics metrics = metricsOpt.get();
        assertThat(metrics.getNodeCount()).isZero();
        assertThat(metrics.getEdgeCount()).isZero();
        assertThat(metrics.getDensity()).isZero();
        assertThat(metrics.getAverageDegree()).isZero();
        assertThat(metrics.isConnected()).isTrue();
        assertThat(metrics.getComponentCount()).isZero();
    }

    @Test
    @DisplayName("should compute metrics for single node graph")
    void shouldComputeMetricsForSingleNodeGraph() {
        Graph graph = new Graph("Single Node");
        graph.addNode("A");
        graph = graphRepository.save(graph);
        UUID graphId = graph.getId();
        flushAndClear();

        metricsService.computeAndSaveMetrics(graphId);

        GraphMetrics metrics = metricsRepository.findByGraphId(graphId).orElseThrow();
        assertThat(metrics.getNodeCount()).isEqualTo(1);
        assertThat(metrics.getEdgeCount()).isZero();
        assertThat(metrics.isConnected()).isTrue();
        assertThat(metrics.getComponentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("should compute metrics for simple connected graph")
    void shouldComputeMetricsForSimpleConnectedGraph() {
        Graph graph = new Graph("Triangle");
        GraphNode a = graph.addNode("A");
        GraphNode b = graph.addNode("B");
        GraphNode c = graph.addNode("C");
        graph.addEdge(a.getId(), b.getId());
        graph.addEdge(b.getId(), c.getId());
        graph.addEdge(c.getId(), a.getId());
        graph = graphRepository.save(graph);
        UUID graphId = graph.getId();
        flushAndClear();

        metricsService.computeAndSaveMetrics(graphId);

        GraphMetrics metrics = metricsRepository.findByGraphId(graphId).orElseThrow();
        assertThat(metrics.getNodeCount()).isEqualTo(3);
        assertThat(metrics.getEdgeCount()).isEqualTo(3);
        assertThat(metrics.isConnected()).isTrue();
        assertThat(metrics.getComponentCount()).isEqualTo(1);
        assertThat(metrics.getDensity()).isEqualTo(0.5); // 3 / (3 * 2) = 0.5
        assertThat(metrics.getAverageDegree()).isEqualTo(2.0); // (2 * 3) / 3 = 2
    }

    @Test
    @DisplayName("should compute metrics for disconnected graph")
    void shouldComputeMetricsForDisconnectedGraph() {
        Graph graph = new Graph("Disconnected");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph = graphRepository.save(graph);
        UUID graphId = graph.getId();
        flushAndClear();

        metricsService.computeAndSaveMetrics(graphId);

        GraphMetrics metrics = metricsRepository.findByGraphId(graphId).orElseThrow();
        assertThat(metrics.getNodeCount()).isEqualTo(3);
        assertThat(metrics.getEdgeCount()).isZero();
        assertThat(metrics.isConnected()).isFalse();
        assertThat(metrics.getComponentCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("should compute per-node metrics")
    void shouldComputePerNodeMetrics() {
        Graph graph = new Graph("Star");
        GraphNode center = graph.addNode("Center");
        GraphNode leaf1 = graph.addNode("Leaf1");
        GraphNode leaf2 = graph.addNode("Leaf2");
        graph.addEdge(center.getId(), leaf1.getId());
        graph.addEdge(center.getId(), leaf2.getId());
        UUID centerId = center.getId();
        graph = graphRepository.save(graph);
        UUID graphId = graph.getId();
        flushAndClear();

        metricsService.computeAndSaveMetrics(graphId);

        List<GraphNodeMetrics> nodeMetrics = nodeMetricsRepository.findByGraphId(graphId);
        assertThat(nodeMetrics).hasSize(3);

        GraphNodeMetrics centerMetrics = nodeMetrics.stream()
                .filter(m -> m.getNodeId().equals(centerId))
                .findFirst()
                .orElseThrow();
        assertThat(centerMetrics.getOutDegree()).isEqualTo(2);
        assertThat(centerMetrics.getInDegree()).isZero();
    }

    @Test
    @DisplayName("should compute degree distribution")
    void shouldComputeDegreeDistribution() {
        Graph graph = new Graph("Test Graph");
        GraphNode a = graph.addNode("A");
        GraphNode b = graph.addNode("B");
        GraphNode c = graph.addNode("C");
        graph.addEdge(a.getId(), b.getId());
        graph.addEdge(a.getId(), c.getId());
        graph = graphRepository.save(graph);
        UUID graphId = graph.getId();
        flushAndClear();

        metricsService.computeAndSaveMetrics(graphId);

        List<GraphDegreeDistribution> distribution = degreeDistRepository.findByGraphId(graphId);
        assertThat(distribution).isNotEmpty();

        // Node A has out-degree 2, nodes B and C have in-degree 1
        // Total degrees: A=2, B=1, C=1
        int nodesWithDegree1 = distribution.stream()
                .filter(d -> d.getDegreeValue() == 1)
                .mapToInt(GraphDegreeDistribution::getNodeCount)
                .sum();
        int nodesWithDegree2 = distribution.stream()
                .filter(d -> d.getDegreeValue() == 2)
                .mapToInt(GraphDegreeDistribution::getNodeCount)
                .sum();

        assertThat(nodesWithDegree1).isEqualTo(2);
        assertThat(nodesWithDegree2).isEqualTo(1);
    }

    @Test
    @DisplayName("should skip computation for non-existent graph")
    void shouldSkipComputationForNonExistentGraph() {
        UUID nonExistentId = UUID.randomUUID();

        metricsService.computeAndSaveMetrics(nonExistentId);

        assertThat(metricsRepository.findByGraphId(nonExistentId)).isEmpty();
    }

    @Test
    @DisplayName("should recompute metrics on subsequent calls")
    void shouldRecomputeMetricsOnSubsequentCalls() {
        Graph graph = new Graph("Growing Graph");
        graph.addNode("A");
        graph = graphRepository.save(graph);
        UUID graphId = graph.getId();
        flushAndClear();

        metricsService.computeAndSaveMetrics(graphId);

        GraphMetrics firstMetrics = metricsRepository.findByGraphId(graphId).orElseThrow();
        assertThat(firstMetrics.getNodeCount()).isEqualTo(1);
        flushAndClear();

        // Reload graph and add more nodes
        Graph reloadedGraph = graphRepository.findById(graphId).orElseThrow();
        reloadedGraph.addNode("B");
        graphRepository.save(reloadedGraph);
        flushAndClear();

        metricsService.computeAndSaveMetrics(graphId);

        GraphMetrics secondMetrics = metricsRepository.findByGraphId(graphId).orElseThrow();
        assertThat(secondMetrics.getNodeCount()).isEqualTo(2);
    }
}
