package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.domain.models.Graph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GraphController.class)
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GraphService graphService;

    @Test
    void shouldReturnAllGraphs() throws Exception {
        Graph graph1 = createTestGraph(1L, "Graph 1");
        Graph graph2 = createTestGraph(2L, "Graph 2");
        List<Graph> graphs = Arrays.asList(graph1, graph2);

        when(graphService.findAll()).thenReturn(graphs);

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
        when(graphService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/graphs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldCreateNewGraph() throws Exception {
        Graph savedGraph = createTestGraph(1L, "Test Graph");

        when(graphService.createGraph(any(), any(), any(), any())).thenReturn(savedGraph);

        GraphController.CreateGraphRequest request = new GraphController.CreateGraphRequest(
                "Test Graph", "Test description", "String", "String"
        );

        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Graph"))
                .andExpect(jsonPath("$.nodeType").value("String"))
                .andExpect(jsonPath("$.edgeType").value("String"));
    }

    @Test
    void shouldReturnGraphById() throws Exception {
        Graph graph = createTestGraph(1L, "Test Graph");

        when(graphService.findById(1L)).thenReturn(Optional.of(graph));

        mockMvc.perform(get("/graphs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Graph"));
    }

    @Test
    void shouldReturn404WhenGraphNotFound() throws Exception {
        when(graphService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/graphs/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectCreateGraphWithoutName() throws Exception {
        GraphController.CreateGraphRequest request = new GraphController.CreateGraphRequest(
                "", "Description", "String", "String"
        );

        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateGraphWithoutNodeType() throws Exception {
        GraphController.CreateGraphRequest request = new GraphController.CreateGraphRequest(
                "Test Graph", "Description", "", "String"
        );

        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAddNodeToGraph() throws Exception {
        Graph newVersion = createTestGraph(2L, "Test Graph");
        newVersion.setVersion(2);
        newVersion.setParentId(1L);

        Map<String, Object> structure = new HashMap<>();
        structure.put("id", 2L);
        structure.put("name", "Test Graph");
        structure.put("version", 2);
        structure.put("nodeCount", 1);
        structure.put("edgeCount", 0);

        when(graphService.findById(1L)).thenReturn(Optional.of(createTestGraph(1L, "Test Graph")));
        when(graphService.addNode(1L, "Node A")).thenReturn(newVersion);
        when(graphService.getGraphStructure(2L)).thenReturn(structure);

        GraphController.AddNodeRequest request = new GraphController.AddNodeRequest("Node A");

        mockMvc.perform(post("/graphs/1/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.graph.id").value(2))
                .andExpect(jsonPath("$.graph.version").value(2))
                .andExpect(jsonPath("$.structure.nodeCount").value(1));
    }

    @Test
    void shouldReturn404WhenAddingNodeToNonExistentGraph() throws Exception {
        when(graphService.findById(999L)).thenReturn(Optional.empty());

        GraphController.AddNodeRequest request = new GraphController.AddNodeRequest("Node A");

        mockMvc.perform(post("/graphs/999/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddEdgeToGraph() throws Exception {
        Graph newVersion = createTestGraph(3L, "Test Graph");
        newVersion.setVersion(3);
        newVersion.setParentId(2L);

        Map<String, Object> structure = new HashMap<>();
        structure.put("id", 3L);
        structure.put("name", "Test Graph");
        structure.put("version", 3);
        structure.put("nodeCount", 2);
        structure.put("edgeCount", 1);

        when(graphService.findById(2L)).thenReturn(Optional.of(createTestGraph(2L, "Test Graph")));
        when(graphService.addEdge(2L, 0, 1, "Edge A->B")).thenReturn(newVersion);
        when(graphService.getGraphStructure(3L)).thenReturn(structure);

        GraphController.AddEdgeRequest request = new GraphController.AddEdgeRequest(0, 1, "Edge A->B");

        mockMvc.perform(post("/graphs/2/edges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.graph.id").value(3))
                .andExpect(jsonPath("$.graph.version").value(3))
                .andExpect(jsonPath("$.structure.edgeCount").value(1));
    }

    @Test
    void shouldGetGraphStructure() throws Exception {
        Map<String, Object> structure = new HashMap<>();
        structure.put("id", 1L);
        structure.put("name", "Test Graph");
        structure.put("version", 1);
        structure.put("nodeCount", 3);
        structure.put("edgeCount", 2);

        when(graphService.findById(1L)).thenReturn(Optional.of(createTestGraph(1L, "Test Graph")));
        when(graphService.getGraphStructure(1L)).thenReturn(structure);

        mockMvc.perform(get("/graphs/1/structure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nodeCount").value(3))
                .andExpect(jsonPath("$.edgeCount").value(2));
    }

    @Test
    void shouldPerformDepthFirstTraversal() throws Exception {
        List<Integer> traversal = Arrays.asList(0, 1, 2, 3);

        when(graphService.findById(1L)).thenReturn(Optional.of(createTestGraph(1L, "Test Graph")));
        when(graphService.depthFirstTraversal(1L)).thenReturn(traversal);

        mockMvc.perform(get("/graphs/1/traversal/dfs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("depth-first"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(4))
                .andExpect(jsonPath("$.nodes[0]").value(0))
                .andExpect(jsonPath("$.nodes[1]").value(1))
                .andExpect(jsonPath("$.nodes[2]").value(2))
                .andExpect(jsonPath("$.nodes[3]").value(3));
    }

    @Test
    void shouldPerformBreadthFirstTraversal() throws Exception {
        List<Integer> traversal = Arrays.asList(0, 1, 2, 3);

        when(graphService.findById(1L)).thenReturn(Optional.of(createTestGraph(1L, "Test Graph")));
        when(graphService.breadthFirstTraversal(1L)).thenReturn(traversal);

        mockMvc.perform(get("/graphs/1/traversal/bfs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("breadth-first"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(4))
                .andExpect(jsonPath("$.nodes[0]").value(0))
                .andExpect(jsonPath("$.nodes[1]").value(1))
                .andExpect(jsonPath("$.nodes[2]").value(2))
                .andExpect(jsonPath("$.nodes[3]").value(3));
    }

    @Test
    void shouldDeleteGraphById() throws Exception {
        when(graphService.findById(1L)).thenReturn(Optional.of(createTestGraph(1L, "Test Graph")));

        mockMvc.perform(delete("/graphs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentGraph() throws Exception {
        when(graphService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/graphs/999"))
                .andExpect(status().isNotFound());
    }

    private Graph createTestGraph(Long id, String name) {
        Graph graph = new Graph(name, "Test description", "String", "String");
        graph.setId(id);
        graph.setVersion(1);
        graph.setGraphData("{}");
        graph.setCreatedAt(Instant.now());
        return graph;
    }
}
