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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestOpenFeatureConfiguration.class)
@WithMockUser
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
                .andExpect(jsonPath("$.id").value(savedGraph.getId().toString()))
                .andExpect(jsonPath("$.name").value("Test Graph"));
    }

    @Test
    void shouldReturn404WhenGraphNotFound() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid))
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
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(delete("/graphs/" + randomUuid))
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

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Node A"))
                .andExpect(jsonPath("$[1].name").value("Node B"));
    }

    @Test
    void shouldReturn404WhenListingNodesForNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/nodes"))
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
        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

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

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

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

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

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
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(post("/graphs/" + randomUuid + "/nodes")
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
        // Create graph and node via REST endpoints to ensure proper setup
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        String nodeResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeId = objectMapper.readTree(nodeResponse).get("id").asText();

        mockMvc.perform(get("/graphs/" + graphId + "/nodes/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.id").value(nodeId))
                .andExpect(jsonPath("$.node.name").value("Node A"))
                .andExpect(jsonPath("$.toNodes").isArray())
                .andExpect(jsonPath("$.toNodes.length()").value(0));
    }

    @Test
    void shouldReturnNodeWithLinkedNodes() throws Exception {
        // Create graph
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Create nodes
        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeCResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node C\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();
        String nodeCId = objectMapper.readTree(nodeCResponse).get("id").asText();

        // Add edges: A -> B and A -> C
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeCId))
                .andExpect(status().isOk());

        // Get node A with its links
        mockMvc.perform(get("/graphs/" + graphId + "/nodes/" + nodeAId))
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
        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes/" + randomUuid))
                .andExpect(status().isNotFound());
    }

    // POST /graphs/{id}/nodes/{fromId}/{toId} - add an edge
    @Test
    void shouldAddEdgeBetweenNodes() throws Exception {
        // Create graph and nodes via REST
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenAddingEdgeToNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        UUID nodeAId = UuidV7Generator.generate();
        UUID nodeBId = UuidV7Generator.generate();
        mockMvc.perform(post("/graphs/" + randomUuid + "/nodes/" + nodeAId + "/" + nodeBId))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/dfs/{nodeId} - depth-first search
    @Test
    void shouldPerformDepthFirstSearch() throws Exception {
        // Create graph
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Create nodes
        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeCResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node C\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();
        String nodeCId = objectMapper.readTree(nodeCResponse).get("id").asText();

        // Add edges: A -> B -> C
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeBId + "/" + nodeCId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/graphs/" + graphId + "/dfs/" + nodeAId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenDFSOnNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        UUID nodeId = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/dfs/" + nodeId))
                .andExpect(status().isNotFound());
    }

    // GET /graphs/{id}/bfs/{nodeId} - breadth-first search
    @Test
    void shouldPerformBreadthFirstSearch() throws Exception {
        // Create graph
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(graphResponse).get("id").asText();

        // Create nodes
        String nodeAResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeBResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeCResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node C\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String nodeAId = objectMapper.readTree(nodeAResponse).get("id").asText();
        String nodeBId = objectMapper.readTree(nodeBResponse).get("id").asText();
        String nodeCId = objectMapper.readTree(nodeCResponse).get("id").asText();

        // Add edges: A -> B -> C
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeAId + "/" + nodeBId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeBId + "/" + nodeCId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/graphs/" + graphId + "/bfs/" + nodeAId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenBFSOnNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        UUID nodeId = UuidV7Generator.generate();
        mockMvc.perform(get("/graphs/" + randomUuid + "/bfs/" + nodeId))
                .andExpect(status().isNotFound());
    }
}
