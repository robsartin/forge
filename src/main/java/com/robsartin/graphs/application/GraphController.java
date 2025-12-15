package com.robsartin.graphs.application;

import com.robsartin.graphs.infrastructure.FeatureFlagService;
import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.models.GraphNode;
import com.robsartin.graphs.ports.out.GraphRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for managing graphs.
 * Handles HTTP requests for graph operations including nodes, edges, and traversals.
 */
@RestController
@RequestMapping("/graphs")
public class GraphController {

    private final GraphRepository graphRepository;
    private final FeatureFlagService featureFlagService;

    public GraphController(GraphRepository graphRepository, FeatureFlagService featureFlagService) {
        this.graphRepository = graphRepository;
        this.featureFlagService = featureFlagService;
    }

    /**
     * GET /graphs - Retrieves all graphs
     *
     * @return list of all graphs (id and name)
     */
    @GetMapping
    public List<GraphSummaryResponse> getAllGraphs() {
        return graphRepository.findAll().stream()
                .map(g -> new GraphSummaryResponse(g.getId(), g.getName()))
                .toList();
    }

    /**
     * GET /graphs/{id} - Retrieves a specific graph by ID
     *
     * @param id the graph ID
     * @return the graph if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<GraphSummaryResponse> getGraphById(@PathVariable Integer id) {
        return graphRepository.findById(id)
                .map(g -> ResponseEntity.ok(new GraphSummaryResponse(g.getId(), g.getName())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /graphs - Creates a new graph
     *
     * @param request the graph creation request
     * @return the created graph with HTTP 201 status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GraphSummaryResponse createGraph(@Valid @RequestBody CreateGraphRequest request) {
        Graph graph = new Graph(request.name());
        Graph savedGraph = graphRepository.save(graph);
        return new GraphSummaryResponse(savedGraph.getId(), savedGraph.getName());
    }

    /**
     * DELETE /graphs/{id} - Deletes a graph by ID
     *
     * @param id the graph ID to delete
     * @return 204 No Content if deleted, 404 if not found, 403 if delete is disabled
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGraph(@PathVariable Integer id) {
        if (!featureFlagService.isDeleteEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!graphRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        graphRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /graphs/{id}/nodes - Retrieves all nodes in a graph
     *
     * @param id the graph ID
     * @return list of all nodes in the graph
     */
    @GetMapping("/{id}/nodes")
    public ResponseEntity<List<NodeResponse>> getAllNodes(@PathVariable Integer id) {
        return graphRepository.findById(id)
                .map(graph -> {
                    List<NodeResponse> nodes = graph.getNodes().stream()
                            .map(n -> new NodeResponse(n.getId(), n.getName()))
                            .toList();
                    return ResponseEntity.ok(nodes);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /graphs/{id}/nodes - Creates a new node in a graph
     *
     * @param id the graph ID
     * @param request the node creation request
     * @return the created node with HTTP 201 status
     */
    @PostMapping("/{id}/nodes")
    public ResponseEntity<NodeResponse> createNode(@PathVariable Integer id,
                                                   @Valid @RequestBody CreateNodeRequest request) {
        return graphRepository.findById(id)
                .map(graph -> {
                    GraphNode node = graph.addNode(request.name());
                    Graph savedGraph = graphRepository.save(graph);
                    GraphNode savedNode = savedGraph.getNodes().stream()
                            .filter(n -> n.getName().equals(request.name()))
                            .reduce((first, second) -> second)
                            .orElse(node);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(new NodeResponse(savedNode.getId(), savedNode.getName()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}")
    public ResponseEntity<NodeResponse> createNodeAlt(@PathVariable Integer id,
                                                   @Valid @RequestBody CreateNodeRequest request) {

        return createNode(id, request);
    }

    /**
     * GET /graphs/{id}/nodes/{nodeId} - Retrieves a specific node by ID with its linked nodes
     *
     * @param id the graph ID
     * @param nodeId the node ID
     * @return the node with its linked nodes if found, 404 if not found
     */
    @GetMapping("/{id}/nodes/{nodeId}")
    public ResponseEntity<NodeWithLinksResponse> getNodeById(@PathVariable Integer id,
                                                              @PathVariable Integer nodeId) {
        return graphRepository.findById(id)
                .flatMap(graph -> graph.getNodes().stream()
                        .filter(n -> n.getId().equals(nodeId))
                        .findFirst()
                        .map(node -> {
                            NodeInfoResponse nodeInfo = new NodeInfoResponse(node.getGraphNodeId(), node.getName());
                            List<NodeInfoResponse> toNodes = new ArrayList<>();

                            if (node.getGraphNodeId() != null) {
                                var context = graph.getImmutableGraph().getContext(node.getGraphNodeId());
                                if (context != null) {
                                    for (Integer successorId : context.getSuccessors().keySet()) {
                                        GraphNode linkedNode = graph.findNodeByGraphNodeId(successorId);
                                        if (linkedNode != null) {
                                            toNodes.add(new NodeInfoResponse(linkedNode.getGraphNodeId(), linkedNode.getName()));
                                        }
                                    }
                                }
                            }

                            return ResponseEntity.ok(new NodeWithLinksResponse(nodeInfo, toNodes));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /graphs/{id}/nodes/{fromId}/{toId} - Adds an edge between two nodes
     *
     * @param id the graph ID
     * @param fromId the source node graph node ID
     * @param toId the target node graph node ID
     * @return 200 OK if edge added, 404 if graph not found, 400 if nodes not found
     */
    @PostMapping("/{id}/nodes/{fromId}/{toId}")
    public ResponseEntity<Void> addEdge(@PathVariable Integer id,
                                        @PathVariable Integer fromId,
                                        @PathVariable Integer toId) {
        return graphRepository.findById(id)
                .map(graph -> {
                    // Find nodes by graphNodeId
                    GraphNode fromNode = graph.getNodes().stream()
                            .filter(n -> n.getGraphNodeId().equals(fromId))
                            .findFirst()
                            .orElse(null);
                    GraphNode toNode = graph.getNodes().stream()
                            .filter(n -> n.getGraphNodeId().equals(toId))
                            .findFirst()
                            .orElse(null);

                    if (fromNode == null || toNode == null) {
                        throw new IllegalArgumentException("Both nodes must exist in the graph");
                    }

                    graph.addEdge(fromNode.getGraphNodeId(), toNode.getGraphNodeId());
                    graphRepository.save(graph);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /graphs/{id}/dfs/{nodeId} - Performs depth-first search from a node
     *
     * @param id the graph ID
     * @param nodeId the starting node graph ID
     * @return list of nodes visited in DFS order
     */
    @GetMapping("/{id}/dfs/{nodeId}")
    public ResponseEntity<List<NodeResponse>> depthFirstSearch(@PathVariable Integer id,
                                                               @PathVariable Integer nodeId) {
        return graphRepository.findById(id)
                .map(graph -> {
                    List<NodeResponse> visited = new ArrayList<>();
                    graph.getImmutableGraph().depthFirstTraversal(nodeId, context -> {
                        GraphNode node = graph.findNodeByGraphNodeId(context.getNodeId());
                        if (node != null) {
                            visited.add(new NodeResponse(node.getId(), node.getName()));
                        } else {
                            visited.add(new NodeResponse(context.getNodeId(), context.getLabel()));
                        }
                    });
                    return ResponseEntity.ok(visited);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /graphs/{id}/bfs/{nodeId} - Performs breadth-first search from a node
     *
     * @param id the graph ID
     * @param nodeId the starting node graph ID
     * @return list of nodes visited in BFS order
     */
    @GetMapping("/{id}/bfs/{nodeId}")
    public ResponseEntity<List<NodeResponse>> breadthFirstSearch(@PathVariable Integer id,
                                                                 @PathVariable Integer nodeId) {
        return graphRepository.findById(id)
                .map(graph -> {
                    List<NodeResponse> visited = new ArrayList<>();
                    graph.getImmutableGraph().breadthFirstTraversal(nodeId, context -> {
                        GraphNode node = graph.findNodeByGraphNodeId(context.getNodeId());
                        if (node != null) {
                            visited.add(new NodeResponse(node.getId(), node.getName()));
                        } else {
                            visited.add(new NodeResponse(context.getNodeId(), context.getLabel()));
                        }
                    });
                    return ResponseEntity.ok(visited);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Request DTO for creating a graph
     */
    public record CreateGraphRequest(@NotBlank(message = "Name is required") String name) {
    }

    /**
     * Request DTO for creating a node
     */
    public record CreateNodeRequest(@NotBlank(message = "Name is required") String name) {
    }

    /**
     * Response DTO for graph summary
     */
    public record GraphSummaryResponse(Integer id, String name) {
    }

    /**
     * Response DTO for node
     */
    public record NodeResponse(Integer id, String name) {
    }

    /**
     * Response DTO for node info with graphNodeId
     */
    public record NodeInfoResponse(Integer graphNodeId, String name) {
    }

    /**
     * Response DTO for node with its linked nodes
     */
    public record NodeWithLinksResponse(NodeInfoResponse node, List<NodeInfoResponse> toNodes) {
    }
}
