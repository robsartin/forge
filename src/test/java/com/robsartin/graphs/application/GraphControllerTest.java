package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import com.robsartin.graphs.infrastructure.UuidV7Generator;
import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.models.GraphNode;
import com.robsartin.graphs.ports.out.GraphRepository;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestOpenFeatureConfiguration.class)
class GraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GraphRepository graphRepository;

    @Autowired
    private EntityManager entityManager;

    private RequestPostProcessor authenticatedUser;

    @BeforeEach
    void setUp() {
        graphRepository.deleteAll();
        authenticatedUser = user("testuser").roles("USER");
    }

    // GET /graphs - list all graphs
    @Test
    void shouldReturnAllGraphs() throws Exception {
        Graph graph1 = new Graph("Graph 1");
        Graph graph2 = new Graph("Graph 2");
        graphRepository.save(graph1);
        graphRepository.save(graph2);

        mockMvc.perform(get("/graphs").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Graph 1"))
                .andExpect(jsonPath("$[1].name").value("Graph 2"));
    }

    @Test
    void shouldReturnEmptyListWhenNoGraphs() throws Exception {
        mockMvc.perform(get("/graphs").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /graphs/{id} - get graph by id
    @Test
    void shouldReturnGraphById() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId()).with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedGraph.getId().toString()))
                .andExpect(jsonPath("$.name").value("Test Graph"));
    }

    @Test
    void shouldReturn404WhenGraphNotFound() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid).with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    // POST /graphs - create a new graph
    @Test
    void shouldCreateNewGraph() throws Exception {
        mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Graph\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Graph"));
    }

    @Test
    void shouldRejectPostWithoutName() throws Exception {
        mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPostWithEmptyName() throws Exception {
        mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPostWithBlankName() throws Exception {
        mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPostWithNullName() throws Exception {
        mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPersistGraphWhenCreating() throws Exception {
        mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
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

        mockMvc.perform(delete("/graphs/" + savedGraph.getId()).with(authenticatedUser).with(csrf()))
                .andExpect(status().isNoContent());

        assert graphRepository.findById(savedGraph.getId()).isEmpty();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(delete("/graphs/" + randomUuid).with(authenticatedUser).with(csrf()))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/nodes - list all nodes in a graph
    @Test
    void shouldReturnAllNodesInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        UUID nodeAId = UuidV7Generator.generate();
        UUID nodeBId = UuidV7Generator.generate();
        GraphNode node1 = new GraphNode("Node A", nodeAId);
        node1.setGraph(graph);
        GraphNode node2 = new GraphNode("Node B", nodeBId);
        node2.setGraph(graph);
        graph.getNodes().add(node1);
        graph.getNodes().add(node2);
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Node A"))
                .andExpect(jsonPath("$[1].name").value("Node B"));
    }

    @Test
    void shouldReturn404WhenListingNodesForNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/nodes").with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    // POST /graphs/{id}/nodes - create a new node in a graph
    @Test
    void shouldCreateNewNodeInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(post("/graphs/" + savedGraph.getId() + "/nodes").with(authenticatedUser).with(csrf())
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
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract graph ID from response
        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Add node via POST /graphs/{id} (the endpoint reported as failing)
        mockMvc.perform(post("/graphs/" + graphId).with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"node 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("node 1"));
    }

    @Test
    void shouldCreateMultipleNodesViaRestEndpoints() throws Exception {
        // Create graph via REST
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Add first node
        mockMvc.perform(post("/graphs/" + graphId).with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"node 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("node 1"));

        // Add second node
        mockMvc.perform(post("/graphs/" + graphId).with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"node 2\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("node 2"));

        // Verify both nodes exist
        mockMvc.perform(get("/graphs/" + graphId + "/nodes").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldCreateNodeWithPersistenceContextClearing() throws Exception {
        // This test simulates separate HTTP requests by clearing the persistence context
        // This ensures @PostLoad is triggered when the graph is reloaded

        // Create graph via REST
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Flush and clear to simulate end of first HTTP request/transaction
        entityManager.flush();
        entityManager.clear();

        // Add node via POST /graphs/{id} - this should trigger @PostLoad when loading the graph
        mockMvc.perform(post("/graphs/" + graphId).with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"node 1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("node 1"));

        // Verify node was persisted
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/graphs/" + graphId + "/nodes").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("node 1"));
    }

    @Test
    void shouldReturn404WhenCreatingNodeInNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(post("/graphs/" + randomUuid + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"New Node\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectNodeCreationWithoutName() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(post("/graphs/" + savedGraph.getId() + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // GET /graphs/{id}/nodes/{nodeId} - get a specific node with links
    @Test
    void shouldReturnNodeByIdWithLinks() throws Exception {
        // Create graph and node via REST endpoints to ensure proper setup
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        String nodeResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeId = objectMapper.readTree(nodeResponse).get("id").asText();

        mockMvc.perform(get("/graphs/" + graphId + "/nodes/" + nodeId).with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.id").value(nodeId))
                .andExpect(jsonPath("$.node.name").value("Node A"))
                .andExpect(jsonPath("$.toNodes").isArray())
                .andExpect(jsonPath("$.toNodes.length()").value(0));
    }

    @Test
    void shouldReturnNodeWithLinkedNodes() throws Exception {
        // Create graph
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Create nodes
        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeCResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node C\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();
        String nodeCId = objectMapper.readTree(nodeCResponse).get("id").asText();

        // Add edges: A -> B and A -> C
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeCId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());

        // Get node A with its links
        mockMvc.perform(get("/graphs/" + graphId + "/nodes/" + nodeAId).with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.id").value(nodeAId))
                .andExpect(jsonPath("$.node.name").value("Node A"))
                .andExpect(jsonPath("$.toNodes").isArray())
                .andExpect(jsonPath("$.toNodes.length()").value(2));
    }

    @Test
    void shouldReturn404WhenNodeNotFoundInGraph() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes/" + randomUuid).with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    // POST /graphs/{id}/nodes/{fromId}/{toId} - add an edge
    @Test
    void shouldAddEdgeBetweenNodes() throws Exception {
        // Create graph and nodes via REST
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenAddingEdgeToNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        UUID nodeAId = UuidV7Generator.generate();
        UUID nodeBId = UuidV7Generator.generate();
        mockMvc.perform(post("/graphs/" + randomUuid + "/nodes/" + nodeAId + "/" + nodeBId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/dfs/{nodeId} - depth-first search
    @Test
    void shouldPerformDepthFirstSearch() throws Exception {
        // Create graph
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Create nodes
        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeCResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node C\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();
        String nodeCId = objectMapper.readTree(nodeCResponse).get("id").asText();

        // Add edges: A -> B -> C
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeBId + "/" + nodeCId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/graphs/" + graphId + "/dfs/" + nodeAId).with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenDFSOnNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        UUID nodeId = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/dfs/" + nodeId).with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/bfs/{nodeId} - breadth-first search
    @Test
    void shouldPerformBreadthFirstSearch() throws Exception {
        // Create graph
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Create nodes
        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeCResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node C\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();
        String nodeCId = objectMapper.readTree(nodeCResponse).get("id").asText();

        // Add edges: A -> B -> C
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeBId + "/" + nodeCId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/graphs/" + graphId + "/bfs/" + nodeAId).with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenBFSOnNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        UUID nodeId = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/bfs/" + nodeId).with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/full - get full graph with nodes and edges
    @Test
    void shouldReturnFullGraphWithNodesAndEdges() throws Exception {
        // Create graph
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Create nodes
        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();

        // Add edge: A -> B
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());

        // Get full graph
        mockMvc.perform(get("/graphs/" + graphId + "/full").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(graphId))
                .andExpect(jsonPath("$.name").value("Test Graph"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(2))
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.edges.length()").value(1))
                .andExpect(jsonPath("$.edges[0].source").value(nodeAId))
                .andExpect(jsonPath("$.edges[0].target").value(nodeBId));
    }

    @Test
    void shouldReturn404WhenGettingFullNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/full").with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyEdgesForGraphWithoutEdges() throws Exception {
        // Create graph with nodes but no edges
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"No Edges Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/graphs/" + graphId + "/full").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes.length()").value(1))
                .andExpect(jsonPath("$.edges.length()").value(0));
    }

    // GET /graphs/page - paginated graphs
    @Test
    void shouldReturnPaginatedGraphs() throws Exception {
        // Create multiple graphs
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"Graph " + i + "\"}"))
                    .andExpect(status().isCreated());
        }

        // Get first page with size 2
        mockMvc.perform(get("/graphs/page").with(authenticatedUser)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    void shouldReturnSecondPageOfGraphs() throws Exception {
        // Create multiple graphs
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"Test Graph " + i + "\"}"))
                    .andExpect(status().isCreated());
        }

        // Get second page
        mockMvc.perform(get("/graphs/page").with(authenticatedUser)
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    @Test
    void shouldLimitPageSizeTo100() throws Exception {
        mockMvc.perform(get("/graphs/page").with(authenticatedUser)
                        .param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    // GET /graphs/{id}/nodes/page - paginated nodes
    @Test
    void shouldReturnPaginatedNodes() throws Exception {
        // Create graph
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Paginated Nodes Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Create multiple nodes
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"Node " + i + "\"}"))
                    .andExpect(status().isCreated());
        }

        // Get first page with size 2
        mockMvc.perform(get("/graphs/" + graphId + "/nodes/page").with(authenticatedUser)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    void shouldReturn404ForPaginatedNodesOnNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/nodes/page").with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/export - export graph
    @Test
    void shouldExportGraph() throws Exception {
        // Create graph with nodes and edge
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Export Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId).with(authenticatedUser).with(csrf()))
                .andExpect(status().isOk());

        // Export
        mockMvc.perform(get("/graphs/" + graphId + "/export").with(authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.exportedAt").exists())
                .andExpect(jsonPath("$.graph.name").value("Export Test Graph"))
                .andExpect(jsonPath("$.graph.nodes.length()").value(2))
                .andExpect(jsonPath("$.graph.edges.length()").value(1));
    }

    @Test
    void shouldReturn404WhenExportingNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/export").with(authenticatedUser))
                .andExpect(status().isNotFound());
    }

    // POST /graphs/import - import graph
    @Test
    void shouldImportGraph() throws Exception {
        String importJson = """
            {
                "version": "1.0",
                "graph": {
                    "name": "Imported Graph",
                    "nodes": [
                        {"id": "old-id-1", "name": "Node X"},
                        {"id": "old-id-2", "name": "Node Y"}
                    ],
                    "edges": [
                        {"from": "old-id-1", "to": "old-id-2"}
                    ]
                }
            }
            """;

        mockMvc.perform(post("/graphs/import").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Imported Graph"))
                .andExpect(jsonPath("$.nodes.length()").value(2))
                .andExpect(jsonPath("$.edges.length()").value(1));
    }

    @Test
    void shouldImportAndExportRoundTrip() throws Exception {
        // Create and export a graph
        String graphResponse = mockMvc.perform(post("/graphs").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"RoundTrip Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        mockMvc.perform(post("/graphs/" + graphId + "/nodes").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node 1\"}"))
                .andExpect(status().isCreated());

        String exportResponse = mockMvc.perform(get("/graphs/" + graphId + "/export").with(authenticatedUser))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Import the exported graph
        mockMvc.perform(post("/graphs/import").with(authenticatedUser).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exportResponse))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("RoundTrip Graph"))
                .andExpect(jsonPath("$.nodes.length()").value(1));
    }
}
