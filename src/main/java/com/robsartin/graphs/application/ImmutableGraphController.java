package com.robsartin.graphs.application;

import com.robsartin.graphs.models.ImmutableGraphEdgeEntity;
import com.robsartin.graphs.models.ImmutableGraphEntity;
import com.robsartin.graphs.models.ImmutableGraphNodeEntity;
import com.robsartin.graphs.ports.out.ImmutableGraphRepository;
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
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * REST Controller for managing immutable graphs as a whole.
 * Handles HTTP requests for saving and loading entire immutable graphs.
 */
@RestController
@RequestMapping("/immutable-graphs")
@Tag(name = "Immutable Graphs", description = "API for saving and loading entire immutable graphs")
public class ImmutableGraphController {

    private static final Logger log = LoggerFactory.getLogger(ImmutableGraphController.class);

    private final ImmutableGraphRepository immutableGraphRepository;

    public ImmutableGraphController(ImmutableGraphRepository immutableGraphRepository) {
        this.immutableGraphRepository = immutableGraphRepository;
    }

    /**
     * POST /immutable-graphs - Saves an entire immutable graph
     *
     * @param request the graph to save with all nodes and edges
     * @return the saved graph with HTTP 201 status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Save entire immutable graph", description = "Saves an entire immutable graph with all nodes and edges in a single operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Graph saved successfully",
                    content = @Content(schema = @Schema(implementation = ImmutableGraphResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - name is required", content = @Content)
    })
    @Timed(value = "immutableGraph.save", description = "Time taken to save an immutable graph")
    @CircuitBreaker(name = "immutableGraphService")
    @RateLimiter(name = "immutableGraphService")
    @Retry(name = "immutableGraphService")
    public ImmutableGraphResponse saveGraph(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Immutable graph to save",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SaveImmutableGraphRequest.class)))
            @Valid @RequestBody SaveImmutableGraphRequest request) {

        log.info("Saving immutable graph with name: {}, nodes: {}, edges: {}",
                request.name(), request.nodes().size(), request.edges().size());

        ImmutableGraphEntity graph = new ImmutableGraphEntity(request.name());

        for (NodeRequest nodeRequest : request.nodes()) {
            ImmutableGraphNodeEntity node = new ImmutableGraphNodeEntity(
                    nodeRequest.graphNodeId(),
                    nodeRequest.label()
            );
            graph.addNode(node);
        }

        for (EdgeRequest edgeRequest : request.edges()) {
            ImmutableGraphEdgeEntity edge = new ImmutableGraphEdgeEntity(
                    edgeRequest.fromId(),
                    edgeRequest.toId()
            );
            graph.addEdge(edge);
        }

        ImmutableGraphEntity savedGraph = immutableGraphRepository.save(graph);

        log.info("Saved immutable graph with graphId: {}", savedGraph.getGraphId());

        return toResponse(savedGraph);
    }

    /**
     * GET /immutable-graphs/{graphId} - Retrieves an entire immutable graph
     *
     * @param graphId the graph ID
     * @return the graph with all nodes and edges if found, 404 if not found
     */
    @GetMapping("/{graphId}")
    @Operation(summary = "Get entire immutable graph", description = "Retrieves an entire immutable graph with all nodes and edges")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Graph found",
                    content = @Content(schema = @Schema(implementation = ImmutableGraphResponse.class))),
            @ApiResponse(responseCode = "404", description = "Graph not found", content = @Content)
    })
    @Timed(value = "immutableGraph.getById", description = "Time taken to retrieve an immutable graph by ID")
    @CircuitBreaker(name = "immutableGraphService")
    @RateLimiter(name = "immutableGraphService")
    @Retry(name = "immutableGraphService")
    public ResponseEntity<ImmutableGraphResponse> getGraph(
            @Parameter(description = "Graph ID", required = true) @PathVariable Integer graphId) {

        log.info("Retrieving immutable graph with graphId: {}", graphId);

        return immutableGraphRepository.findById(graphId)
                .map(graph -> {
                    log.info("Found immutable graph: {}", graph.getName());
                    return ResponseEntity.ok(toResponse(graph));
                })
                .orElseGet(() -> {
                    log.warn("Immutable graph not found with graphId: {}", graphId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * GET /immutable-graphs - Retrieves all immutable graphs
     *
     * @return list of all graphs with their nodes and edges
     */
    @GetMapping
    @Operation(summary = "Get all immutable graphs", description = "Retrieves a list of all immutable graphs with their nodes and edges")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of graphs",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ImmutableGraphResponse.class))))
    })
    @Timed(value = "immutableGraph.getAll", description = "Time taken to retrieve all immutable graphs")
    @CircuitBreaker(name = "immutableGraphService")
    @RateLimiter(name = "immutableGraphService")
    @Retry(name = "immutableGraphService")
    public List<ImmutableGraphResponse> getAllGraphs() {
        log.info("Retrieving all immutable graphs");

        List<ImmutableGraphResponse> graphs = immutableGraphRepository.findAll().stream()
                .map(this::toResponse)
                .toList();

        log.info("Found {} immutable graphs", graphs.size());

        return graphs;
    }

    /**
     * DELETE /immutable-graphs/{graphId} - Deletes an immutable graph
     *
     * @param graphId the graph ID to delete
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{graphId}")
    @Operation(summary = "Delete an immutable graph", description = "Deletes an immutable graph by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Graph deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Graph not found")
    })
    @Timed(value = "immutableGraph.delete", description = "Time taken to delete an immutable graph")
    @CircuitBreaker(name = "immutableGraphService")
    @RateLimiter(name = "immutableGraphService")
    @Retry(name = "immutableGraphService")
    public ResponseEntity<Void> deleteGraph(
            @Parameter(description = "Graph ID to delete", required = true) @PathVariable Integer graphId) {

        log.info("Deleting immutable graph with graphId: {}", graphId);

        if (!immutableGraphRepository.existsById(graphId)) {
            log.warn("Immutable graph not found for deletion with graphId: {}", graphId);
            return ResponseEntity.notFound().build();
        }

        immutableGraphRepository.deleteById(graphId);

        log.info("Deleted immutable graph with graphId: {}", graphId);

        return ResponseEntity.noContent().build();
    }

    private ImmutableGraphResponse toResponse(ImmutableGraphEntity graph) {
        List<NodeResponse> nodes = graph.getNodes().stream()
                .sorted(Comparator.comparing(ImmutableGraphNodeEntity::getGraphNodeId))
                .map(n -> new NodeResponse(n.getGraphNodeId(), n.getLabel()))
                .toList();

        List<EdgeResponse> edges = graph.getEdges().stream()
                .map(e -> new EdgeResponse(e.getFromId(), e.getToId()))
                .toList();

        return new ImmutableGraphResponse(graph.getGraphId(), graph.getName(), nodes, edges);
    }

    /**
     * Request DTO for saving an immutable graph
     */
    @Schema(description = "Request body for saving an immutable graph")
    public record SaveImmutableGraphRequest(
            @Schema(description = "Name of the graph", example = "My Graph", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "Name is required") String name,
            @Schema(description = "List of nodes in the graph")
            @NotNull List<NodeRequest> nodes,
            @Schema(description = "List of edges in the graph")
            @NotNull List<EdgeRequest> edges) {
    }

    /**
     * Request DTO for a node
     */
    @Schema(description = "Node information for saving")
    public record NodeRequest(
            @Schema(description = "Unique node ID within the graph", example = "0")
            @NotNull Integer graphNodeId,
            @Schema(description = "Label of the node", example = "Node A")
            @NotBlank String label) {
    }

    /**
     * Request DTO for an edge
     */
    @Schema(description = "Edge information for saving")
    public record EdgeRequest(
            @Schema(description = "Source node ID", example = "0")
            @NotNull Integer fromId,
            @Schema(description = "Target node ID", example = "1")
            @NotNull Integer toId) {
    }

    /**
     * Response DTO for an immutable graph
     */
    @Schema(description = "Response containing an immutable graph with all nodes and edges")
    public record ImmutableGraphResponse(
            @Schema(description = "Unique identifier of the graph", example = "1")
            Integer graphId,
            @Schema(description = "Name of the graph", example = "My Graph")
            String name,
            @Schema(description = "List of nodes in the graph")
            List<NodeResponse> nodes,
            @Schema(description = "List of edges in the graph")
            List<EdgeResponse> edges) {
    }

    /**
     * Response DTO for a node
     */
    @Schema(description = "Node information in response")
    public record NodeResponse(
            @Schema(description = "Unique node ID within the graph", example = "0")
            Integer graphNodeId,
            @Schema(description = "Label of the node", example = "Node A")
            String label) {
    }

    /**
     * Response DTO for an edge
     */
    @Schema(description = "Edge information in response")
    public record EdgeResponse(
            @Schema(description = "Source node ID", example = "0")
            Integer fromId,
            @Schema(description = "Target node ID", example = "1")
            Integer toId) {
    }
}
