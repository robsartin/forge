package infrastructure;

import java.util.*;
import java.util.function.Consumer;

/**
 * Immutable graph implementation based on Martin Erwig's inductive graph approach
 * from "Fully Persistent Graphs – Which One To Choose?"
 */
public class ImmutableGraph<N, E> {

    private final Map<Integer, Context<N, E>> nodes;
    private final int nextNodeId;

    // Empty graph constructor
    public ImmutableGraph() {
        this.nodes = Collections.emptyMap();
        this.nextNodeId = 0;
    }

    private ImmutableGraph(Map<Integer, Context<N, E>> nodes, int nextNodeId) {
        this.nodes = Collections.unmodifiableMap(new HashMap<>(nodes));
        this.nextNodeId = nextNodeId;
    }

    /**
     * Context represents a node with its label and adjacent edges
     */
    public static class Context<N, E> {
        private final int nodeId;
        private final N label;
        private final Map<Integer, E> predecessors;  // incoming edges
        private final Map<Integer, E> successors;    // outgoing edges

        public Context(int nodeId, N label,
                       Map<Integer, E> predecessors,
                       Map<Integer, E> successors) {
            this.nodeId = nodeId;
            this.label = label;
            this.predecessors = Collections.unmodifiableMap(new HashMap<>(predecessors));
            this.successors = Collections.unmodifiableMap(new HashMap<>(successors));
        }

        public int getNodeId() { return nodeId; }
        public N getLabel() { return label; }
        public Map<Integer, E> getPredecessors() { return predecessors; }
        public Map<Integer, E> getSuccessors() { return successors; }
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
        Map<Integer, Context<N, E>> newNodes = new HashMap<>(nodes);
        int nodeId = nextNodeId;
        Context<N, E> context = new Context<>(nodeId, label,
                                              Collections.emptyMap(), Collections.emptyMap());
        newNodes.put(nodeId, context);
        return new GraphWithNode<>(
                new ImmutableGraph<>(newNodes, nextNodeId + 1),
                nodeId
        );
    }

    /**
     * Add an edge between two nodes, returns new graph
     */
    public ImmutableGraph<N, E> addEdge(int fromNode, int toNode, E edgeLabel) {
        if (!nodes.containsKey(fromNode) || !nodes.containsKey(toNode)) {
            throw new IllegalArgumentException("Both nodes must exist in the graph");
        }

        Map<Integer, Context<N, E>> newNodes = new HashMap<>(nodes);

        // Update source node's successors
        Context<N, E> fromContext = nodes.get(fromNode);
        Map<Integer, E> newSuccessors = new HashMap<>(fromContext.successors);
        newSuccessors.put(toNode, edgeLabel);
        newNodes.put(fromNode, new Context<>(fromNode, fromContext.label,
                                             fromContext.predecessors, newSuccessors));

        // Update target node's predecessors
        Context<N, E> toContext = nodes.get(toNode);
        Map<Integer, E> newPredecessors = new HashMap<>(toContext.predecessors);
        newPredecessors.put(fromNode, edgeLabel);
        newNodes.put(toNode, new Context<>(toNode, toContext.label,
                                           newPredecessors, toContext.successors));

        return new ImmutableGraph<>(newNodes, nextNodeId);
    }

