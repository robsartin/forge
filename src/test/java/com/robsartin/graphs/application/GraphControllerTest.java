package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.domain.models.Graph;
import com.robsartin.graphs.domain.models.GraphNode;
import com.robsartin.graphs.domain.ports.out.GraphRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GraphController.class)
class GraphControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private GraphRepository graphRepository;

    @Autowired
    GraphControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    // GET /graphs - list all graphs
    @Test
    void shouldReturnAllGraphs() throws Exception {
        Graph graph1 = new Graph("Graph 1");
        graph1.setId(1);
        Graph graph2 = new Graph("Graph 2");
        graph2.setId(2);
        List<Graph> graphs = Arrays.asList(graph1, graph2);

        when(graphRepository.findAll()).thenReturn(graphs);

        mockMvc.perform(get("/graphs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Graph 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Graph 2"));
    }

    @Test
    void shouldReturnEmptyListWhenNoGraphs() throws Exception {
        when(graphRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/graphs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /graphs/{id} - get graph by id
    @Test
    void shouldReturnGraphById() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);

        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));

        mockMvc.perform(get("/graphs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Graph"));
    }

    @Test
    void shouldReturn404WhenGraphNotFound() throws Exception {
        when(graphRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/graphs/999"))
                .andExpect(status().isNotFound());
    }

    // POST /graphs - create a new graph
    @Test
    void shouldCreateNewGraph() throws Exception {
        Graph savedGraph = new Graph("New Graph");
        savedGraph.setId(1);

        when(graphRepository.save(any(Graph.class))).thenReturn(savedGraph);

        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Graph\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Graph"));
    }

    @Test
    void shouldRejectPostWithoutName() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPostWithEmptyName() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPostWithBlankName() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPostWithNullName() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCallRepositorySaveWhenCreatingGraph() throws Exception {
        Graph savedGraph = new Graph("Test Graph");
        savedGraph.setId(1);

        when(graphRepository.save(any(Graph.class))).thenReturn(savedGraph);

        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated());

        verify(graphRepository).save(any(Graph.class));
    }

    // DELETE /graphs/{id} - delete graph by id
    @Test
    void shouldDeleteGraphById() throws Exception {
        when(graphRepository.existsById(1)).thenReturn(true);
        doNothing().when(graphRepository).deleteById(1);

        mockMvc.perform(delete("/graphs/1"))
                .andExpect(status().isNoContent());

        verify(graphRepository).deleteById(1);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentGraph() throws Exception {
        when(graphRepository.existsById(999)).thenReturn(false);

        mockMvc.perform(delete("/graphs/999"))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/nodes - list all nodes in a graph
    @Test
    void shouldReturnAllNodesInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);
        GraphNode node1 = new GraphNode("Node A", 0);
        node1.setId(1);
        node1.setGraph(graph);
        GraphNode node2 = new GraphNode("Node B", 1);
        node2.setId(2);
        node2.setGraph(graph);
        graph.getNodes().add(node1);
        graph.getNodes().add(node2);

        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));

        mockMvc.perform(get("/graphs/1/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Node A"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Node B"));
    }

    @Test
    void shouldReturn404WhenListingNodesForNonExistentGraph() throws Exception {
        when(graphRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/graphs/999/nodes"))
                .andExpect(status().isNotFound());
    }

    // POST /graphs/{id}/nodes - create a new node in a graph
    @Test
    void shouldCreateNewNodeInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);

        Graph savedGraph = new Graph("Test Graph");
        savedGraph.setId(1);
        GraphNode node = new GraphNode("New Node", 0);
        node.setId(1);
        node.setGraph(savedGraph);
        savedGraph.getNodes().add(node);

        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));
        when(graphRepository.save(any(Graph.class))).thenReturn(savedGraph);

        mockMvc.perform(post("/graphs/1/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Node\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Node"));
    }

    @Test
    void shouldReturn404WhenCreatingNodeInNonExistentGraph() throws Exception {
        when(graphRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(post("/graphs/999/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Node\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectNodeCreationWithoutName() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);
        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));

        mockMvc.perform(post("/graphs/1/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // GET /graphs/{id}/nodes/{nodeId} - get a specific node
    @Test
    void shouldReturnNodeById() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);
        GraphNode node = new GraphNode("Node A", 0);
        node.setId(5);
        node.setGraph(graph);
        graph.getNodes().add(node);

        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));

        mockMvc.perform(get("/graphs/1/nodes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Node A"));
    }

    @Test
    void shouldReturn404WhenNodeNotFoundInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);

        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));

        mockMvc.perform(get("/graphs/1/nodes/999"))
                .andExpect(status().isNotFound());
    }

    // POST /graphs/{id}/nodes/{fromId}/{toId} - add an edge
    @Test
    void shouldAddEdgeBetweenNodes() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);
        GraphNode node1 = new GraphNode("Node A", 0);
        node1.setId(1);
        node1.setGraph(graph);
        GraphNode node2 = new GraphNode("Node B", 1);
        node2.setId(2);
        node2.setGraph(graph);
        graph.getNodes().add(node1);
        graph.getNodes().add(node2);
        // Initialize the immutable graph with nodes
        graph.setImmutableGraph(graph.getImmutableGraph().addNode("Node A").getGraph());
        graph.setImmutableGraph(graph.getImmutableGraph().addNode("Node B").getGraph());

        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));
        when(graphRepository.save(any(Graph.class))).thenReturn(graph);

        mockMvc.perform(post("/graphs/1/nodes/0/1"))
                .andExpect(status().isOk());

        verify(graphRepository).save(any(Graph.class));
    }

    @Test
    void shouldReturn404WhenAddingEdgeToNonExistentGraph() throws Exception {
        when(graphRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(post("/graphs/999/nodes/0/1"))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/dfs/{nodeId} - depth-first search
    @Test
    void shouldPerformDepthFirstSearch() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);
        GraphNode node1 = new GraphNode("Node A", 0);
        node1.setId(1);
        GraphNode node2 = new GraphNode("Node B", 1);
        node2.setId(2);
        GraphNode node3 = new GraphNode("Node C", 2);
        node3.setId(3);
        node1.setGraph(graph);
        node2.setGraph(graph);
        node3.setGraph(graph);
        graph.getNodes().add(node1);
        graph.getNodes().add(node2);
        graph.getNodes().add(node3);

        // Build the immutable graph: A -> B -> C
        var result1 = graph.getImmutableGraph().addNode("Node A");
        graph.setImmutableGraph(result1.getGraph());
        var result2 = graph.getImmutableGraph().addNode("Node B");
        graph.setImmutableGraph(result2.getGraph());
        var result3 = graph.getImmutableGraph().addNode("Node C");
        graph.setImmutableGraph(result3.getGraph());
        graph.setImmutableGraph(graph.getImmutableGraph().addEdge(0, 1, "edge"));
        graph.setImmutableGraph(graph.getImmutableGraph().addEdge(1, 2, "edge"));

        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));

        mockMvc.perform(get("/graphs/1/dfs/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenDFSOnNonExistentGraph() throws Exception {
        when(graphRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/graphs/999/dfs/0"))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/bfs/{nodeId} - breadth-first search
    @Test
    void shouldPerformBreadthFirstSearch() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.setId(1);
        GraphNode node1 = new GraphNode("Node A", 0);
        node1.setId(1);
        GraphNode node2 = new GraphNode("Node B", 1);
        node2.setId(2);
        GraphNode node3 = new GraphNode("Node C", 2);
        node3.setId(3);
        node1.setGraph(graph);
        node2.setGraph(graph);
        node3.setGraph(graph);
        graph.getNodes().add(node1);
        graph.getNodes().add(node2);
        graph.getNodes().add(node3);

        // Build the immutable graph: A -> B -> C
        var result1 = graph.getImmutableGraph().addNode("Node A");
        graph.setImmutableGraph(result1.getGraph());
        var result2 = graph.getImmutableGraph().addNode("Node B");
        graph.setImmutableGraph(result2.getGraph());
        var result3 = graph.getImmutableGraph().addNode("Node C");
        graph.setImmutableGraph(result3.getGraph());
        graph.setImmutableGraph(graph.getImmutableGraph().addEdge(0, 1, "edge"));
        graph.setImmutableGraph(graph.getImmutableGraph().addEdge(1, 2, "edge"));

        when(graphRepository.findById(1)).thenReturn(Optional.of(graph));

        mockMvc.perform(get("/graphs/1/bfs/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenBFSOnNonExistentGraph() throws Exception {
        when(graphRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/graphs/999/bfs/0"))
                .andExpect(status().isNotFound());
    }
}
