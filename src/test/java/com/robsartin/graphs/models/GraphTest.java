package com.robsartin.graphs.models;

import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import com.robsartin.graphs.ports.out.GraphRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Import(TestOpenFeatureConfiguration.class)
class GraphTest {

    @Autowired
    private GraphRepository graphRepository;

    @BeforeEach
    void setUp() {
        graphRepository.deleteAll();
    }

    @Test
    @DisplayName("Node database ID should be used as immutable graph node ID")
    void nodeDatabaseIdShouldBeUsedAsImmutableGraphNodeId() {
        Graph graph = new Graph("Test Graph");
        GraphNode node = graph.addNode("Node A");
        Graph savedGraph = graphRepository.save(graph);

        GraphNode savedNode = savedGraph.getNodes().get(0);
        Integer nodeDbId = savedNode.getId();

        // The immutable graph should contain a node with the same ID as the database ID
        assertTrue(savedGraph.getImmutableGraph().containsNode(nodeDbId));
        assertEquals("Node A", savedGraph.getImmutableGraph().getContext(nodeDbId).getLabel());
    }

    @Test
    @DisplayName("Multiple nodes should use their database IDs in immutable graph")
    void multipleNodesShouldUseDatabaseIdsInImmutableGraph() {
        Graph graph = new Graph("Test Graph");
        graph.addNode("Node A");
        graph.addNode("Node B");
        graph.addNode("Node C");
        Graph savedGraph = graphRepository.save(graph);

        for (GraphNode node : savedGraph.getNodes()) {
            Integer nodeDbId = node.getId();
            assertTrue(savedGraph.getImmutableGraph().containsNode(nodeDbId),
                    "Immutable graph should contain node with database ID " + nodeDbId);
            assertEquals(node.getName(), savedGraph.getImmutableGraph().getContext(nodeDbId).getLabel());
        }
    }

    @Test
    @DisplayName("Edges should work with database IDs")
    void edgesShouldWorkWithDatabaseIds() {
        Graph graph = new Graph("Test Graph");
        graph.addNode("Node A");
        graph.addNode("Node B");
        Graph savedGraph = graphRepository.save(graph);

        GraphNode nodeA = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node A"))
                .findFirst().orElseThrow();
        GraphNode nodeB = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node B"))
                .findFirst().orElseThrow();

        savedGraph.addEdge(nodeA.getId(), nodeB.getId());
        Graph graphWithEdge = graphRepository.save(savedGraph);

        var contextA = graphWithEdge.getImmutableGraph().getContext(nodeA.getId());
        assertTrue(contextA.getSuccessors().containsKey(nodeB.getId()),
                "Node A should have an edge to Node B using database IDs");
    }

    @Test
    @DisplayName("findNodeById should find node by database ID")
    void findNodeByIdShouldFindNodeByDatabaseId() {
        Graph graph = new Graph("Test Graph");
        graph.addNode("Node A");
        graph.addNode("Node B");
        Graph savedGraph = graphRepository.save(graph);

        GraphNode nodeA = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node A"))
                .findFirst().orElseThrow();

        GraphNode found = savedGraph.findNodeById(nodeA.getId());
        assertNotNull(found);
        assertEquals("Node A", found.getName());
    }

    @Test
    @DisplayName("Traversals should work with database IDs")
    void traversalsShouldWorkWithDatabaseIds() {
        Graph graph = new Graph("Test Graph");
        graph.addNode("Node A");
        graph.addNode("Node B");
        graph.addNode("Node C");
        Graph savedGraph = graphRepository.save(graph);

        GraphNode nodeA = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node A"))
                .findFirst().orElseThrow();
        GraphNode nodeB = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node B"))
                .findFirst().orElseThrow();
        GraphNode nodeC = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node C"))
                .findFirst().orElseThrow();

        savedGraph.addEdge(nodeA.getId(), nodeB.getId());
        savedGraph.addEdge(nodeB.getId(), nodeC.getId());
        Graph graphWithEdges = graphRepository.save(savedGraph);

        // DFS from Node A should visit A -> B -> C
        java.util.List<String> dfsOrder = new java.util.ArrayList<>();
        graphWithEdges.getImmutableGraph().depthFirstTraversal(nodeA.getId(),
                ctx -> dfsOrder.add(ctx.getLabel()));

        assertEquals(3, dfsOrder.size());
        assertEquals("Node A", dfsOrder.get(0));
        assertEquals("Node B", dfsOrder.get(1));
        assertEquals("Node C", dfsOrder.get(2));
    }

    @Test
    @DisplayName("Graph reconstruction on load should use database IDs")
    void graphReconstructionOnLoadShouldUseDatabaseIds() {
        // Create and save a graph with nodes
        Graph graph = new Graph("Test Graph");
        graph.addNode("Node A");
        graph.addNode("Node B");
        Graph savedGraph = graphRepository.save(graph);

        Integer graphId = savedGraph.getId();
        Integer nodeADbId = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node A"))
                .findFirst().orElseThrow().getId();
        Integer nodeBDbId = savedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Node B"))
                .findFirst().orElseThrow().getId();

        // Clear entity manager to force reload
        graphRepository.flush();

        // Reload the graph - this should trigger @PostLoad
        Graph reloadedGraph = graphRepository.findById(graphId).orElseThrow();

        // Verify the immutable graph uses database IDs
        assertTrue(reloadedGraph.getImmutableGraph().containsNode(nodeADbId));
        assertTrue(reloadedGraph.getImmutableGraph().containsNode(nodeBDbId));
        assertEquals("Node A", reloadedGraph.getImmutableGraph().getContext(nodeADbId).getLabel());
        assertEquals("Node B", reloadedGraph.getImmutableGraph().getContext(nodeBDbId).getLabel());
    }
}
