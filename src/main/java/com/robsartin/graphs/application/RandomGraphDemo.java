package com.robsartin.graphs.application;

import com.robsartin.graphs.infrastructure.ImmutableGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Demonstrates creating a random graph with preferential attachment
 * and performing depth-first and breadth-first traversals.
 */
public class RandomGraphDemo {

    private static final Logger log = LoggerFactory.getLogger(RandomGraphDemo.class);
    private static final int NUM_NODES = 10;
    private static final int NUM_EDGES = 40;
    private static final Random random = new Random();

    public static void main(String[] args) {
        log.info("Creating random graph with preferential attachment...");
        log.info("Nodes: {}, Edges: {}", NUM_NODES, NUM_EDGES);

        ImmutableGraph<String, Integer> graph = createRandomGraphWithPreferentialAttachment();

        log.info("Graph created successfully!");

        // Perform traversals using the first node
        UUID startNode = graph.getNodeIds().iterator().next();

        // Perform depth-first traversal starting from first node
        log.info("=== Depth-First Traversal ===");
        graph.depthFirstTraversal(startNode, context ->
            log.info("{}", context.getLabel())
        );

        // Perform breadth-first traversal starting from first node
        log.info("=== Breadth-First Traversal ===");
        graph.breadthFirstTraversal(startNode, context ->
            log.info("{}", context.getLabel())
        );
    }

    /**
     * Creates a random graph using preferential attachment (Barabási-Albert model).
     * Nodes are numbered 1 to 10, and edges are added with preferential attachment
     * where nodes with higher degree are more likely to receive new edges.
     */
    private static ImmutableGraph<String, Integer> createRandomGraphWithPreferentialAttachment() {
        ImmutableGraph<String, Integer> graph = new ImmutableGraph<>();
        List<UUID> nodeIds = new ArrayList<>();

        // Create all nodes first (labeled "1" through "10")
        for (int i = 0; i < NUM_NODES; i++) {
            var result = graph.addNode(String.valueOf(i + 1));
            graph = result.getGraph();
            nodeIds.add(result.getNodeId());
        }

        // Track degree of each node for preferential attachment
        int[] degrees = new int[NUM_NODES];

        // Add edges with preferential attachment
        for (int edgeCount = 0; edgeCount < NUM_EDGES; edgeCount++) {
            int fromIndex = selectNodeByPreferentialAttachment(degrees);
            int toIndex = selectNodeByPreferentialAttachment(degrees);

            // Avoid self-loops and ensure we're not duplicating edges
            int attempts = 0;
            while (fromIndex == toIndex && attempts < 100) {
                toIndex = selectNodeByPreferentialAttachment(degrees);
                attempts++;
            }

            // Add the edge
            try {
                graph = graph.addEdge(nodeIds.get(fromIndex), nodeIds.get(toIndex), edgeCount + 1);
                degrees[fromIndex]++;
                degrees[toIndex]++;
            } catch (Exception e) {
                // Edge might already exist, continue
            }
        }

        return graph;
    }

    /**
     * Selects a node index based on preferential attachment.
     * Nodes with higher degree are more likely to be selected.
     * If all nodes have degree 0, select uniformly at random.
     */
    private static int selectNodeByPreferentialAttachment(int[] degrees) {
        // Calculate total degree
        int totalDegree = 0;
        for (int degree : degrees) {
            totalDegree += degree;
        }

        // If no edges yet, select uniformly at random
        if (totalDegree == 0) {
            return random.nextInt(NUM_NODES);
        }

        // Select based on degree distribution
        int target = random.nextInt(totalDegree + NUM_NODES); // Add NUM_NODES to give each node base probability
        int cumulative = 0;

        for (int i = 0; i < NUM_NODES; i++) {
            cumulative += degrees[i] + 1; // +1 ensures nodes with degree 0 still have some probability
            if (target < cumulative) {
                return i;
            }
        }

        // Fallback (should not reach here)
        return random.nextInt(NUM_NODES);
    }
}
