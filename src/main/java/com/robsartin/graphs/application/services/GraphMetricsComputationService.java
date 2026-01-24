package com.robsartin.graphs.application.services;

import com.robsartin.graphs.infrastructure.ImmutableGraph;
import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.models.GraphDegreeDistribution;
import com.robsartin.graphs.models.GraphMetrics;
import com.robsartin.graphs.models.GraphNodeMetrics;
import com.robsartin.graphs.ports.out.GraphDegreeDistributionRepository;
import com.robsartin.graphs.ports.out.GraphMetricsRepository;
import com.robsartin.graphs.ports.out.GraphNodeMetricsRepository;
import com.robsartin.graphs.ports.out.GraphRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * Service responsible for computing graph metrics.
 * All computations are performed on the ImmutableGraph representation.
 */
@Service
public class GraphMetricsComputationService {

    private static final Logger log = LoggerFactory.getLogger(GraphMetricsComputationService.class);

    private final GraphRepository graphRepository;
    private final GraphMetricsRepository metricsRepository;
    private final GraphNodeMetricsRepository nodeMetricsRepository;
    private final GraphDegreeDistributionRepository degreeDistRepository;

    public GraphMetricsComputationService(
            GraphRepository graphRepository,
            GraphMetricsRepository metricsRepository,
            GraphNodeMetricsRepository nodeMetricsRepository,
            GraphDegreeDistributionRepository degreeDistRepository) {
        this.graphRepository = graphRepository;
        this.metricsRepository = metricsRepository;
        this.nodeMetricsRepository = nodeMetricsRepository;
        this.degreeDistRepository = degreeDistRepository;
    }

    @Transactional
    public void computeAndSaveMetrics(UUID graphId) {
        log.info("Computing metrics for graph {}", graphId);

        Optional<Graph> graphOpt = graphRepository.findById(graphId);
        if (graphOpt.isEmpty()) {
            log.warn("Graph {} not found, skipping metrics computation", graphId);
            return;
        }

        Graph graph = graphOpt.get();
        ImmutableGraph<String, String> immutableGraph = graph.getImmutableGraph();

        // Clear existing metrics for this graph
        metricsRepository.deleteByGraphId(graphId);
        nodeMetricsRepository.deleteByGraphId(graphId);
        degreeDistRepository.deleteByGraphId(graphId);

        // Compute and save aggregate metrics
        GraphMetrics metrics = computeAggregateMetrics(graph, immutableGraph);
        metricsRepository.save(metrics);

        // Compute and save per-node metrics
        List<GraphNodeMetrics> nodeMetricsList = computeNodeMetrics(graph, immutableGraph);
        nodeMetricsRepository.saveAll(nodeMetricsList);

        // Compute and save degree distribution
        List<GraphDegreeDistribution> degreeDistList = computeDegreeDistribution(graph, immutableGraph);
        degreeDistRepository.saveAll(degreeDistList);

        log.info("Computed metrics for graph {} with {} nodes and {} edges",
                graphId, metrics.getNodeCount(), metrics.getEdgeCount());
    }

    private GraphMetrics computeAggregateMetrics(Graph graph, ImmutableGraph<String, String> immutableGraph) {
        GraphMetrics metrics = new GraphMetrics(graph);

        int nodeCount = immutableGraph.nodeCount();
        int edgeCount = graph.getEdges().size();

        metrics.setNodeCount(nodeCount);
        metrics.setEdgeCount(edgeCount);

        // Density calculation (for directed graph)
        if (nodeCount <= 1) {
            metrics.setDensity(0.0);
        } else {
            double maxEdges = (double) nodeCount * (nodeCount - 1);
            metrics.setDensity(edgeCount / maxEdges);
        }

        // Average degree (treating as directed: in-degree + out-degree)
        if (nodeCount == 0) {
            metrics.setAverageDegree(0.0);
        } else {
            metrics.setAverageDegree((2.0 * edgeCount) / nodeCount);
        }

        // Connectivity analysis (treating graph as undirected for weak connectivity)
        ConnectivityResult connectivity = analyzeConnectivity(immutableGraph);
        metrics.setConnected(connectivity.isConnected());
        metrics.setComponentCount(connectivity.componentCount());

        // Path metrics (only for connected graphs with more than 1 node)
        if (connectivity.isConnected() && nodeCount > 1) {
            PathMetrics pathMetrics = computePathMetrics(immutableGraph);
            metrics.setDiameter(pathMetrics.diameter());
            metrics.setAveragePathLength(pathMetrics.averagePathLength());
        }

        // Average clustering coefficient
        double avgClustering = computeAverageClusteringCoefficient(immutableGraph);
        metrics.setAverageClusteringCoefficient(avgClustering);

        return metrics;
    }

