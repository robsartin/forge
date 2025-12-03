package infrastructure;

import java.util.*;
import java.util.function.Consumer;

/**
 * Traditional mutable graph implementation using adjacency lists
 */
public class MutableGraph<N, E> {

    private final Map<Integer, Node<N, E>> nodes;
    private int nextNodeId;

    public MutableGraph() {
        this.nodes = new HashMap<>();
        this.nextNodeId = 0;
    }

    /**
     * Node with label and adjacency information
     */
    public static class Node<N, E> {
        private final int nodeId;
        private N label;
        private final Map<Integer, E> outgoingEdges;
        private final Map<Integer, E> incomingEdges;

        public Node(int nodeId, N label) {
            this.nodeId = nodeId;
            this.label = label;
            this.outgoingEdges = new HashMap<>();
            this.incomingEdges = new HashMap<>();
        }

        public int getNodeId() { return nodeId; }
        public N getLabel() { return label; }
        public void setLabel(N label) { this.label = label; }
        public Map<Integer, E> getOutgoingEdges() { return outgoingEdges; }
        public Map<Integer, E> getIncomingEdges() { return incomingEdges; }
    }

    /**
     * Add a node with a label
     */
    public int addNode(N label) {
        int nodeId = nextNodeId++;
        nodes.put(nodeId, new Node<>(nodeId, label));
        return nodeId;
    }

    /**
     * Remove a node from the graph
     */
    public void removeNode(int nodeId) {
        Node<N, E> node = nodes.get(nodeId);
        if (node == null) {
            return;
        }

        // Remove all edges to/from this node
        for (int pred : node.incomingEdges.keySet()) {
            Node<N, E> predNode = nodes.get(pred);
            if (predNode != null) {
                predNode.outgoingEdges.remove(nodeId);
            }
        }

        for (int succ : node.outgoingEdges.keySet()) {
            Node<N, E> succNode = nodes.get(succ);
            if (succNode != null) {
                succNode.incomingEdges.remove(nodeId);
            }
        }

        nodes.remove(nodeId);
    }

    /**
     * Add an edge between two nodes
     */
    public void addEdge(int fromNode, int toNode, E edgeLabel) {
        Node<N, E> from = nodes.get(fromNode);
        Node<N, E> to = nodes.get(toNode);

        if (from == null || to == null) {
            throw new IllegalArgumentException("Both nodes must exist in the graph");
        }

        from.outgoingEdges.put(toNode, edgeLabel);
        to.incomingEdges.put(fromNode, edgeLabel);
    }

    /**
     * Remove an edge
     */
    public void removeEdge(int fromNode, int toNode) {
        Node<N, E> from = nodes.get(fromNode);
        Node<N, E> to = nodes.get(toNode);

        if (from != null) {
            from.outgoingEdges.remove(toNode);
        }
        if (to != null) {
            to.incomingEdges.remove(fromNode);
        }
    }

    public boolean containsNode(int nodeId) {
        return nodes.containsKey(nodeId);
    }

    public Node<N, E> getNode(int nodeId) {
        return nodes.get(nodeId);
    }

    public Set<Integer> getNodeIds() {
        return nodes.keySet();
    }

    public int nodeCount() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    /**
     * Depth-first traversal
     */
    public void depthFirstTraversal(int startNode, Consumer<Node<N, E>> visitor) {
        Node<N, E> start = nodes.get(startNode);
        if (start == null) {
            throw new IllegalArgumentException("Start node does not exist");
        }

        Set<Integer> visited = new HashSet<>();
        dfsHelper(startNode, visitor, visited);
    }

    private void dfsHelper(int nodeId, Consumer<Node<N, E>> visitor, Set<Integer> visited) {
        if (visited.contains(nodeId)) {
            return;
        }

        visited.add(nodeId);
        Node<N, E> node = nodes.get(nodeId);
        visitor.accept(node);

        for (int successor : node.outgoingEdges.keySet()) {
            dfsHelper(successor, visitor, visited);
        }
    }

    /**
     * Breadth-first traversal
     */
    public void breadthFirstTraversal(int startNode, Consumer<Node<N, E>> visitor) {
        Node<N, E> start = nodes.get(startNode);
        if (start == null) {
            throw new IllegalArgumentException("Start node does not exist");
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            int nodeId = queue.poll();
            Node<N, E> node = nodes.get(nodeId);
            visitor.accept(node);

            for (int successor : node.outgoingEdges.keySet()) {
                if (!visited.contains(successor)) {
                    visited.add(successor);
                    queue.offer(successor);
                }
            }
        }
    }
}