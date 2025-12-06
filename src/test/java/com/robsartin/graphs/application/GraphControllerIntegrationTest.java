package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.domain.models.Graph;
import com.robsartin.graphs.domain.ports.out.GraphRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GraphControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GraphRepository graphRepository;

    @BeforeEach
    void setUp() {
        graphRepository.findAll().forEach(graph -> graphRepository.deleteById(graph.getId()));
    }

    @Test
    void shouldCreateAndRetrieveGraph() throws Exception {
        String createResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Graph\",\"description\":\"A test graph\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Graph"))
                .andExpect(jsonPath("$.description").value("A test graph"))
                .andExpect(jsonPath("$.nodeType").value("String"))
                .andExpect(jsonPath("$.edgeType").value("String"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Graph createdGraph = objectMapper.readValue(createResponse, Graph.class);

        mockMvc.perform(get("/graphs/" + createdGraph.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdGraph.getId()))
                .andExpect(jsonPath("$.name").value("Test Graph"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void shouldRetrieveAllGraphs() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Graph 1\",\"description\":\"First\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Graph 2\",\"description\":\"Second\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/graphs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Graph 1", "Graph 2")));
    }

    @Test
    void shouldReturn404ForNonExistentGraph() throws Exception {
        mockMvc.perform(get("/graphs/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectGraphWithoutName() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Test\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectGraphWithoutNodeType() throws Exception {
        mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"description\":\"Test\",\"edgeType\":\"String\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAddNodeToGraph() throws Exception {
        String createResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Graph\",\"description\":\"Test\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Graph graph = objectMapper.readValue(createResponse, Graph.class);

        mockMvc.perform(post("/graphs/" + graph.getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node A\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.graph.version").value(2))
                .andExpect(jsonPath("$.graph.parentId").value(graph.getId()))
                .andExpect(jsonPath("$.structure.nodeCount").value(1))
                .andExpect(jsonPath("$.structure.edgeCount").value(0));
    }

    @Test
    void shouldAddMultipleNodesToGraph() throws Exception {
        String createResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Graph\",\"description\":\"Test\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Graph graph = objectMapper.readValue(createResponse, Graph.class);

        String addNodeResponse1 = mockMvc.perform(post("/graphs/" + graph.getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        GraphController.GraphResponse response1 = objectMapper.readValue(
                addNodeResponse1,
                GraphController.GraphResponse.class
        );

        mockMvc.perform(post("/graphs/" + response1.graph().getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node B\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.graph.version").value(3))
                .andExpect(jsonPath("$.structure.nodeCount").value(2))
                .andExpect(jsonPath("$.structure.edgeCount").value(0));
    }

    @Test
    void shouldAddEdgeToGraph() throws Exception {
        String createResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Graph\",\"description\":\"Test\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Graph graph = objectMapper.readValue(createResponse, Graph.class);

        String addNode1Response = mockMvc.perform(post("/graphs/" + graph.getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node A\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        GraphController.GraphResponse response1 = objectMapper.readValue(
                addNode1Response,
                GraphController.GraphResponse.class
        );

        String addNode2Response = mockMvc.perform(post("/graphs/" + response1.graph().getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node B\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        GraphController.GraphResponse response2 = objectMapper.readValue(
                addNode2Response,
                GraphController.GraphResponse.class
        );

        mockMvc.perform(post("/graphs/" + response2.graph().getId() + "/edges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromNodeId\":0,\"toNodeId\":1,\"label\":\"Edge A->B\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.graph.version").value(4))
                .andExpect(jsonPath("$.structure.nodeCount").value(2))
                .andExpect(jsonPath("$.structure.edgeCount").value(1));
    }

    @Test
    void shouldGetGraphStructure() throws Exception {
        String createResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Graph\",\"description\":\"Test\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Graph graph = objectMapper.readValue(createResponse, Graph.class);

        mockMvc.perform(get("/graphs/" + graph.getId() + "/structure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(graph.getId()))
                .andExpect(jsonPath("$.name").value("Test Graph"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.nodeCount").value(0))
                .andExpect(jsonPath("$.edgeCount").value(0));
    }

    @Test
    void shouldPerformDepthFirstTraversal() throws Exception {
        String createResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Graph\",\"description\":\"Test\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Graph graph = objectMapper.readValue(createResponse, Graph.class);

        String addNode1Response = mockMvc.perform(post("/graphs/" + graph.getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node A\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        GraphController.GraphResponse response1 = objectMapper.readValue(
                addNode1Response,
                GraphController.GraphResponse.class
        );

        String addNode2Response = mockMvc.perform(post("/graphs/" + response1.graph().getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node B\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        GraphController.GraphResponse response2 = objectMapper.readValue(
                addNode2Response,
                GraphController.GraphResponse.class
        );

        mockMvc.perform(get("/graphs/" + response2.graph().getId() + "/traversal/dfs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("depth-first"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes", hasSize(2)));
    }

    @Test
    void shouldPerformBreadthFirstTraversal() throws Exception {
        String createResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Graph\",\"description\":\"Test\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Graph graph = objectMapper.readValue(createResponse, Graph.class);

        String addNode1Response = mockMvc.perform(post("/graphs/" + graph.getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node A\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        GraphController.GraphResponse response1 = objectMapper.readValue(
                addNode1Response,
                GraphController.GraphResponse.class
        );

        String addNode2Response = mockMvc.perform(post("/graphs/" + response1.graph().getId() + "/nodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Node B\"}"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        GraphController.GraphResponse response2 = objectMapper.readValue(
                addNode2Response,
                GraphController.GraphResponse.class
        );

        mockMvc.perform(get("/graphs/" + response2.graph().getId() + "/traversal/bfs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("breadth-first"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes", hasSize(2)));
    }

    @Test
    void shouldDeleteGraph() throws Exception {
        String createResponse = mockMvc.perform(post("/graphs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Graph\",\"description\":\"Test\",\"nodeType\":\"String\",\"edgeType\":\"String\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Graph graph = objectMapper.readValue(createResponse, Graph.class);

        mockMvc.perform(delete("/graphs/" + graph.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/graphs/" + graph.getId()))
                .andExpect(status().isNotFound());
    }
}