    private List<GraphNodeMetrics> computeNodeMetrics(Graph graph, ImmutableGraph<String, String> immutableGraph) {
        List<GraphNodeMetrics> nodeMetricsList = new ArrayList<>();
        int nodeCount = immutableGraph.nodeCount();

        if (nodeCount == 0) {
            return nodeMetricsList;
        }

        // Precompute all shortest paths for betweenness and closeness
        Map<UUID, Map<UUID, Integer>> allPairsShortestPaths = computeAllPairsShortestPaths(immutableGraph);

        // Compute betweenness centrality for all nodes
        Map<UUID, Double> betweennessCentralities = computeAllBetweennessCentralities(immutableGraph, allPairsShortestPaths);

        for (UUID nodeId : immutableGraph.getNodeIds()) {
            GraphNodeMetrics nodeMetrics = new GraphNodeMetrics(graph, nodeId);

            ImmutableGraph.Context<String, String> context = immutableGraph.getContext(nodeId);
            int inDegree = context.getPredecessors().size();
            int outDegree = context.getSuccessors().size();

            nodeMetrics.setInDegree(inDegree);
            nodeMetrics.setOutDegree(outDegree);

            // Degree centrality (using total degree for directed graph)
            int totalDegree = inDegree + outDegree;
            if (nodeCount <= 1) {
                nodeMetrics.setDegreeCentrality(0.0);
            } else {
                // Normalized by max possible degree (2*(n-1) for directed graph)
                nodeMetrics.setDegreeCentrality((double) totalDegree / (2.0 * (nodeCount - 1)));
            }

            // Betweenness centrality
            nodeMetrics.setBetweennessCentrality(betweennessCentralities.getOrDefault(nodeId, 0.0));

            // Closeness centrality
            double closeness = computeClosenessCentrality(nodeId, allPairsShortestPaths.get(nodeId), nodeCount);
            nodeMetrics.setClosenessCentrality(closeness);

            // Local clustering coefficient
            double clustering = computeLocalClusteringCoefficient(nodeId, immutableGraph);
            nodeMetrics.setClusteringCoefficient(clustering);

            nodeMetricsList.add(nodeMetrics);
        }

        return nodeMetricsList;
    }

    private List<GraphDegreeDistribution> computeDegreeDistribution(Graph graph, ImmutableGraph<String, String> immutableGraph) {
        Map<Integer, Integer> distribution = new HashMap<>();

        for (UUID nodeId : immutableGraph.getNodeIds()) {
            ImmutableGraph.Context<String, String> context = immutableGraph.getContext(nodeId);
            int degree = context.getPredecessors().size() + context.getSuccessors().size();
            distribution.merge(degree, 1, Integer::sum);
        }

        List<GraphDegreeDistribution> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : distribution.entrySet()) {
            result.add(new GraphDegreeDistribution(graph, entry.getKey(), entry.getValue()));
        }

