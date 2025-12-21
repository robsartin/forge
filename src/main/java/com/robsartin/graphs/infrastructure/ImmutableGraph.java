package com.robsartin.graphs.infrastructure;

import java.util.*;
import java.util.function.Consumer;

/**
 * Immutable graph implementation based on Martin Erwig's inductive graph approach
 * from "Fully Persistent Graphs – Which One To Choose?"
 */
public class ImmutableGraph<N, E> {

    private final Map<UUID, Context<N, E>> nodes;

    // Empty graph constructor
    public ImmutableGraph() {
        this.nodes = Collections.emptyMap();
    }

    private ImmutableGraph(Map<UUID, Context<N, E>> nodes) {
        this.nodes = Collections.unmodifiableMap(new HashMap<>(nodes));
    }

    /**
     * Context represents a node with its label and adjacent edges
     */
    public static class Context<N, E> {
        private final UUID nodeId;
        private final N label;
        private final Map<UUID, E> predecessors;  // incoming edges
        private final Map<UUID, E> successors;    // outgoing edges

        public Context(UUID nodeId, N label,
                       Map<UUID, E> predecessors,
                       Map<UUID, E> successors) {
            this.nodeId = nodeId;
            this.label = label;
            this.predecessors = Collections.unmodifiableMap(new HashMap<>(predecessors));
            this.successors = Collections.unmodifiableMap(new HashMap<>(successors));
        }

        public UUID getNodeId() { return nodeId; }
        public N getLabel() { return label; }
        public Map<UUID, E> getPredecessors() { return predecessors; }
        public Map<UUID, E> getSuccessors() { return successors; }
    }

    /**
     * Decomposition result - separates a node's context from the rest of the graph
     */
    public static class Decomposition<N, E> {
        private final Context<N, E> context;
        private final ImmutableGraph<N, E> graph;

        public Decomposition(Context<N, E> context, ImmutableGraph<N, E> graph) {
            this.context = context;
            this.graph = graph;
        }

        public Context<N, E> getContext() { return context; }
        public ImmutableGraph<N, E> getGraph() { return graph; }
        public boolean isEmpty() { return context == null; }
    }

    /**
     * Add a node with a label, returns new graph and node ID
     */
    public GraphWithNode<N, E> addNode(N label) {
        Map<UUID, Context<N, E>> newNodes = new HashMap<>(nodes);
        UUID nodeId = UuidV7Generator.generate();
        Context<N, E> context = new Context<>(nodeId, label,
                                              Collections.emptyMap(), Collections.emptyMap());
        newNodes.put(nodeId, context);
        return new GraphWithNode<>(
                new ImmutableGraph<>(newNodes),
                nodeId
        );
    }

    /**
     * Add a node with a specific UUID (for reconstruction from persistence)
     */
    public GraphWithNode<N, E> addNodeWithId(UUID nodeId, N label) {
        Map<UUID, Context<N, E>> newNodes = new HashMap<>(nodes);
        Context<N, E> context = new Context<>(nodeId, label,
                                              Collections.emptyMap(), Collections.emptyMap());
        newNodes.put(nodeId, context);
        return new GraphWithNode<>(
                new ImmutableGraph<>(newNodes),
                nodeId
        );
    }

    /**
     * Add an edge between two nodes, returns new graph
     */
    public ImmutableGraph<N, E> addEdge(UUID fromNode, UUID toNode, E edgeLabel) {
        if (!nodes.containsKey(fromNode) || !nodes.containsKey(toNode)) {
            throw new IllegalArgumentException("Both nodes must exist in the graph");
        }

        Map<UUID, Context<N, E>> newNodes = new HashMap<>(nodes);

        // Update source node's successors
        Context<N, E> fromContext = nodes.get(fromNode);
        Map<UUID, E> newSuccessors = new HashMap<>(fromContext.successors);
        newSuccessors.put(toNode, edgeLabel);
        newNodes.put(fromNode, new Context<>(fromNode, fromContext.label,
                                             fromContext.predecessors, newSuccessors));

        // Update target node's predecessors
        Context<N, E> toContext = nodes.get(toNode);
        Map<UUID, E> newPredecessors = new HashMap<>(toContext.predecessors);
        newPredecessors.put(fromNode, edgeLabel);
        newNodes.put(toNode, new Context<>(toNode, toContext.label,
                                           newPredecessors, toContext.successors));

        return new ImmutableGraph<>(newNodes);
    }

    /**
     * Remove an edge between two nodes, returns new graph
     */
    public ImmutableGraph<N, E> removeEdge(UUID fromNode, UUID toNode) {
        if (!nodes.containsKey(fromNode) || !nodes.containsKey(toNode)) {
            return this;
        }

        Map<UUID, Context<N, E>> newNodes = new HashMap<>(nodes);

        // Update source node's successors
        Context<N, E> fromContext = nodes.get(fromNode);
        Map<UUID, E> newSuccessors = new HashMap<>(fromContext.successors);
        newSuccessors.remove(toNode);
        newNodes.put(fromNode, new Context<>(fromNode, fromContext.label,
                                             fromContext.predecessors, newSuccessors));

        // Update target node's predecessors
        Context<N, E> toContext = nodes.get(toNode);
        Map<UUID, E> newPredecessors = new HashMap<>(toContext.predecessors);
        newPredecessors.remove(fromNode);
        newNodes.put(toNode, new Context<>(toNode, toContext.label,
                                           newPredecessors, toContext.successors));

        return new ImmutableGraph<>(newNodes);
    }

