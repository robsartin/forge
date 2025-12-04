package com.robsartin.graphs.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class MutableGraphTest {

    @Test
    @DisplayName("Create empty graph")
    void testEmptyGraph() {
        MutableGraph<String, Integer> graph = new MutableGraph<>();
        assertTrue(graph.isEmpty());
        assertEquals(0, graph.nodeCount());
    }

    @Test
    @DisplayName("Add nodes to graph")
    void testAddNodes() {
        MutableGraph<String, Integer> graph = new MutableGraph<>();

        int nodeA = graph.addNode("A");
        int nodeB = graph.addNode("B");

        assertEquals(2, graph.nodeCount());
        assertTrue(graph.containsNode(nodeA));
        assertTrue(graph.containsNode(nodeB));
        assertEquals("A", graph.getNode(nodeA).getLabel());
        assertEquals("B", graph.getNode(nodeB).getLabel());
    }

    @Test
    @DisplayName("Add edges to graph")
    void testAddEdges() {
        MutableGraph<String, Integer> graph = new MutableGraph<>();

        int nodeA = graph.addNode("A");
        int nodeB = graph.addNode("B");
        int nodeC = graph.addNode("C");

        graph.addEdge(nodeA, nodeB, 10);
        graph.addEdge(nodeB, nodeC, 20);

        var nodeAObj = graph.getNode(nodeA);
        var nodeBObj = graph.getNode(nodeB);
        var nodeCObj = graph.getNode(nodeC);

        assertTrue(nodeAObj.getOutgoingEdges().containsKey(nodeB));
        assertEquals(10, nodeAObj.getOutgoingEdges().get(nodeB));

        assertTrue(nodeBObj.getIncomingEdges().containsKey(nodeA));
        assertTrue(nodeBObj.getOutgoingEdges().containsKey(nodeC));
        assertEquals(20, nodeBObj.getOutgoingEdges().get(nodeC));

        assertTrue(nodeCObj.getIncomingEdges().containsKey(nodeB));
    }

    @Test
    @DisplayName("Remove node from graph")
    void testRemoveNode() {
        MutableGraph<String, Integer> graph = new MutableGraph<>();

        int nodeA = graph.addNode("A");
        int nodeB = graph.addNode("B");
        int nodeC = graph.addNode("C");

        graph.addEdge(nodeA, nodeB, 10);
        graph.addEdge(nodeB, nodeC, 20);

        assertEquals(3, graph.nodeCount());

        graph.removeNode(nodeB);

        assertEquals(2, graph.nodeCount());
        assertFalse(graph.containsNode(nodeB));

        // Edges should be removed
        var nodeAObj = graph.getNode(nodeA);
        assertFalse(nodeAObj.getOutgoingEdges().containsKey(nodeB));
    }

    @Test
    @DisplayName("Remove edge from graph")
    void testRemoveEdge() {
        MutableGraph<String, Integer> graph = new MutableGraph<>();

        int nodeA = graph.addNode("A");
        int nodeB = graph.addNode("B");

        graph.addEdge(nodeA, nodeB, 10);
        assertTrue(graph.getNode(nodeA).getOutgoingEdges().containsKey(nodeB));

        graph.removeEdge(nodeA, nodeB);
        assertFalse(graph.getNode(nodeA).getOutgoingEdges().containsKey(nodeB));
        assertFalse(graph.getNode(nodeB).getIncomingEdges().containsKey(nodeA));
    }

    @Test
    @DisplayName("Depth-first traversal")
    void testDepthFirstTraversal() {
        MutableGraph<String, Integer> graph = buildTestGraph();
        int nodeA = 0;  // First node added

        List<String> visited = new ArrayList<>();
        graph.depthFirstTraversal(nodeA, node -> visited.add(node.getLabel()));

        assertEquals(4, visited.size());
        assertEquals("A", visited.get(0));
        assertTrue(visited.contains("B"));
        assertTrue(visited.contains("C"));
        assertTrue(visited.contains("D"));

        // B should come before D
        int bIndex = visited.indexOf("B");
        int dIndex = visited.indexOf("D");
        assertTrue(bIndex < dIndex);
    }

    @Test
    @DisplayName("Breadth-first traversal")
    void testBreadthFirstTraversal() {
        MutableGraph<String, Integer> graph = buildTestGraph();
        int nodeA = 0;

        List<String> visited = new ArrayList<>();
        graph.breadthFirstTraversal(nodeA, node -> visited.add(node.getLabel()));

        assertEquals(4, visited.size());
        assertEquals("A", visited.get(0));

        int bIndex = visited.indexOf("B");
        int cIndex = visited.indexOf("C");
        int dIndex = visited.indexOf("D");

        assertTrue(bIndex < dIndex);
        assertTrue(cIndex < dIndex);
    }

    @Test
    @DisplayName("Multiple traversals on same graph")
    void testMultipleTraversals() {
        MutableGraph<String, Integer> graph = buildTestGraph();

        List<String> firstTraversal = new ArrayList<>();
        graph.depthFirstTraversal(0, node -> firstTraversal.add(node.getLabel()));

        List<String> secondTraversal = new ArrayList<>();
        graph.depthFirstTraversal(0, node -> secondTraversal.add(node.getLabel()));

        assertEquals(firstTraversal, secondTraversal);
        assertEquals(4, graph.nodeCount());
    }

    /**
     * Build a test graph:
     *     A -> B -> D
     *     A -> C
     */
    private MutableGraph<String, Integer> buildTestGraph() {
        MutableGraph<String, Integer> graph = new MutableGraph<>();

        int nodeA = graph.addNode("A");
        int nodeB = graph.addNode("B");
        int nodeC = graph.addNode("C");
        int nodeD = graph.addNode("D");

        graph.addEdge(nodeA, nodeB, 1);
        graph.addEdge(nodeA, nodeC, 2);
        graph.addEdge(nodeB, nodeD, 3);

        return graph;
    }
}