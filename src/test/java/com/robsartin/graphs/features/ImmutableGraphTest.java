package com.robsartin.graphs.features;

import com.robsartin.graphs.infrastructure.ImmutableGraph;
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