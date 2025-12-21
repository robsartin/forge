package com.robsartin.graphs.application;

import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.models.GraphNode;
import com.robsartin.graphs.ports.out.GraphRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing graphs.
 * Handles HTTP requests for graph operations including nodes, edges, and traversals.
 */
@RestController
@RequestMapping("/graphs")
@Tag(name = "Graphs", description = "Graph management API for creating, querying, and manipulating graphs")
public class GraphController {

    private final GraphRepository graphRepository;

    public GraphController(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    /**
     * GET /graphs - Retrieves all graphs
     *
     * @return list of all graphs (id and name)
     */
    @GetMapping
    @Operation(summary = "Get all graphs", description = "Retrieves a list of all graphs with their IDs and names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of graphs",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GraphSummaryResponse.class))))
    })
    @Timed(value = "graph.getAll", description = "Time taken to retrieve all graphs")
    @CircuitBreaker(name = "graphService")
    @RateLimiter(name = "graphService")
    @Retry(name = "graphService")
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
    @Operation(summary = "Get graph by ID", description = "Retrieves a specific graph by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Graph found",
                    content = @Content(schema = @Schema(implementation = GraphSummaryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Graph not found", content = @Content)
    })
    @Timed(value = "graph.getById", description = "Time taken to retrieve a graph by ID")
    @CircuitBreaker(name = "graphService")
    @RateLimiter(name = "graphService")
    @Retry(name = "graphService")
    public ResponseEntity<GraphSummaryResponse> getGraphById(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id) {
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
    @Operation(summary = "Create a new graph", description = "Creates a new graph with the specified name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Graph created successfully",
                    content = @Content(schema = @Schema(implementation = GraphSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - name is required", content = @Content)
    })
    @Timed(value = "graph.create", description = "Time taken to create a graph")
    @CircuitBreaker(name = "graphService")
    @RateLimiter(name = "graphService")
    @Retry(name = "graphService")
    public GraphSummaryResponse createGraph(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Graph creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateGraphRequest.class)))
            @Valid @RequestBody CreateGraphRequest request) {
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
    @Operation(summary = "Delete a graph", description = "Deletes a graph by its ID. Requires delete feature flag to be enabled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Graph deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Delete operation is disabled via feature flag"),
            @ApiResponse(responseCode = "404", description = "Graph not found")
    })
    @Timed(value = "graph.delete", description = "Time taken to delete a graph")
    @CircuitBreaker(name = "graphService")
    @RateLimiter(name = "graphService")
    @Retry(name = "graphService")
    public ResponseEntity<Void> deleteGraph(
            @Parameter(description = "Graph ID to delete", required = true) @PathVariable UUID id) {
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
    @Operation(summary = "Get all nodes in a graph", description = "Retrieves all nodes belonging to a specific graph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved nodes",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NodeResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Graph not found", content = @Content)
    })
    @Timed(value = "node.getAll", description = "Time taken to retrieve all nodes in a graph")
    @CircuitBreaker(name = "nodeService")
    @RateLimiter(name = "nodeService")
    @Retry(name = "nodeService")
    public ResponseEntity<List<NodeResponse>> getAllNodes(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id) {
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
    @Operation(summary = "Create a new node", description = "Creates a new node in the specified graph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Node created successfully",
                    content = @Content(schema = @Schema(implementation = NodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - name is required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Graph not found", content = @Content)
    })
    @Timed(value = "node.create", description = "Time taken to create a node")
    @CircuitBreaker(name = "nodeService")
    @RateLimiter(name = "nodeService")
    @Retry(name = "nodeService")
    public ResponseEntity<NodeResponse> createNode(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Node creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateNodeRequest.class)))
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
    @Operation(summary = "Create a new node (alternative)", description = "Alternative endpoint to create a new node in the specified graph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Node created successfully",
                    content = @Content(schema = @Schema(implementation = NodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - name is required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Graph not found", content = @Content)
    })
    @Timed(value = "node.createAlt", description = "Time taken to create a node (alternative endpoint)")
    @CircuitBreaker(name = "nodeService")
    @RateLimiter(name = "nodeService")
    @Retry(name = "nodeService")
    public ResponseEntity<NodeResponse> createNodeAlt(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
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
    @Operation(summary = "Get node by ID with links", description = "Retrieves a specific node along with its connected successor nodes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Node found with linked nodes",
                    content = @Content(schema = @Schema(implementation = NodeWithLinksResponse.class))),
            @ApiResponse(responseCode = "404", description = "Graph or node not found", content = @Content)
    })
    @Timed(value = "node.getById", description = "Time taken to retrieve a node by ID")
    @CircuitBreaker(name = "nodeService")
    @RateLimiter(name = "nodeService")
    @Retry(name = "nodeService")
    public ResponseEntity<NodeWithLinksResponse> getNodeById(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Node ID", required = true) @PathVariable UUID nodeId) {
        return graphRepository.findById(id)
                .flatMap(graph -> graph.getNodes().stream()
                        .filter(n -> n.getId().equals(nodeId))
                        .findFirst()
                        .map(node -> {
                            NodeInfoResponse nodeInfo = new NodeInfoResponse(node.getId(), node.getName());
                            List<NodeInfoResponse> toNodes = new ArrayList<>();

                            if (node.getId() != null) {
                                var context = graph.getImmutableGraph().getContext(node.getId());
                                if (context != null) {
                                    for (UUID successorId : context.getSuccessors().keySet()) {
                                        GraphNode linkedNode = graph.findNodeById(successorId);
                                        if (linkedNode != null) {
                                            toNodes.add(new NodeInfoResponse(linkedNode.getId(), linkedNode.getName()));
                                        }
                                    }
                                }
                            }

                            return ResponseEntity.ok(new NodeWithLinksResponse(nodeInfo, toNodes));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /graphs/{id}/nodes/{nodeId} - Updates a node's name
     *
     * @param id the graph ID
     * @param nodeId the node ID
     * @param request the update request containing the new name
     * @return the updated node if found, 404 if not found
     */
    @PatchMapping("/{id}/nodes/{nodeId}")
    @Operation(summary = "Update node name", description = "Updates the name of a specific node in the graph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Node updated successfully",
                    content = @Content(schema = @Schema(implementation = NodeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Graph or node not found", content = @Content)
    })
    @Timed(value = "node.update", description = "Time taken to update a node")
    @CircuitBreaker(name = "nodeService")
    @RateLimiter(name = "nodeService")
    @Retry(name = "nodeService")
    public ResponseEntity<NodeResponse> updateNode(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Node ID", required = true) @PathVariable UUID nodeId,
            @Valid @RequestBody CreateNodeRequest request) {
        return graphRepository.findById(id)
                .map(graph -> graph.getNodes().stream()
                        .filter(n -> n.getId().equals(nodeId))
                        .findFirst()
                        .map(node -> {
                            node.setName(request.name());
                            graphRepository.save(graph);
                            return ResponseEntity.ok(new NodeResponse(node.getId(), node.getName()));
                        })
                        .orElse(ResponseEntity.notFound().build()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /graphs/{id}/nodes/{nodeId} - Deletes a node and all incident edges
     *
     * @param id the graph ID
     * @param nodeId the node ID to delete
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{id}/nodes/{nodeId}")
    @Operation(summary = "Delete a node", description = "Deletes a node and all edges connected to it")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Node deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Graph or node not found")
    })
    @Timed(value = "node.delete", description = "Time taken to delete a node")
    @CircuitBreaker(name = "nodeService")
    @RateLimiter(name = "nodeService")
    @Retry(name = "nodeService")
    public ResponseEntity<Void> deleteNode(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Node ID to delete", required = true) @PathVariable UUID nodeId) {
        return graphRepository.findById(id)
                .map(graph -> {
                    if (graph.removeNode(nodeId)) {
                        graphRepository.save(graph);
                        return ResponseEntity.noContent().<Void>build();
                    }
                    return ResponseEntity.notFound().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /graphs/{id}/nodes/{fromId}/{toId} - Adds an edge between two nodes
     *
     * @param id the graph ID
     * @param fromId the source node ID
     * @param toId the target node ID
     * @return 200 OK if edge added, 404 if graph not found, 400 if nodes not found
     */
    @PostMapping("/{id}/nodes/{fromId}/{toId}")
    @Operation(summary = "Add edge between nodes", description = "Creates a directed edge from one node to another in the specified graph")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edge added successfully"),
            @ApiResponse(responseCode = "400", description = "One or both nodes not found in the graph"),
            @ApiResponse(responseCode = "404", description = "Graph not found")
    })
    @Timed(value = "edge.add", description = "Time taken to add an edge")
    @CircuitBreaker(name = "nodeService")
    @RateLimiter(name = "nodeService")
    @Retry(name = "nodeService")
    public ResponseEntity<Void> addEdge(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Source node ID", required = true) @PathVariable UUID fromId,
            @Parameter(description = "Target node ID", required = true) @PathVariable UUID toId) {
        return graphRepository.findById(id)
                .map(graph -> {
                    // Find nodes by ID
                    GraphNode fromNode = graph.getNodes().stream()
                            .filter(n -> n.getId().equals(fromId))
                            .findFirst()
                            .orElse(null);
                    GraphNode toNode = graph.getNodes().stream()
                            .filter(n -> n.getId().equals(toId))
                            .findFirst()
                            .orElse(null);

                    if (fromNode == null || toNode == null) {
                        throw new IllegalArgumentException("Both nodes must exist in the graph");
                    }

                    graph.addEdge(fromNode.getId(), toNode.getId());
                    graphRepository.save(graph);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /graphs/{id}/nodes/{fromId}/{toId} - Deletes an edge between two nodes
     *
     * @param id the graph ID
     * @param fromId the source node ID
     * @param toId the target node ID
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{id}/nodes/{fromId}/{toId}")
    @Operation(summary = "Delete edge between nodes", description = "Removes a directed edge from one node to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Edge deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Graph or edge not found")
    })
    @Timed(value = "edge.delete", description = "Time taken to delete an edge")
    @CircuitBreaker(name = "nodeService")
    @RateLimiter(name = "nodeService")
    @Retry(name = "nodeService")
    public ResponseEntity<Void> deleteEdge(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Source node ID", required = true) @PathVariable UUID fromId,
            @Parameter(description = "Target node ID", required = true) @PathVariable UUID toId) {
        return graphRepository.findById(id)
                .map(graph -> {
                    if (graph.removeEdge(fromId, toId)) {
                        graphRepository.save(graph);
                        return ResponseEntity.noContent().<Void>build();
                    }
                    return ResponseEntity.notFound().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /graphs/{id}/dfs/{nodeId} - Performs depth-first search from a node
     *
     * @param id the graph ID
     * @param nodeId the starting node ID
     * @return list of nodes visited in DFS order
     */
    @GetMapping("/{id}/dfs/{nodeId}")
    @Operation(summary = "Depth-first search", description = "Performs a depth-first traversal of the graph starting from the specified node")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DFS completed successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NodeResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Graph not found", content = @Content)
    })
    @Timed(value = "traversal.dfs", description = "Time taken to perform depth-first search")
    @CircuitBreaker(name = "traversalService")
    @RateLimiter(name = "traversalService")
    @Retry(name = "traversalService")
    public ResponseEntity<List<NodeResponse>> depthFirstSearch(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Starting node ID for traversal", required = true) @PathVariable UUID nodeId) {
        return graphRepository.findById(id)
                .map(graph -> {
                    List<NodeResponse> visited = new ArrayList<>();
                    graph.getImmutableGraph().depthFirstTraversal(nodeId, context -> {
                        GraphNode node = graph.findNodeById(context.getNodeId());
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
     * @param nodeId the starting node ID
     * @return list of nodes visited in BFS order
     */
    @GetMapping("/{id}/bfs/{nodeId}")
    @Operation(summary = "Breadth-first search", description = "Performs a breadth-first traversal of the graph starting from the specified node")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "BFS completed successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NodeResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Graph not found", content = @Content)
    })
    @Timed(value = "traversal.bfs", description = "Time taken to perform breadth-first search")
    @CircuitBreaker(name = "traversalService")
    @RateLimiter(name = "traversalService")
    @Retry(name = "traversalService")
    public ResponseEntity<List<NodeResponse>> breadthFirstSearch(
            @Parameter(description = "Graph ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Starting node ID for traversal", required = true) @PathVariable UUID nodeId) {
        return graphRepository.findById(id)
                .map(graph -> {
                    List<NodeResponse> visited = new ArrayList<>();
                    graph.getImmutableGraph().breadthFirstTraversal(nodeId, context -> {
                        GraphNode node = graph.findNodeById(context.getNodeId());
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
    @Schema(description = "Request body for creating a new graph")
    public record CreateGraphRequest(
            @Schema(description = "Name of the graph", example = "My Graph", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "Name is required") String name) {
    }

    /**
     * Request DTO for creating a node
     */
    @Schema(description = "Request body for creating a new node")
    public record CreateNodeRequest(
            @Schema(description = "Name of the node", example = "Node A", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "Name is required") String name) {
    }

    /**
     * Response DTO for graph summary
     */
    @Schema(description = "Summary information about a graph")
    public record GraphSummaryResponse(
            @Schema(description = "Unique identifier of the graph", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID id,
            @Schema(description = "Name of the graph", example = "My Graph")
            String name) {
    }

    /**
     * Response DTO for node
     */
    @Schema(description = "Basic information about a node")
    public record NodeResponse(
            @Schema(description = "Unique identifier of the node", example = "550e8400-e29b-41d4-a716-446655440001")
            UUID id,
            @Schema(description = "Name of the node", example = "Node A")
            String name) {
    }

    /**
     * Response DTO for node info
     */
    @Schema(description = "Node information including ID")
    public record NodeInfoResponse(
            @Schema(description = "Unique identifier of the node", example = "550e8400-e29b-41d4-a716-446655440001")
            UUID id,
            @Schema(description = "Name of the node", example = "Node A")
            String name) {
    }

    /**
     * Response DTO for node with its linked nodes
     */
    @Schema(description = "Node with its outgoing edge connections")
    public record NodeWithLinksResponse(
            @Schema(description = "The requested node")
            NodeInfoResponse node,
            @Schema(description = "List of nodes connected via outgoing edges")
            List<NodeInfoResponse> toNodes) {
    }
}
