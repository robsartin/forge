package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.models.GraphNode;
import com.robsartin.graphs.ports.out.GraphRepository;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GraphRepository graphRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        graphRepository.deleteAll();
    }

    // GET /graphs - list all graphs
    @Test
    void shouldReturnAllGraphs() throws Exception {
        Graph graph1 = new Graph("Graph 1");
        Graph graph2 = new Graph("Graph 2");
        graphRepository.save(graph1);
        graphRepository.save(graph2);

        mockMvc.perform(get("/graphs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Graph 1"))
                .andExpect(jsonPath("$[1].name").value("Graph 2"));
    }

    @Test
    void shouldReturnEmptyListWhenNoGraphs() throws Exception {
        mockMvc.perform(get("/graphs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /graphs/{id} - get graph by id
    @Test
    void shouldReturnGraphById() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedGraph.getId()))
                .andExpect(jsonPath("$.name").value("Test Graph"));
    }

    @Test
    void shouldReturn404WhenGraphNotFound() throws Exception {
        mockMvc.perform(get("/graphs/999"))
                .andExpect(status().isNotFound());
    }

    // POST /graphs - create a new graph
    @Test
    void shouldCreateNewGraph() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Graph\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
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
    void shouldPersistGraphWhenCreating() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated());

        assert graphRepository.findAll().size() == 1;
        assert graphRepository.findAll().get(0).getName().equals("Test Graph");
    }

    // DELETE /graphs/{id} - delete graph by id
    @Test
    void shouldDeleteGraphById() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(delete("/graphs/" + savedGraph.getId()))
                .andExpect(status().isNoContent());

        assert graphRepository.findById(savedGraph.getId()).isEmpty();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentGraph() throws Exception {
        mockMvc.perform(delete("/graphs/999"))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/nodes - list all nodes in a graph
    @Test
    void shouldReturnAllNodesInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        GraphNode node1 = new GraphNode("Node A", 0);
        node1.setGraph(graph);
        GraphNode node2 = new GraphNode("Node B", 1);
        node2.setGraph(graph);
        graph.getNodes().add(node1);
        graph.getNodes().add(node2);
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Node A"))
                .andExpect(jsonPath("$[1].name").value("Node B"));
    }

    @Test
    void shouldReturn404WhenListingNodesForNonExistentGraph() throws Exception {
        mockMvc.perform(get("/graphs/999/nodes"))
                .andExpect(status().isNotFound());
    }

    // POST /graphs/{id}/nodes - create a new node in a graph
    @Test
    void shouldCreateNewNodeInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(post("/graphs/" + savedGraph.getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Node\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Node"));
    }

    // POST /graphs/{id} - create a new node in a graph (alternate endpoint)
    @Test
    void shouldCreateNodeViaGraphIdEndpoint() throws Exception {
        // Create graph via REST endpoint (mimics user's actual flow)
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract graph ID from response
        Integer graphId = objectMapper.readTree(graphResponse).get("id").asInt();

        // Add node via POST /graphs/{id} (the endpoint reported as failing)
        mockMvc.perform(post("/graphs/" + graphId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"node 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("node 1"));
    }

    @Test
    void shouldCreateMultipleNodesViaRestEndpoints() throws Exception {
        // Create graph via REST
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer graphId = objectMapper.readTree(graphResponse).get("id").asInt();

        // Add first node
        mockMvc.perform(post("/graphs/" + graphId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"node 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("node 1"));

        // Add second node
        mockMvc.perform(post("/graphs/" + graphId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"node 2\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("node 2"));

        // Verify both nodes exist
        mockMvc.perform(get("/graphs/" + graphId + "/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldCreateNodeWithPersistenceContextClearing() throws Exception {
        // This test simulates separate HTTP requests by clearing the persistence context
        // This ensures @PostLoad is triggered when the graph is reloaded

        // Create graph via REST
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer graphId = objectMapper.readTree(graphResponse).get("id").asInt();

        // Flush and clear to simulate end of first HTTP request/transaction
        entityManager.flush();
        entityManager.clear();

        // Add node via POST /graphs/{id} - this should trigger @PostLoad when loading the graph
        mockMvc.perform(post("/graphs/" + graphId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"node 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("node 1"));

        // Verify node was persisted
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/graphs/" + graphId + "/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("node 1"));
    }

    @Test
    void shouldReturn404WhenCreatingNodeInNonExistentGraph() throws Exception {
        mockMvc.perform(post("/graphs/999/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Node\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectNodeCreationWithoutName() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(post("/graphs/" + savedGraph.getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // GET /graphs/{id}/nodes/{nodeId} - get a specific node with links
    @Test
    void shouldReturnNodeByIdWithLinks() throws Exception {
        Graph graph = new Graph("Test Graph");
        GraphNode node = new GraphNode("Node A", 0);
        node.setGraph(graph);
        graph.getNodes().add(node);
        graph.setImmutableGraph(graph.getImmutableGraph().addNode("Node A").getGraph());
        Graph savedGraph = graphRepository.save(graph);
        Integer nodeId = savedGraph.getNodes().get(0).getId();

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.graphNodeId").value(0))
                .andExpect(jsonPath("$.node.name").value("Node A"))
                .andExpect(jsonPath("$.toNodes").isArray())
                .andExpect(jsonPath("$.toNodes.length()").value(0));
    }

    @Test
    void shouldReturnNodeWithLinkedNodes() throws Exception {
        Graph graph = new Graph("Test Graph");
        GraphNode node1 = new GraphNode("Node A", 0);
        GraphNode node2 = new GraphNode("Node B", 1);
        GraphNode node3 = new GraphNode("Node C", 2);
        node1.setGraph(graph);
        node2.setGraph(graph);
        node3.setGraph(graph);
        graph.getNodes().add(node1);
        graph.getNodes().add(node2);
        graph.getNodes().add(node3);

        // Build the immutable graph: A -> B and A -> C
        graph.setImmutableGraph(graph.getImmutableGraph().addNode("Node A").getGraph());
        graph.setImmutableGraph(graph.getImmutableGraph().addNode("Node B").getGraph());
        graph.setImmutableGraph(graph.getImmutableGraph().addNode("Node C").getGraph());
        graph.setImmutableGraph(graph.getImmutableGraph().addEdge(0, 1, "edge"));
        graph.setImmutableGraph(graph.getImmutableGraph().addEdge(0, 2, "edge"));
        Graph savedGraph = graphRepository.save(graph);
        Integer nodeId = savedGraph.getNodes().get(0).getId();

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.graphNodeId").value(0))
                .andExpect(jsonPath("$.node.name").value("Node A"))
                .andExpect(jsonPath("$.toNodes").isArray())
                .andExpect(jsonPath("$.toNodes.length()").value(2));
    }

    @Test
    void shouldReturn404WhenNodeNotFoundInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes/999"))
                .andExpect(status().isNotFound());
    }

    // POST /graphs/{id}/nodes/{fromId}/{toId} - add an edge
    @Test
    void shouldAddEdgeBetweenNodes() throws Exception {
        Graph graph = new Graph("Test Graph");
        GraphNode node1 = new GraphNode("Node A", 0);
        node1.setGraph(graph);
        GraphNode node2 = new GraphNode("Node B", 1);
        node2.setGraph(graph);
        graph.getNodes().add(node1);
        graph.getNodes().add(node2);
        graph.setImmutableGraph(graph.getImmutableGraph().addNode("Node A").getGraph());
        graph.setImmutableGraph(graph.getImmutableGraph().addNode("Node B").getGraph());
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(post("/graphs/" + savedGraph.getId() + "/nodes/0/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenAddingEdgeToNonExistentGraph() throws Exception {
        mockMvc.perform(post("/graphs/999/nodes/0/1"))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/dfs/{nodeId} - depth-first search
    @Test
    void shouldPerformDepthFirstSearch() throws Exception {
        Graph graph = new Graph("Test Graph");
        GraphNode node1 = new GraphNode("Node A", 0);
        GraphNode node2 = new GraphNode("Node B", 1);
        GraphNode node3 = new GraphNode("Node C", 2);
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
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/dfs/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenDFSOnNonExistentGraph() throws Exception {
        mockMvc.perform(get("/graphs/999/dfs/0"))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/bfs/{nodeId} - breadth-first search
    @Test
    void shouldPerformBreadthFirstSearch() throws Exception {
        Graph graph = new Graph("Test Graph");
        GraphNode node1 = new GraphNode("Node A", 0);
        GraphNode node2 = new GraphNode("Node B", 1);
        GraphNode node3 = new GraphNode("Node C", 2);
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
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/bfs/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenBFSOnNonExistentGraph() throws Exception {
        mockMvc.perform(get("/graphs/999/bfs/0"))
                .andExpect(status().isNotFound());
    }
}
