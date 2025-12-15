package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
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
import org.springframework.transaction.annotation.Transactional;

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
        GraphNode node1 = new GraphNode("Node A");
        node1.setGraph(graph);
        GraphNode node2 = new GraphNode("Node B");
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
        int graphId = objectMapper.readTree(graphResponse).get("id").asInt();

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

        int graphId = objectMapper.readTree(graphResponse).get("id").asInt();

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

        int graphId = objectMapper.readTree(graphResponse).get("id").asInt();

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
        graph.addNode("Node A");
        Graph savedGraph = graphRepository.save(graph);
        savedGraph.syncImmutableGraph();
        Integer nodeId = savedGraph.getNodes().get(0).getId();

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes/" + nodeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.id").value(nodeId))
                .andExpect(jsonPath("$.node.name").value("Node A"))
                .andExpect(jsonPath("$.toNodes").isArray())
                .andExpect(jsonPath("$.toNodes.length()").value(0));
    }

    @Test
    void shouldReturnNodeWithLinkedNodes() throws Exception {
        Graph graph = new Graph("Test Graph");
        graph.addNode("Node A");
        graph.addNode("Node B");
        graph.addNode("Node C");
        Graph savedGraph = graphRepository.save(graph);

        GraphNode nodeA = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node A")).findFirst().orElseThrow();
        GraphNode nodeB = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node B")).findFirst().orElseThrow();
        GraphNode nodeC = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node C")).findFirst().orElseThrow();

        // Build the immutable graph: A -> B and A -> C using database IDs
        savedGraph.addEdge(nodeA.getId(), nodeB.getId());
        savedGraph.addEdge(nodeA.getId(), nodeC.getId());
        graphRepository.save(savedGraph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/nodes/" + nodeA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.id").value(nodeA.getId()))
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
        graph.addNode("Node A");
        graph.addNode("Node B");
        Graph savedGraph = graphRepository.save(graph);

        GraphNode nodeA = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node A")).findFirst().orElseThrow();
        GraphNode nodeB = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node B")).findFirst().orElseThrow();

        mockMvc.perform(post("/graphs/" + savedGraph.getId() + "/nodes/" + nodeA.getId() + "/" + nodeB.getId()))
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
        graph.addNode("Node A");
        graph.addNode("Node B");
        graph.addNode("Node C");
        Graph savedGraph = graphRepository.save(graph);

        GraphNode nodeA = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node A")).findFirst().orElseThrow();
        GraphNode nodeB = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node B")).findFirst().orElseThrow();
        GraphNode nodeC = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node C")).findFirst().orElseThrow();

        // Build the immutable graph: A -> B -> C using database IDs
        savedGraph.addEdge(nodeA.getId(), nodeB.getId());
        savedGraph.addEdge(nodeB.getId(), nodeC.getId());
        graphRepository.save(savedGraph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/dfs/" + nodeA.getId()))
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
        graph.addNode("Node A");
        graph.addNode("Node B");
        graph.addNode("Node C");
        Graph savedGraph = graphRepository.save(graph);

        GraphNode nodeA = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node A")).findFirst().orElseThrow();
        GraphNode nodeB = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node B")).findFirst().orElseThrow();
        GraphNode nodeC = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node C")).findFirst().orElseThrow();

        // Build the immutable graph: A -> B -> C using database IDs
        savedGraph.addEdge(nodeA.getId(), nodeB.getId());
        savedGraph.addEdge(nodeB.getId(), nodeC.getId());
        graphRepository.save(savedGraph);

        mockMvc.perform(get("/graphs/" + savedGraph.getId() + "/bfs/" + nodeA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldReturn404WhenBFSOnNonExistentGraph() throws Exception {
        mockMvc.perform(get("/graphs/999/bfs/0"))
                .andExpect(status().isNotFound());
    }

    // Tests for unified node ID (database ID = graph node ID)

    @Test
    void shouldAddEdgeUsingDatabaseNodeIds() throws Exception {
        // Create graph via REST
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int graphId = objectMapper.readTree(graphResponse).get("id").asInt();

        // Add first node and get its database ID
        String node1Response = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int nodeADbId = objectMapper.readTree(node1Response).get("id").asInt();

        // Add second node and get its database ID
        String node2Response = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int nodeBDbId = objectMapper.readTree(node2Response).get("id").asInt();

        // Add edge using database IDs (not sequential 0, 1)
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeADbId + "/" + nodeBDbId))
                .andExpect(status().isOk());

        // Verify the edge was created by getting node A with links
        mockMvc.perform(get("/graphs/" + graphId + "/nodes/" + nodeADbId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.id").value(nodeADbId))
                .andExpect(jsonPath("$.toNodes.length()").value(1))
                .andExpect(jsonPath("$.toNodes[0].id").value(nodeBDbId));
    }

    @Test
    void shouldPerformDFSUsingDatabaseNodeId() throws Exception {
        // Create graph via REST
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int graphId = objectMapper.readTree(graphResponse).get("id").asInt();

        // Add nodes
        String node1Response = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int nodeADbId = objectMapper.readTree(node1Response).get("id").asInt();

        String node2Response = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int nodeBDbId = objectMapper.readTree(node2Response).get("id").asInt();

        String node3Response = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node C\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int nodeCDbId = objectMapper.readTree(node3Response).get("id").asInt();

        // Add edges: A -> B -> C
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeADbId + "/" + nodeBDbId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeBDbId + "/" + nodeCDbId))
                .andExpect(status().isOk());

        // Perform DFS starting from Node A using its database ID
        mockMvc.perform(get("/graphs/" + graphId + "/dfs/" + nodeADbId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Node A"))
                .andExpect(jsonPath("$[1].name").value("Node B"))
                .andExpect(jsonPath("$[2].name").value("Node C"));
    }

    @Test
    void shouldPerformBFSUsingDatabaseNodeId() throws Exception {
        // Create graph via REST
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int graphId = objectMapper.readTree(graphResponse).get("id").asInt();

        // Add nodes
        String node1Response = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int nodeADbId = objectMapper.readTree(node1Response).get("id").asInt();

        String node2Response = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int nodeBDbId = objectMapper.readTree(node2Response).get("id").asInt();

        // Add edges: A -> B
        mockMvc.perform(post("/graphs/" + graphId + "/nodes/" + nodeADbId + "/" + nodeBDbId))
                .andExpect(status().isOk());

        // Perform BFS starting from Node A using its database ID
        mockMvc.perform(get("/graphs/" + graphId + "/bfs/" + nodeADbId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Node A"))
                .andExpect(jsonPath("$[1].name").value("Node B"));
    }

    @Test
    void shouldReturnNodeInfoWithDatabaseIdInsteadOfGraphNodeId() throws Exception {
        // Create graph via REST
        String graphResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Test Graph\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int graphId = objectMapper.readTree(graphResponse).get("id").asInt();

        // Add a node
        String nodeResponse = mockMvc.perform(post("/graphs/" + graphId + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int nodeDbId = objectMapper.readTree(nodeResponse).get("id").asInt();

        // Get node by ID - the response should use 'id' field (database ID), not 'graphNodeId'
        mockMvc.perform(get("/graphs/" + graphId + "/nodes/" + nodeDbId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.node.id").value(nodeDbId))
                .andExpect(jsonPath("$.node.name").value("Node A"));
    }
}
