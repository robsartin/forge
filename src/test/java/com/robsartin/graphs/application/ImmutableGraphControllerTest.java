package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import com.robsartin.graphs.infrastructure.UuidV7Generator;
import com.robsartin.graphs.models.ImmutableGraphEntity;
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
import java.util.UUID;

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
        UUID nodeAId = UuidV7Generator.generate();
        UUID nodeBId = UuidV7Generator.generate();
        UUID nodeCId = UuidV7Generator.generate();

        String requestBody = """
            {
                "name": "Test Graph",
                "nodes": [
                    {"id": "%s", "label": "Node A"},
                    {"id": "%s", "label": "Node B"},
                    {"id": "%s", "label": "Node C"}
                ],
                "edges": [
                    {"fromId": "%s", "toId": "%s"},
                    {"fromId": "%s", "toId": "%s"}
                ]
            }
            """.formatted(nodeAId, nodeBId, nodeCId, nodeAId, nodeBId, nodeBId, nodeCId);

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Empty Graph"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(0))
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.edges.length()").value(0));
    }

    @Test
    void shouldSaveGraphWithNodesButNoEdges() throws Exception {
        UUID nodeAId = UuidV7Generator.generate();
        UUID nodeBId = UuidV7Generator.generate();

        String requestBody = """
            {
                "name": "Disconnected Graph",
                "nodes": [
                    {"id": "%s", "label": "Node A"},
                    {"id": "%s", "label": "Node B"}
                ],
                "edges": []
            }
            """.formatted(nodeAId, nodeBId);

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
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
        UUID nodeAId = UuidV7Generator.generate();

        String requestBody = """
            {
                "name": "Test Graph",
                "nodes": [
                    {"id": "%s", "label": "Node A"}
                ],
                "edges": []
            }
            """.formatted(nodeAId);

        mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        List<ImmutableGraphEntity> graphs = immutableGraphRepository.findAll();
        assert graphs.size() == 1;
        assert graphs.get(0).getName().equals("Test Graph");
        assert graphs.get(0).getNodes().size() == 1;
    }

    // GET /immutable-graphs/{id} - get entire immutable graph
    @Test
    void shouldReturnEntireImmutableGraph() throws Exception {
        UUID nodeAId = UuidV7Generator.generate();
        UUID nodeBId = UuidV7Generator.generate();
        UUID nodeCId = UuidV7Generator.generate();

        // First save a graph
        String requestBody = """
            {
                "name": "Test Graph",
                "nodes": [
                    {"id": "%s", "label": "Node A"},
                    {"id": "%s", "label": "Node B"},
                    {"id": "%s", "label": "Node C"}
                ],
                "edges": [
                    {"fromId": "%s", "toId": "%s"},
                    {"fromId": "%s", "toId": "%s"}
                ]
            }
            """.formatted(nodeAId, nodeBId, nodeCId, nodeAId, nodeBId, nodeBId, nodeCId);

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(response).get("id").asText();

        // Clear persistence context to simulate separate HTTP request
        entityManager.flush();
        entityManager.clear();

        // Now retrieve the graph
        mockMvc.perform(get("/immutable-graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(graphId))
                .andExpect(jsonPath("$.name").value("Test Graph"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(3))
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.edges.length()").value(2));
    }

    @Test
    void shouldReturn404WhenGraphNotFound() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(get("/immutable-graphs/" + randomUuid))
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

        String graphId = objectMapper.readTree(response).get("id").asText();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/immutable-graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(graphId))
                .andExpect(jsonPath("$.name").value("Empty Graph"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes.length()").value(0))
                .andExpect(jsonPath("$.edges").isArray())
                .andExpect(jsonPath("$.edges.length()").value(0));
    }

    // GET /immutable-graphs - list all immutable graphs
    @Test
    void shouldReturnAllImmutableGraphs() throws Exception {
        UUID node1Id = UuidV7Generator.generate();
        UUID node2Id = UuidV7Generator.generate();

        String requestBody1 = """
            {
                "name": "Graph 1",
                "nodes": [{"id": "%s", "label": "Node A"}],
                "edges": []
            }
            """.formatted(node1Id);

        String requestBody2 = """
            {
                "name": "Graph 2",
                "nodes": [{"id": "%s", "label": "Node B"}],
                "edges": []
            }
            """.formatted(node2Id);

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

    // DELETE /immutable-graphs/{id} - delete immutable graph
    @Test
    void shouldDeleteImmutableGraph() throws Exception {
        UUID nodeAId = UuidV7Generator.generate();

        String requestBody = """
            {
                "name": "Test Graph",
                "nodes": [{"id": "%s", "label": "Node A"}],
                "edges": []
            }
            """.formatted(nodeAId);

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/immutable-graphs/" + graphId))
                .andExpect(status().isNoContent());

        assert immutableGraphRepository.findById(UUID.fromString(graphId)).isEmpty();
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentGraph() throws Exception {
        UUID randomUuid = UuidV7Generator.generate();
        mockMvc.perform(delete("/immutable-graphs/" + randomUuid))
                .andExpect(status().isNotFound());
    }

    // Test edge relationships
    @Test
    void shouldPreserveEdgeRelationships() throws Exception {
        UUID nodeAId = UuidV7Generator.generate();
        UUID nodeBId = UuidV7Generator.generate();
        UUID nodeCId = UuidV7Generator.generate();
        UUID nodeDId = UuidV7Generator.generate();

        String requestBody = """
            {
                "name": "Complex Graph",
                "nodes": [
                    {"id": "%s", "label": "A"},
                    {"id": "%s", "label": "B"},
                    {"id": "%s", "label": "C"},
                    {"id": "%s", "label": "D"}
                ],
                "edges": [
                    {"fromId": "%s", "toId": "%s"},
                    {"fromId": "%s", "toId": "%s"},
                    {"fromId": "%s", "toId": "%s"},
                    {"fromId": "%s", "toId": "%s"}
                ]
            }
            """.formatted(nodeAId, nodeBId, nodeCId, nodeDId,
                          nodeAId, nodeBId, nodeAId, nodeCId, nodeBId, nodeDId, nodeCId, nodeDId);

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(response).get("id").asText();

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
        UUID nodeAId = UuidV7Generator.generate();
        UUID nodeBId = UuidV7Generator.generate();
        UUID nodeCId = UuidV7Generator.generate();

        String requestBody = """
            {
                "name": "Cyclic Graph",
                "nodes": [
                    {"id": "%s", "label": "A"},
                    {"id": "%s", "label": "B"},
                    {"id": "%s", "label": "C"}
                ],
                "edges": [
                    {"fromId": "%s", "toId": "%s"},
                    {"fromId": "%s", "toId": "%s"},
                    {"fromId": "%s", "toId": "%s"}
                ]
            }
            """.formatted(nodeAId, nodeBId, nodeCId, nodeAId, nodeBId, nodeBId, nodeCId, nodeCId, nodeAId);

        String response = mockMvc.perform(post("/immutable-graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String graphId = objectMapper.readTree(response).get("id").asText();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/immutable-graphs/" + graphId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.edges.length()").value(3));
    }
}
