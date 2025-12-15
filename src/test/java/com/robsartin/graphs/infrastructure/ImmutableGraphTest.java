package com.robsartin.graphs.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class ImmutableGraphTest {

    @Test
    @DisplayName("Create empty graph")
    void testEmptyGraph() {
        ImmutableGraph<String, Integer> graph = new ImmutableGraph<>();
        assertTrue(graph.isEmpty());
        assertEquals(0, graph.nodeCount());
    }

    @Test
    @DisplayName("Add nodes to graph")
    void testAddNodes() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();

        var gn1 = g0.addNode("A");
        ImmutableGraph<String, Integer> g1 = gn1.getGraph();
        int nodeA = gn1.getNodeId();

        var gn2 = g1.addNode("B");
        ImmutableGraph<String, Integer> g2 = gn2.getGraph();
        int nodeB = gn2.getNodeId();

        // Original graph unchanged
        assertEquals(0, g0.nodeCount());

        // New graphs have nodes
        assertEquals(1, g1.nodeCount());
        assertEquals(2, g2.nodeCount());

        assertTrue(g2.containsNode(nodeA));
        assertTrue(g2.containsNode(nodeB));
        assertEquals("A", g2.getContext(nodeA).getLabel());
        assertEquals("B", g2.getContext(nodeB).getLabel());
    }

    @Test
    @DisplayName("Add edges to graph")
    void testAddEdges() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();

        var gn1 = g0.addNode("A");
        var gn2 = gn1.getGraph().addNode("B");
        var gn3 = gn2.getGraph().addNode("C");

        ImmutableGraph<String, Integer> g1 = gn3.getGraph();
        int nodeA = gn1.getNodeId();
        int nodeB = gn2.getNodeId();
        int nodeC = gn3.getNodeId();

        ImmutableGraph<String, Integer> g2 = g1.addEdge(nodeA, nodeB, 10);
        ImmutableGraph<String, Integer> g3 = g2.addEdge(nodeB, nodeC, 20);

        // Check edges exist
        var contextA = g3.getContext(nodeA);
        var contextB = g3.getContext(nodeB);
        var contextC = g3.getContext(nodeC);

        assertTrue(contextA.getSuccessors().containsKey(nodeB));
        assertEquals(10, contextA.getSuccessors().get(nodeB));

        assertTrue(contextB.getPredecessors().containsKey(nodeA));
        assertTrue(contextB.getSuccessors().containsKey(nodeC));
        assertEquals(20, contextB.getSuccessors().get(nodeC));

        assertTrue(contextC.getPredecessors().containsKey(nodeB));

        // Original graph unchanged
        assertEquals(0, g1.getContext(nodeA).getSuccessors().size());
    }

    @Test
    @DisplayName("Match operation decomposes graph")
    void testMatch() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();

        var gn1 = g0.addNode("A");
        var gn2 = gn1.getGraph().addNode("B");

        ImmutableGraph<String, Integer> g1 = gn2.getGraph();
        int nodeA = gn1.getNodeId();
        int nodeB = gn2.getNodeId();

        g1 = g1.addEdge(nodeA, nodeB, 10);

        var decomp = g1.match(nodeA);
        assertFalse(decomp.isEmpty());
        assertEquals("A", decomp.getContext().getLabel());

        ImmutableGraph<String, Integer> remaining = decomp.getGraph();
        assertEquals(1, remaining.nodeCount());
        assertTrue(remaining.containsNode(nodeB));
        assertFalse(remaining.containsNode(nodeA));

        // Original graph unchanged
        assertEquals(2, g1.nodeCount());
    }

    @Test
    @DisplayName("Depth-first traversal")
    void testDepthFirstTraversal() {
        ImmutableGraph<String, Integer> graph = buildTestGraph();
        int startNode = 0;  // Node "A"

        List<String> visited = new ArrayList<>();
        graph.depthFirstTraversal(startNode, ctx -> visited.add(ctx.getLabel()));

        // Should visit A, then B, then D, then C
        assertEquals(4, visited.size());
        assertEquals("A", visited.get(0));
        assertTrue(visited.contains("B"));
        assertTrue(visited.contains("C"));
        assertTrue(visited.contains("D"));

        // B should come before D (B->D edge)
        int bIndex = visited.indexOf("B");
        int dIndex = visited.indexOf("D");
        assertTrue(bIndex < dIndex);
    }

    @Test
    @DisplayName("Breadth-first traversal")
    void testBreadthFirstTraversal() {
        ImmutableGraph<String, Integer> graph = buildTestGraph();
        int startNode = 0;  // Node "A"

        List<String> visited = new ArrayList<>();
        graph.breadthFirstTraversal(startNode, ctx -> visited.add(ctx.getLabel()));

        assertEquals(4, visited.size());
        assertEquals("A", visited.get(0));

        // B and C should be at level 1 (visited before D at level 2)
        int bIndex = visited.indexOf("B");
        int cIndex = visited.indexOf("C");
        int dIndex = visited.indexOf("D");

        assertTrue(bIndex < dIndex);
        assertTrue(cIndex < dIndex);
    }

    @Test
    @DisplayName("Multiple traversals on same graph (immutability test)")
    void testMultipleTraversals() {
        ImmutableGraph<String, Integer> graph = buildTestGraph();

        List<String> firstTraversal = new ArrayList<>();
        graph.depthFirstTraversal(0, ctx -> firstTraversal.add(ctx.getLabel()));

        List<String> secondTraversal = new ArrayList<>();
        graph.depthFirstTraversal(0, ctx -> secondTraversal.add(ctx.getLabel()));

        assertEquals(firstTraversal, secondTraversal);
        assertEquals(4, graph.nodeCount());
    }

    @Test
    @DisplayName("Compose operation rebuilds graph")
    void testCompose() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();
        var gn1 = g0.addNode("A");
        var gn2 = gn1.getGraph().addNode("B");

        ImmutableGraph<String, Integer> g1 = gn2.getGraph();
        int nodeA = gn1.getNodeId();
        int nodeB = gn2.getNodeId();

        g1 = g1.addEdge(nodeA, nodeB, 10);

        var decomp = g1.match(nodeA);
        ImmutableGraph<String, Integer> g2 = decomp.getGraph().compose(decomp.getContext());

        assertEquals(2, g2.nodeCount());
        assertTrue(g2.containsNode(nodeA));
        assertTrue(g2.containsNode(nodeB));

        var contextA = g2.getContext(nodeA);
        assertTrue(contextA.getSuccessors().containsKey(nodeB));
    }

    @Test
    @DisplayName("Add node with specific ID")
    void testAddNodeWithSpecificId() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();

        // Add node with specific ID 42
        ImmutableGraph<String, Integer> g1 = g0.addNodeWithId(42, "A");

        assertEquals(1, g1.nodeCount());
        assertTrue(g1.containsNode(42));
        assertEquals("A", g1.getContext(42).getLabel());

        // Original graph unchanged
        assertEquals(0, g0.nodeCount());
    }

    @Test
    @DisplayName("Add multiple nodes with specific IDs")
    void testAddMultipleNodesWithSpecificIds() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();

        ImmutableGraph<String, Integer> g1 = g0.addNodeWithId(100, "A");
        ImmutableGraph<String, Integer> g2 = g1.addNodeWithId(200, "B");
        ImmutableGraph<String, Integer> g3 = g2.addNodeWithId(150, "C");

        assertEquals(3, g3.nodeCount());
        assertTrue(g3.containsNode(100));
        assertTrue(g3.containsNode(200));
        assertTrue(g3.containsNode(150));
        assertEquals("A", g3.getContext(100).getLabel());
        assertEquals("B", g3.getContext(200).getLabel());
        assertEquals("C", g3.getContext(150).getLabel());
    }

    @Test
    @DisplayName("Add edges between nodes with specific IDs")
    void testAddEdgesBetweenNodesWithSpecificIds() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();

        ImmutableGraph<String, Integer> g1 = g0.addNodeWithId(100, "A");
        ImmutableGraph<String, Integer> g2 = g1.addNodeWithId(200, "B");
        ImmutableGraph<String, Integer> g3 = g2.addEdge(100, 200, 10);

        var contextA = g3.getContext(100);
        var contextB = g3.getContext(200);

        assertTrue(contextA.getSuccessors().containsKey(200));
        assertEquals(10, contextA.getSuccessors().get(200));
        assertTrue(contextB.getPredecessors().containsKey(100));
    }

    @Test
    @DisplayName("Traversals work with nodes added with specific IDs")
    void testTraversalsWithSpecificNodeIds() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();

        ImmutableGraph<String, Integer> g1 = g0.addNodeWithId(100, "A");
        ImmutableGraph<String, Integer> g2 = g1.addNodeWithId(200, "B");
        ImmutableGraph<String, Integer> g3 = g2.addNodeWithId(300, "C");
        ImmutableGraph<String, Integer> g4 = g3.addEdge(100, 200, 1);
        ImmutableGraph<String, Integer> g5 = g4.addEdge(200, 300, 2);

        List<String> dfsVisited = new ArrayList<>();
        g5.depthFirstTraversal(100, ctx -> dfsVisited.add(ctx.getLabel()));

        assertEquals(3, dfsVisited.size());
        assertEquals("A", dfsVisited.get(0));
        assertEquals("B", dfsVisited.get(1));
        assertEquals("C", dfsVisited.get(2));

        List<String> bfsVisited = new ArrayList<>();
        g5.breadthFirstTraversal(100, ctx -> bfsVisited.add(ctx.getLabel()));

        assertEquals(3, bfsVisited.size());
        assertEquals("A", bfsVisited.get(0));
        assertEquals("B", bfsVisited.get(1));
        assertEquals("C", bfsVisited.get(2));
    }

    @Test
    @DisplayName("Adding duplicate node ID should throw exception")
    void testAddDuplicateNodeIdThrowsException() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();
        ImmutableGraph<String, Integer> g1 = g0.addNodeWithId(42, "A");

        assertThrows(IllegalArgumentException.class, () -> {
            g1.addNodeWithId(42, "B");
        });
    }

    /**
     * Build a test graph:
     *     A -> B -> D
     *     A -> C
     */
    private ImmutableGraph<String, Integer> buildTestGraph() {
        ImmutableGraph<String, Integer> g0 = new ImmutableGraph<>();

        var gn1 = g0.addNode("A");
        var gn2 = gn1.getGraph().addNode("B");
        var gn3 = gn2.getGraph().addNode("C");
        var gn4 = gn3.getGraph().addNode("D");

        ImmutableGraph<String, Integer> g = gn4.getGraph();

        int nodeA = gn1.getNodeId();
        int nodeB = gn2.getNodeId();
        int nodeC = gn3.getNodeId();
        int nodeD = gn4.getNodeId();

        g = g.addEdge(nodeA, nodeB, 1);
        g = g.addEdge(nodeA, nodeC, 2);
        g = g.addEdge(nodeB, nodeD, 3);

        return g;
    }
}