package com.robsartin.graphs.application;

import com.robsartin.graphs.domain.models.Graph;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/graphs")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping
    public List<Graph> getAllGraphs() {
        return graphService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Graph> getGraphById(@PathVariable Long id) {
        return graphService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Graph createGraph(@Valid @RequestBody CreateGraphRequest request) {
        return graphService.createGraph(
                request.name(),
                request.description(),
                request.nodeType(),
                request.edgeType()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGraph(@PathVariable Long id) {
        if (graphService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        graphService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/nodes")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GraphResponse> addNode(@PathVariable Long id,
                                                  @Valid @RequestBody AddNodeRequest request) {
        if (graphService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Graph newVersion = graphService.addNode(id, request.label());
        Map<String, Object> structure = graphService.getGraphStructure(newVersion.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GraphResponse(newVersion, structure));
    }

    @PostMapping("/{id}/edges")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<GraphResponse> addEdge(@PathVariable Long id,
                                                  @Valid @RequestBody AddEdgeRequest request) {
        if (graphService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Graph newVersion = graphService.addEdge(
                id,
                request.fromNodeId(),
                request.toNodeId(),
                request.label()
        );
        Map<String, Object> structure = graphService.getGraphStructure(newVersion.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GraphResponse(newVersion, structure));
    }

    @GetMapping("/{id}/structure")
    public ResponseEntity<Map<String, Object>> getGraphStructure(@PathVariable Long id) {
        if (graphService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(graphService.getGraphStructure(id));
    }

    @GetMapping("/{id}/traversal/dfs")
    public ResponseEntity<TraversalResponse> depthFirstTraversal(@PathVariable Long id) {
        if (graphService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Integer> traversal = graphService.depthFirstTraversal(id);
        return ResponseEntity.ok(new TraversalResponse("depth-first", traversal));
    }

    @GetMapping("/{id}/traversal/bfs")
    public ResponseEntity<TraversalResponse> breadthFirstTraversal(@PathVariable Long id) {
        if (graphService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Integer> traversal = graphService.breadthFirstTraversal(id);
        return ResponseEntity.ok(new TraversalResponse("breadth-first", traversal));
    }

    public record CreateGraphRequest(
            @NotBlank(message = "Name is required") String name,
            String description,
            @NotBlank(message = "Node type is required") String nodeType,
            @NotBlank(message = "Edge type is required") String edgeType
    ) {}

    public record AddNodeRequest(
            @NotBlank(message = "Label is required") String label
    ) {}

    public record AddEdgeRequest(
            @NotNull(message = "From node ID is required") Integer fromNodeId,
            @NotNull(message = "To node ID is required") Integer toNodeId,
            @NotBlank(message = "Label is required") String label
    ) {}

    public record GraphResponse(
            Graph graph,
            Map<String, Object> structure
    ) {}

    public record TraversalResponse(
            String type,
            List<Integer> nodes
    ) {}
}