        return result;
    }

    private ConnectivityResult analyzeConnectivity(ImmutableGraph<String, String> graph) {
        Set<UUID> nodeIds = graph.getNodeIds();
        if (nodeIds.isEmpty()) {
            return new ConnectivityResult(true, 0);
        }

        // Find weakly connected components using BFS (treating edges as undirected)
        Set<UUID> visited = new HashSet<>();
        int componentCount = 0;

        for (UUID startNode : nodeIds) {
            if (!visited.contains(startNode)) {
                componentCount++;
                bfsUndirected(startNode, graph, visited);
            }
        }

        return new ConnectivityResult(componentCount == 1, componentCount);
    }

    private void bfsUndirected(UUID startNode, ImmutableGraph<String, String> graph, Set<UUID> visited) {
        Queue<UUID> queue = new LinkedList<>();
        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            ImmutableGraph.Context<String, String> context = graph.getContext(current);

            // Visit both predecessors and successors (undirected)
            for (UUID neighbor : context.getSuccessors().keySet()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
            for (UUID neighbor : context.getPredecessors().keySet()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
    }

    private PathMetrics computePathMetrics(ImmutableGraph<String, String> graph) {
        Set<UUID> nodeIds = graph.getNodeIds();
        int nodeCount = nodeIds.size();

        if (nodeCount <= 1) {
            return new PathMetrics(0, 0.0);
        }

        int maxDistance = 0;
        long totalDistance = 0;
        int pathCount = 0;

        // BFS from each node to compute distances
        for (UUID source : nodeIds) {
            Map<UUID, Integer> distances = bfsDistances(source, graph);

            for (Map.Entry<UUID, Integer> entry : distances.entrySet()) {
                if (!entry.getKey().equals(source)) {
                    int dist = entry.getValue();
                    if (dist < Integer.MAX_VALUE) {
                        maxDistance = Math.max(maxDistance, dist);
                        totalDistance += dist;
                        pathCount++;
                    }
                }
            }
        }

        double averagePathLength = pathCount > 0 ? (double) totalDistance / pathCount : 0.0;
        return new PathMetrics(maxDistance, averagePathLength);
    }

    private Map<UUID, Integer> bfsDistances(UUID source, ImmutableGraph<String, String> graph) {
        Map<UUID, Integer> distances = new HashMap<>();
        for (UUID nodeId : graph.getNodeIds()) {
            distances.put(nodeId, Integer.MAX_VALUE);
        }
        distances.put(source, 0);

        Queue<UUID> queue = new LinkedList<>();
        queue.offer(source);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            int currentDist = distances.get(current);
            ImmutableGraph.Context<String, String> context = graph.getContext(current);

            for (UUID neighbor : context.getSuccessors().keySet()) {
                if (distances.get(neighbor) == Integer.MAX_VALUE) {
                    distances.put(neighbor, currentDist + 1);
                    queue.offer(neighbor);
                }
            }
        }

        return distances;
    }

    private Map<UUID, Map<UUID, Integer>> computeAllPairsShortestPaths(ImmutableGraph<String, String> graph) {
        Map<UUID, Map<UUID, Integer>> allPaths = new HashMap<>();

        for (UUID source : graph.getNodeIds()) {
            allPaths.put(source, bfsDistances(source, graph));
        }

        return allPaths;
    }

    private Map<UUID, Double> computeAllBetweennessCentralities(
            ImmutableGraph<String, String> graph,
            Map<UUID, Map<UUID, Integer>> allPairsShortestPaths) {

        Map<UUID, Double> betweenness = new HashMap<>();
        Set<UUID> nodeIds = graph.getNodeIds();

        for (UUID nodeId : nodeIds) {
            betweenness.put(nodeId, 0.0);
        }

        int n = nodeIds.size();
        if (n <= 2) {
            return betweenness;
        }

        // For each pair (s, t), check if node v is on the shortest path
        List<UUID> nodeList = new ArrayList<>(nodeIds);
        for (int i = 0; i < nodeList.size(); i++) {
            UUID s = nodeList.get(i);
            for (int j = 0; j < nodeList.size(); j++) {
                if (i == j) continue;
                UUID t = nodeList.get(j);

                int distST = allPairsShortestPaths.get(s).get(t);
                if (distST == Integer.MAX_VALUE || distST == 0) continue;

                // Find nodes on shortest path from s to t
                for (UUID v : nodeIds) {
                    if (v.equals(s) || v.equals(t)) continue;

                    int distSV = allPairsShortestPaths.get(s).get(v);
                    int distVT = allPairsShortestPaths.get(v).get(t);

                    // v is on shortest path if dist(s,v) + dist(v,t) == dist(s,t)
                    if (distSV != Integer.MAX_VALUE && distVT != Integer.MAX_VALUE
                            && distSV + distVT == distST) {
                        // Count shortest paths through v
                        // Simplified: assume single shortest path
                        betweenness.merge(v, 1.0, Double::sum);
                    }
                }
            }
        }

        // Normalize by (n-1)(n-2) for directed graphs
        double normFactor = (n - 1) * (n - 2);
        if (normFactor > 0) {
            for (UUID nodeId : nodeIds) {
                betweenness.put(nodeId, betweenness.get(nodeId) / normFactor);
            }
        }

        return betweenness;
    }

    private double computeClosenessCentrality(UUID nodeId, Map<UUID, Integer> shortestPaths, int nodeCount) {
        if (nodeCount <= 1 || shortestPaths == null) {
            return 0.0;
        }

        long totalDistance = 0;
        int reachableCount = 0;

        for (Map.Entry<UUID, Integer> entry : shortestPaths.entrySet()) {
            if (!entry.getKey().equals(nodeId) && entry.getValue() < Integer.MAX_VALUE) {
                totalDistance += entry.getValue();
                reachableCount++;
            }
        }

        if (reachableCount == 0 || totalDistance == 0) {
            return 0.0;
        }

        // Closeness = (n-1) / sum of distances (normalized)
        return (double) reachableCount / totalDistance;
    }

    private double computeLocalClusteringCoefficient(UUID nodeId, ImmutableGraph<String, String> graph) {
        ImmutableGraph.Context<String, String> context = graph.getContext(nodeId);

        // Get all neighbors (both predecessors and successors)
        Set<UUID> neighbors = new HashSet<>();
        neighbors.addAll(context.getPredecessors().keySet());
        neighbors.addAll(context.getSuccessors().keySet());

        int k = neighbors.size();
        if (k < 2) {
            return 0.0;
        }

        // Count edges between neighbors
        int edgesBetweenNeighbors = 0;
        List<UUID> neighborList = new ArrayList<>(neighbors);

        for (int i = 0; i < neighborList.size(); i++) {
            UUID ni = neighborList.get(i);
            ImmutableGraph.Context<String, String> niContext = graph.getContext(ni);

            for (int j = 0; j < neighborList.size(); j++) {
                if (i == j) continue;
                UUID nj = neighborList.get(j);

                // Check if there's an edge from ni to nj
                if (niContext.getSuccessors().containsKey(nj)) {
                    edgesBetweenNeighbors++;
                }
            }
        }

        // For directed graphs: possible edges = k * (k-1)
        int possibleEdges = k * (k - 1);

        return (double) edgesBetweenNeighbors / possibleEdges;
    }

    private double computeAverageClusteringCoefficient(ImmutableGraph<String, String> graph) {
        Set<UUID> nodeIds = graph.getNodeIds();
        int nodeCount = nodeIds.size();

        if (nodeCount == 0) {
            return 0.0;
        }

        double totalClustering = 0.0;
        for (UUID nodeId : nodeIds) {
            totalClustering += computeLocalClusteringCoefficient(nodeId, graph);
        }

        return totalClustering / nodeCount;
    }

    private record ConnectivityResult(boolean isConnected, int componentCount) {}

    private record PathMetrics(int diameter, double averagePathLength) {}
}