    /**
     * Match operation - decompose graph by extracting a node
     */
    public Decomposition<N, E> match(int nodeId) {
        if (!nodes.containsKey(nodeId)) {
            return new Decomposition<>(null, this);
        }

        Context<N, E> context = nodes.get(nodeId);
        Map<Integer, Context<N, E>> newNodes = new HashMap<>(nodes);
        newNodes.remove(nodeId);

        // Remove references to this node from all other nodes
        for (int pred : context.predecessors.keySet()) {
            if (newNodes.containsKey(pred)) {
                Context<N, E> predContext = newNodes.get(pred);
                Map<Integer, E> newSuccessors = new HashMap<>(predContext.successors);
                newSuccessors.remove(nodeId);
                newNodes.put(pred, new Context<>(pred, predContext.label,
                                                 predContext.predecessors, newSuccessors));
            }
        }

        for (int succ : context.successors.keySet()) {
            if (newNodes.containsKey(succ)) {
                Context<N, E> succContext = newNodes.get(succ);
                Map<Integer, E> newPredecessors = new HashMap<>(succContext.predecessors);
                newPredecessors.remove(nodeId);
                newNodes.put(succ, new Context<>(succ, succContext.label,
                                                 newPredecessors, succContext.successors));
            }
        }

        return new Decomposition<>(context, new ImmutableGraph<>(newNodes, nextNodeId));
    }

    /**
     * Compose - add a context back to a graph
     */
    public ImmutableGraph<N, E> compose(Context<N, E> context) {
        Map<Integer, Context<N, E>> newNodes = new HashMap<>(nodes);
        newNodes.put(context.nodeId, context);

        // Update predecessor nodes
        for (Map.Entry<Integer, E> pred : context.predecessors.entrySet()) {
            if (newNodes.containsKey(pred.getKey())) {
                Context<N, E> predContext = newNodes.get(pred.getKey());
                Map<Integer, E> newSuccessors = new HashMap<>(predContext.successors);
                newSuccessors.put(context.nodeId, pred.getValue());
                newNodes.put(pred.getKey(), new Context<>(pred.getKey(),
                                                          predContext.label, predContext.predecessors, newSuccessors));
            }
        }

        // Update successor nodes
        for (Map.Entry<Integer, E> succ : context.successors.entrySet()) {
            if (newNodes.containsKey(succ.getKey())) {
                Context<N, E> succContext = newNodes.get(succ.getKey());
                Map<Integer, E> newPredecessors = new HashMap<>(succContext.predecessors);
                newPredecessors.put(context.nodeId, succ.getValue());
                newNodes.put(succ.getKey(), new Context<>(succ.getKey(),
                                                          succContext.label, newPredecessors, succContext.successors));
            }
        }

        return new ImmutableGraph<>(newNodes, nextNodeId);
    }

    public boolean containsNode(int nodeId) {
        return nodes.containsKey(nodeId);
    }

    public Context<N, E> getContext(int nodeId) {
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
    public void depthFirstTraversal(int startNode, Consumer<Context<N, E>> visitor) {
        if (!nodes.containsKey(startNode)) {
            throw new IllegalArgumentException("Start node does not exist");
        }

        Set<Integer> visited = new HashSet<>();
        dfsHelper(startNode, visitor, visited);
    }

    private void dfsHelper(int nodeId, Consumer<Context<N, E>> visitor, Set<Integer> visited) {
        if (visited.contains(nodeId)) {
            return;
        }

        visited.add(nodeId);
        Context<N, E> context = nodes.get(nodeId);
        visitor.accept(context);

        for (int successor : context.successors.keySet()) {
            dfsHelper(successor, visitor, visited);
        }
    }

    /**
     * Breadth-first traversal
     */
    public void breadthFirstTraversal(int startNode, Consumer<Context<N, E>> visitor) {
        if (!nodes.containsKey(startNode)) {
            throw new IllegalArgumentException("Start node does not exist");
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();

        queue.offer(startNode);
        visited.add(startNode);

        while (!queue.isEmpty()) {
            int nodeId = queue.poll();
            Context<N, E> context = nodes.get(nodeId);
            visitor.accept(context);

            for (int successor : context.successors.keySet()) {
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
        private final int nodeId;

        public GraphWithNode(ImmutableGraph<N, E> graph, int nodeId) {
            this.graph = graph;
            this.nodeId = nodeId;
        }

        public ImmutableGraph<N, E> getGraph() { return graph; }
        public int getNodeId() { return nodeId; }
    }
}