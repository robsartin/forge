package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import com.robsartin.graphs.models.ImmutableGraphEntity;
import com.robsartin.graphs.models.ImmutableGraphNodeEntity;
import com.robsartin.graphs.models.ImmutableGraphEdgeEntity;
import com.robsartin.graphs.ports.out.ImmutableGraphRepository;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestOpenFeatureConfiguration.class)
class ImmutableGraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImmutableGraphRepository immutableGraphRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        immutableGraphRepository.deleteAll();
    }

    // POST /immutable-graphs - save entire immutable graph
    @Test
    void shouldSaveEntireImmutableGraph() throws Exception {
        String requestBody = """
            {
                "name": "Test Graph",
                "nodes": [
                    {"graphNodeId": 0, "label": "Node A"},
                    {"graphNodeId": 1, "label": "Node B"},
                    {"graphNodeId": 2, "label": "Node C"}
                ],
                "edges": [
                    {"fromId": 0, "toId": 1},
                    {"fromId": 1, "toId": 2}
                ]
            }
            """;

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.graphId").exists())
                .andExpect(jsonPath("$.name").value("Test Graph"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(3))
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.edges.length()").value(2));
    }

    @Test
    void shouldSaveEmptyGraph() throws Exception {
        String requestBody = """
            {
                "name": "Empty Graph",
                "nodes": [],
                "edges": []
            }
            """;

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.graphId").exists())
                .andExpect(jsonPath("$.name").value("Empty Graph"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(0))
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.edges.length()").value(0));
    }

    @Test
    void shouldSaveGraphWithNodesButNoEdges() throws Exception {
        String requestBody = """
            {
                "name": "Disconnected Graph",
                "nodes": [
                    {"graphNodeId": 0, "label": "Node A"},
                    {"graphNodeId": 1, "label": "Node B"}
                ],
                "edges": []
            }
            """;

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.graphId").exists())
                .andExpect(jsonPath("$.name").value("Disconnected Graph"))
                .andExpect(jsonPath("$.nodes.length()").value(2))
                .andExpect(jsonPath("$.edges.length()").value(0));
    }

    @Test
    void shouldRejectGraphWithoutName() throws Exception {
        String requestBody = """
            {
                "nodes": [],
                "edges": []
            }
            """;

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectGraphWithEmptyName() throws Exception {
        String requestBody = """
            {
                "name": "",
                "nodes": [],
                "edges": []
            }
            """;

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectGraphWithBlankName() throws Exception {
        String requestBody = """
            {
                "name": "   ",
                "nodes": [],
                "edges": []
            }
            """;

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPersistGraphWhenSaving() throws Exception {
        String requestBody = """
            {
                "name": "Test Graph",
                "nodes": [
                    {"graphNodeId": 0, "label": "Node A"}
                ],
                "edges": []
            }
            """;

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        List<ImmutableGraphEntity> graphs = immutableGraphRepository.findAll();
        assert graphs.size() == 1;
        assert graphs.get(0).getName().equals("Test Graph");
        assert graphs.get(0).getNodes().size() == 1;
    }

    // GET /immutable-graphs/{graphId} - get entire immutable graph
    @Test
    void shouldReturnEntireImmutableGraph() throws Exception {
        // First save a graph
        String requestBody = """
            {
                "name": "Test Graph",
                "nodes": [
                    {"graphNodeId": 0, "label": "Node A"},
                    {"graphNodeId": 1, "label": "Node B"},
                    {"graphNodeId": 2, "label": "Node C"}
                ],
                "edges": [
                    {"fromId": 0, "toId": 1},
                    {"fromId": 1, "toId": 2}
                ]
            }
            """;

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer graphId = objectMapper.readTree(response).get("graphId").asInt();

        // Clear persistence context to simulate separate HTTP request
        entityManager.flush();
        entityManager.clear();

        // Now retrieve the graph
        mockMvc.perform(get("/immutable-graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.graphId").value(graphId))
                .andExpect(jsonPath("$.name").value("Test Graph"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(3))
                .andExpect(jsonPath("$.nodes[0].graphNodeId").value(0))
                .andExpect(jsonPath("$.nodes[0].label").value("Node A"))
                .andExpect(jsonPath("$.nodes[1].graphNodeId").value(1))
                .andExpect(jsonPath("$.nodes[1].label").value("Node B"))
                .andExpect(jsonPath("$.nodes[2].graphNodeId").value(2))
                .andExpect(jsonPath("$.nodes[2].label").value("Node C"))
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.edges.length()").value(2));
    }

    @Test
    void shouldReturn404WhenGraphNotFound() throws Exception {
        mockMvc.perform(get("/immutable-graphs/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyGraphWhenNoNodesOrEdges() throws Exception {
        // First save an empty graph
        String requestBody = """
            {
                "name": "Empty Graph",
                "nodes": [],
                "edges": []
            }
            """;

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer graphId = objectMapper.readTree(response).get("graphId").asInt();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/immutable-graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.graphId").value(graphId))
                .andExpect(jsonPath("$.name").value("Empty Graph"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(0))
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.edges.length()").value(0));
    }

    // GET /immutable-graphs - list all immutable graphs
    @Test
    void shouldReturnAllImmutableGraphs() throws Exception {
        String requestBody1 = """
            {
                "name": "Graph 1",
                "nodes": [{"graphNodeId": 0, "label": "Node A"}],
                "edges": []
            }
            """;

        String requestBody2 = """
            {
                "name": "Graph 2",
                "nodes": [{"graphNodeId": 0, "label": "Node B"}],
                "edges": []
            }
            """;

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody2))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/immutable-graphs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Graph 1"))
                .andExpect(jsonPath("$[1].name").value("Graph 2"));
    }

    @Test
    void shouldReturnEmptyListWhenNoGraphs() throws Exception {
        mockMvc.perform(get("/immutable-graphs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // DELETE /immutable-graphs/{graphId} - delete immutable graph
    @Test
    void shouldDeleteImmutableGraph() throws Exception {
        String requestBody = """
            {
                "name": "Test Graph",
                "nodes": [{"graphNodeId": 0, "label": "Node A"}],
                "edges": []
            }
            """;

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer graphId = objectMapper.readTree(response).get("graphId").asInt();

        mockMvc.perform(delete("/immutable-graphs/" + graphId))
                .andExpect(status().isNoContent());

        assert immutableGraphRepository.findById(graphId).isEmpty();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentGraph() throws Exception {
        mockMvc.perform(delete("/immutable-graphs/999"))
                .andExpect(status().isNotFound());
    }

    // Test edge relationships
    @Test
    void shouldPreserveEdgeRelationships() throws Exception {
        String requestBody = """
            {
                "name": "Complex Graph",
                "nodes": [
                    {"graphNodeId": 0, "label": "A"},
                    {"graphNodeId": 1, "label": "B"},
                    {"graphNodeId": 2, "label": "C"},
                    {"graphNodeId": 3, "label": "D"}
                ],
                "edges": [
                    {"fromId": 0, "toId": 1},
                    {"fromId": 0, "toId": 2},
                    {"fromId": 1, "toId": 3},
                    {"fromId": 2, "toId": 3}
                ]
            }
            """;

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer graphId = objectMapper.readTree(response).get("graphId").asInt();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/immutable-graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes.length()").value(4))
                .andExpect(jsonPath("$.edges.length()").value(4));
    }

    // Test cyclic graph
    @Test
    void shouldSaveCyclicGraph() throws Exception {
        String requestBody = """
            {
                "name": "Cyclic Graph",
                "nodes": [
                    {"graphNodeId": 0, "label": "A"},
                    {"graphNodeId": 1, "label": "B"},
                    {"graphNodeId": 2, "label": "C"}
                ],
                "edges": [
                    {"fromId": 0, "toId": 1},
                    {"fromId": 1, "toId": 2},
                    {"fromId": 2, "toId": 0}
                ]
            }
            """;

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer graphId = objectMapper.readTree(response).get("graphId").asInt();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/immutable-graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.edges.length()").value(3));
    }
}