    /**
     * Match operation - decompose graph by extracting a node
     */
    public Decomposition<N, E> match(UUID nodeId) {
        if (!nodes.containsKey(nodeId)) {
            return new Decomposition<>(null, this);
        }

        Context<N, E> context = nodes.get(nodeId);
        Map<UUID, Context<N, E>> newNodes = new HashMap<>(nodes);
        newNodes.remove(nodeId);

        // Remove references to this node from all other nodes
        for (UUID pred : context.predecessors.keySet()) {
            if (newNodes.containsKey(pred)) {
                Context<N, E> predContext = newNodes.get(pred);
                Map<UUID, E> newSuccessors = new HashMap<>(predContext.successors);
                newSuccessors.remove(nodeId);
                newNodes.put(pred, new Context<>(pred, predContext.label,
                                                 predContext.predecessors, newSuccessors));
            }
        }

        for (UUID succ : context.successors.keySet()) {
            if (newNodes.containsKey(succ)) {
                Context<N, E> succContext = newNodes.get(succ);
                Map<UUID, E> newPredecessors = new HashMap<>(succContext.predecessors);
                newPredecessors.remove(nodeId);
                newNodes.put(succ, new Context<>(succ, succContext.label,
                                                 newPredecessors, succContext.successors));
            }
        }

        return new Decomposition<>(context, new ImmutableGraph<>(newNodes));
    }

    /**
     * Compose - add a context back to a graph
     */
    public ImmutableGraph<N, E> compose(Context<N, E> context) {
        Map<UUID, Context<N, E>> newNodes = new HashMap<>(nodes);
        newNodes.put(context.nodeId, context);

        // Update predecessor nodes
        for (Map.Entry<UUID, E> pred : context.predecessors.entrySet()) {
            if (newNodes.containsKey(pred.getKey())) {
                Context<N, E> predContext = newNodes.get(pred.getKey());
                Map<UUID, E> newSuccessors = new HashMap<>(predContext.successors);
                newSuccessors.put(context.nodeId, pred.getValue());
                newNodes.put(pred.getKey(), new Context<>(pred.getKey(),
                                                          predContext.label, predContext.predecessors, newSuccessors));
            }
        }

        // Update successor nodes
        for (Map.Entry<UUID, E> succ : context.successors.entrySet()) {
            if (newNodes.containsKey(succ.getKey())) {
                Context<N, E> succContext = newNodes.get(succ.getKey());
                Map<UUID, E> newPredecessors = new HashMap<>(succContext.predecessors);
                newPredecessors.put(context.nodeId, succ.getValue());
                newNodes.put(succ.getKey(), new Context<>(succ.getKey(),
                                                          succContext.label, newPredecessors, succContext.successors));
            }
        }

        return new ImmutableGraph<>(newNodes);
    }

    public boolean containsNode(UUID nodeId) {
        return nodes.containsKey(nodeId);
    }

    public Context<N, E> getContext(UUID nodeId) {
        return nodes.get(nodeId);
    }

    public Set<UUID> getNodeIds() {
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
    public void depthFirstTraversal(UUID startNode, Consumer<Context<N, E>> visitor) {
        if (!nodes.containsKey(startNode)) {
            throw new IllegalArgumentException("Start node does not exist");
        }

        Set<UUID> visited = new HashSet<>();
        dfsHelper(startNode, visitor, visited);
    }

    private void dfsHelper(UUID nodeId, Consumer<Context<N, E>> visitor, Set<UUID> visited) {
        if (visited.contains(nodeId)) {
            return;
        }

        visited.add(nodeId);
        Context<N, E> context = nodes.get(nodeId);
        visitor.accept(context);

        for (UUID successor : context.successors.keySet()) {
            dfsHelper(successor, visitor, visited);
        }
    }

    /**
     * Breadth-first traversal
     */
    public void breadthFirstTraversal(UUID startNode, Consumer<Context<N, E>> visitor) {
        if (!nodes.containsKey(startNode)) {
            throw new IllegalArgumentException("Start node does not exist");
        }

        Set<UUID> visited = new HashSet<>();
        Queue<UUID> queue = new LinkedList<>();

        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            UUID nodeId = queue.poll();
            Context<N, E> context = nodes.get(nodeId);
            visitor.accept(context);

            for (UUID successor : context.successors.keySet()) {
                if (!visited.contains(successor)) {
                    visited.add(successor);
                    queue.offer(successor);
                }
            }
        }
    }

    /**
     * Helper class to return both graph and node ID
     */
    public static class GraphWithNode<N, E> {
        private final ImmutableGraph<N, E> graph;
        private final UUID nodeId;

        public GraphWithNode(ImmutableGraph<N, E> graph, UUID nodeId) {
            this.graph = graph;
            this.nodeId = nodeId;
        }

        public ImmutableGraph<N, E> getGraph() { return graph; }
        public UUID getNodeId() { return nodeId; }
    }
}
