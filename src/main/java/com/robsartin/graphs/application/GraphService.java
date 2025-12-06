package com.robsartin.graphs.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.domain.models.Graph;
import com.robsartin.graphs.domain.ports.out.GraphRepository;
import com.robsartin.graphs.infrastructure.ImmutableGraph;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    private final GraphRepository graphRepository;
    private final ObjectMapper objectMapper;

    public GraphService(GraphRepository graphRepository, ObjectMapper objectMapper) {
        this.graphRepository = graphRepository;
        this.objectMapper = objectMapper;
    }

    public Graph createGraph(String name, String description, String nodeType, String edgeType) {
        Graph graph = new Graph(name, description, nodeType, edgeType);
        return graphRepository.save(graph);
    }

    public Optional<Graph> findById(Long id) {
        return graphRepository.findById(id);
    }

    public List<Graph> findAll() {
        return graphRepository.findAll();
    }

    public void deleteById(Long id) {
        graphRepository.deleteById(id);
    }

    public Graph addNode(Long graphId, String nodeLabel) {
        Graph currentGraph = graphRepository.findById(graphId)
                .orElseThrow(() -> new IllegalArgumentException("Graph not found: " + graphId));

        ImmutableGraph<String, String> immutableGraph = deserializeGraph(currentGraph);
        ImmutableGraph.GraphWithNode<String, String> result = immutableGraph.addNode(nodeLabel);

        Graph newVersion = new Graph(
                currentGraph.getName(),
                currentGraph.getDescription(),
                currentGraph.getVersion() + 1,
                currentGraph.getId(),
                serializeGraph(result.graph()),
                currentGraph.getNodeType(),
                currentGraph.getEdgeType()
        );

        return graphRepository.save(newVersion);
    }

    public Graph addEdge(Long graphId, int fromNodeId, int toNodeId, String edgeLabel) {
        Graph currentGraph = graphRepository.findById(graphId)
                .orElseThrow(() -> new IllegalArgumentException("Graph not found: " + graphId));

        ImmutableGraph<String, String> immutableGraph = deserializeGraph(currentGraph);
        ImmutableGraph<String, String> newGraph = immutableGraph.addEdge(fromNodeId, toNodeId, edgeLabel);

        Graph newVersion = new Graph(
                currentGraph.getName(),
                currentGraph.getDescription(),
                currentGraph.getVersion() + 1,
                currentGraph.getId(),
                serializeGraph(newGraph),
                currentGraph.getNodeType(),
                currentGraph.getEdgeType()
        );

        return graphRepository.save(newVersion);
    }

    public List<Integer> depthFirstTraversal(Long graphId) {
        Graph graph = graphRepository.findById(graphId)
                .orElseThrow(() -> new IllegalArgumentException("Graph not found: " + graphId));

        ImmutableGraph<String, String> immutableGraph = deserializeGraph(graph);
        return immutableGraph.depthFirstTraversal();
    }

    public List<Integer> breadthFirstTraversal(Long graphId) {
        Graph graph = graphRepository.findById(graphId)
                .orElseThrow(() -> new IllegalArgumentException("Graph not found: " + graphId));

        ImmutableGraph<String, String> immutableGraph = deserializeGraph(graph);
        return immutableGraph.breadthFirstTraversal();
    }

    public Map<String, Object> getGraphStructure(Long graphId) {
        Graph graph = graphRepository.findById(graphId)
                .orElseThrow(() -> new IllegalArgumentException("Graph not found: " + graphId));

        ImmutableGraph<String, String> immutableGraph = deserializeGraph(graph);

        Map<String, Object> structure = new HashMap<>();
        structure.put("id", graph.getId());
        structure.put("name", graph.getName());
        structure.put("description", graph.getDescription());
        structure.put("version", graph.getVersion());
        structure.put("parentId", graph.getParentId());
        structure.put("nodeCount", immutableGraph.nodeCount());
        structure.put("edgeCount", immutableGraph.edgeCount());
        structure.put("createdAt", graph.getCreatedAt());

        return structure;
    }

    private String serializeGraph(ImmutableGraph<String, String> graph) {
        Map<String, Object> data = new HashMap<>();

        Map<Integer, String> nodes = new HashMap<>();
        Map<String, List<Map<String, Object>>> edges = new HashMap<>();

        for (int nodeId : graph.getNodes()) {
            ImmutableGraph.Decomposition<String, String> decomp = graph.match(nodeId);
            if (decomp.context() != null) {
                nodes.put(nodeId, decomp.context().nodeLabel());

                List<Map<String, Object>> outEdges = new ArrayList<>();
                decomp.context().successors().forEach((targetId, edgeLabel) -> {
                    Map<String, Object> edge = new HashMap<>();
                    edge.put("to", targetId);
                    edge.put("label", edgeLabel);
                    outEdges.add(edge);
                });

                if (!outEdges.isEmpty()) {
                    edges.put(String.valueOf(nodeId), outEdges);
                }
            }
        }

        data.put("nodes", nodes);
        data.put("edges", edges);

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize graph", e);
        }
    }

    private ImmutableGraph<String, String> deserializeGraph(Graph graph) {
        try {
            Map<String, Object> data = objectMapper.readValue(
                    graph.getGraphData(),
                    new TypeReference<Map<String, Object>>() {}
            );

            ImmutableGraph<String, String> immutableGraph = new ImmutableGraph<>();

            @SuppressWarnings("unchecked")
            Map<String, String> nodes = (Map<String, String>) data.getOrDefault("nodes", new HashMap<>());

            Map<Integer, Integer> oldToNewIdMap = new HashMap<>();

            for (Map.Entry<String, String> entry : nodes.entrySet()) {
                int oldId = Integer.parseInt(entry.getKey());
                ImmutableGraph.GraphWithNode<String, String> result = immutableGraph.addNode(entry.getValue());
                immutableGraph = result.graph();
                oldToNewIdMap.put(oldId, result.nodeId());
            }

            @SuppressWarnings("unchecked")
            Map<String, List<Map<String, Object>>> edges =
                    (Map<String, List<Map<String, Object>>>) data.getOrDefault("edges", new HashMap<>());

            for (Map.Entry<String, List<Map<String, Object>>> entry : edges.entrySet()) {
                int fromId = oldToNewIdMap.get(Integer.parseInt(entry.getKey()));

                for (Map<String, Object> edge : entry.getValue()) {
                    int toId = oldToNewIdMap.get(((Number) edge.get("to")).intValue());
                    String label = (String) edge.get("label");
                    immutableGraph = immutableGraph.addEdge(fromId, toId, label);
                }
            }

            return immutableGraph;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize graph", e);
        }
    }
}
