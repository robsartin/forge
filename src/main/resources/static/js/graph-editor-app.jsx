/**
 * Main Graph Editor Application Component
 */
function GraphEditor() {
    const { useState, useEffect, useRef, useCallback } = React;
    const api = window.graphApi;

    const [graphs, setGraphs] = useState([]);
    const [selectedGraph, setSelectedGraph] = useState(null);
    const [nodes, setNodes] = useState([]);
    const [edges, setEdges] = useState([]);
    const [selectedNode, setSelectedNode] = useState(null);
    const [editingNode, setEditingNode] = useState(null);
    const [editingName, setEditingName] = useState('');
    const [editingPosition, setEditingPosition] = useState({ x: 0, y: 0 });
    const [newGraphName, setNewGraphName] = useState('');
    const [newNodeName, setNewNodeName] = useState('');
    const [edgeFrom, setEdgeFrom] = useState('');
    const [edgeTo, setEdgeTo] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [status, setStatus] = useState('Ready');
    const [interactionMode, setInteractionMode] = useState('rename');
    const graphContainerRef = useRef(null);

    // Load graphs on mount
    useEffect(() => {
        loadGraphs();
    }, []);

    const loadGraphs = async () => {
        try {
            setLoading(true);
            const data = await api.getGraphs();
            setGraphs(data);
            setStatus(`Loaded ${data.length} graphs`);
        } catch (err) {
            setError('Failed to load graphs');
        } finally {
            setLoading(false);
        }
    };

    const loadGraphData = async (graphId) => {
        try {
            setLoading(true);
            const nodeData = await api.getNodes(graphId);
            setNodes(nodeData.map(n => ({ ...n, x: Math.random() * 500, y: Math.random() * 400 })));

            const edgeList = [];
            for (const node of nodeData) {
                const nodeWithLinks = await api.getNodeWithLinks(graphId, node.id);
                for (const toNode of nodeWithLinks.toNodes || []) {
                    edgeList.push({ source: node.id, target: toNode.id });
                }
            }
            setEdges(edgeList);
            setStatus(`Loaded graph with ${nodeData.length} nodes and ${edgeList.length} edges`);
        } catch (err) {
            setError('Failed to load graph data');
        } finally {
            setLoading(false);
        }
    };

    const handleSelectGraph = (graph) => {
        setSelectedGraph(graph);
        setSelectedNode(null);
        loadGraphData(graph.id);
    };

    const handleCreateGraph = async () => {
        if (!newGraphName.trim()) return;
        try {
            const newGraph = await api.createGraph(newGraphName);
            setGraphs([...graphs, newGraph]);
            setNewGraphName('');
            setStatus(`Created graph: ${newGraphName}`);
        } catch (err) {
            setError('Failed to create graph');
        }
    };

    const handleDeleteGraph = async (graphId) => {
        try {
            await api.deleteGraph(graphId);
            setGraphs(graphs.filter(g => g.id !== graphId));
            if (selectedGraph?.id === graphId) {
                setSelectedGraph(null);
                setNodes([]);
                setEdges([]);
            }
            setStatus('Graph deleted');
        } catch (err) {
            setError('Failed to delete graph');
        }
    };

    const handleCreateNode = async () => {
        if (!selectedGraph || !newNodeName.trim()) return;
        try {
            const newNode = await api.createNode(selectedGraph.id, newNodeName);
            setNodes([...nodes, { ...newNode, x: 400, y: 300 }]);
            setNewNodeName('');
            setStatus(`Created node: ${newNodeName}`);
        } catch (err) {
            setError('Failed to create node');
        }
    };

    const handleAddEdge = async () => {
        if (!selectedGraph || !edgeFrom || !edgeTo) return;
        try {
            await api.addEdge(selectedGraph.id, edgeFrom, edgeTo);
            setEdges([...edges, { source: edgeFrom, target: edgeTo }]);
            setEdgeFrom('');
            setEdgeTo('');
            setStatus('Edge added');
        } catch (err) {
            setError('Failed to add edge');
        }
    };

    const handleNodeClick = useCallback((nodeId) => {
        setSelectedNode(nodeId === selectedNode ? null : nodeId);
    }, [selectedNode]);

    const handleCanvasClick = useCallback(async (x, y) => {
        if (!selectedGraph) return;
        const nodeName = `Node ${nodes.length + 1}`;
        try {
            const newNode = await api.createNode(selectedGraph.id, nodeName);
            setNodes(prev => [...prev, { ...newNode, x, y }]);
            setStatus(`Created node: ${nodeName}`);
            setTimeout(() => {
                const container = graphContainerRef.current;
                if (container) {
                    const rect = container.getBoundingClientRect();
                    setEditingNode(newNode.id);
                    setEditingName(nodeName);
                    setEditingPosition({ x: x + rect.left + 300, y: y + rect.top + 56 });
                }
            }, 50);
        } catch (err) {
            setError('Failed to create node');
        }
    }, [selectedGraph, nodes.length]);

    const handleEditStart = useCallback((node) => {
        const container = graphContainerRef.current;
        if (container) {
            const rect = container.getBoundingClientRect();
            setEditingNode(node.id);
            setEditingName(node.name);
            setEditingPosition({ x: node.x + rect.left, y: node.y + rect.top });
        }
    }, []);

    const handleNodeRename = useCallback(async (newName) => {
        if (!editingNode || !newName.trim() || !selectedGraph) {
            setEditingNode(null);
            return;
        }
        try {
            await api.updateNode(selectedGraph.id, editingNode, newName.trim());
            setNodes(prev => prev.map(n =>
                n.id === editingNode ? { ...n, name: newName.trim() } : n
            ));
            setStatus(`Renamed node to: ${newName.trim()}`);
        } catch (err) {
            setError('Failed to rename node');
        }
        setEditingNode(null);
    }, [editingNode, selectedGraph]);

    const handleEdgeCreate = useCallback(async (sourceId, targetId) => {
        if (!selectedGraph) return;
        const edgeExists = edges.some(e =>
            (e.source === sourceId || e.source?.id === sourceId) &&
            (e.target === targetId || e.target?.id === targetId)
        );
        if (edgeExists) {
            setStatus('Edge already exists');
            return;
        }
        try {
            await api.addEdge(selectedGraph.id, sourceId, targetId);
            setEdges(prev => [...prev, { source: sourceId, target: targetId }]);
            const sourceNode = nodes.find(n => n.id === sourceId);
            const targetNode = nodes.find(n => n.id === targetId);
            setStatus(`Created edge: ${sourceNode?.name} -> ${targetNode?.name}`);
        } catch (err) {
            setError('Failed to create edge');
        }
    }, [selectedGraph, edges, nodes]);

    const handleDeleteNode = useCallback(async (nodeId) => {
        if (!selectedGraph) return;
        const nodeToDelete = nodes.find(n => n.id === nodeId);
        if (!nodeToDelete) return;
        try {
            await api.deleteNode(selectedGraph.id, nodeId);
            setNodes(prev => prev.filter(n => n.id !== nodeId));
            setEdges(prev => prev.filter(e => {
                const sourceId = e.source?.id || e.source;
                const targetId = e.target?.id || e.target;
                return sourceId !== nodeId && targetId !== nodeId;
            }));
            setStatus(`Deleted node: ${nodeToDelete.name}`);
        } catch (err) {
            setError('Failed to delete node');
        }
    }, [selectedGraph, nodes]);

    return (
        <div className="graph-editor">
            <header className="header">
                <h1>Graph Editor</h1>
                <span>React + D3 Visualization</span>
            </header>
            <div className="main-content">
                <aside className="sidebar">
                    {error && <div className="error">{error}</div>}

                    <div className="instructions">
                        <strong>How to use:</strong>
                        <ul>
                            <li>Create or select a graph</li>
                            <li><strong>Click canvas</strong> to create a node</li>
                            <li>Select a mode below to interact with nodes</li>
                            <li>Scroll to zoom, right-drag to pan</li>
                        </ul>
                    </div>

                    <div className="mode-toolbar">
                        <h2>Interaction Mode</h2>
                        <div className="mode-buttons">
                            <button
                                className={`btn btn-mode ${interactionMode === 'rename' ? 'active' : ''}`}
                                onClick={() => setInteractionMode('rename')}
                                title="Click a node to rename it"
                            >
                                Rename
                            </button>
                            <button
                                className={`btn btn-mode ${interactionMode === 'link' ? 'active' : ''}`}
                                onClick={() => setInteractionMode('link')}
                                title="Drag from one node to another to create an edge"
                            >
                                Link Edge
                            </button>
                            <button
                                className={`btn btn-mode ${interactionMode === 'move' ? 'active' : ''}`}
                                onClick={() => setInteractionMode('move')}
                                title="Drag the graph to reposition"
                            >
                                Move
                            </button>
                            <button
                                className={`btn btn-mode ${interactionMode === 'delete' ? 'active' : ''}`}
                                onClick={() => setInteractionMode('delete')}
                                title="Click a node to delete it and its edges"
                            >
                                Delete
                            </button>
                        </div>
                    </div>

                    <h2>Create New Graph</h2>
                    <div className="form-group">
                        <label>Graph Name</label>
                        <input
                            type="text"
                            value={newGraphName}
                            onChange={e => setNewGraphName(e.target.value)}
                            placeholder="Enter graph name"
                            onKeyPress={e => e.key === 'Enter' && handleCreateGraph()}
                        />
                    </div>
                    <button className="btn btn-primary" onClick={handleCreateGraph}>
                        Create Graph
                    </button>

                    <div className="graph-list">
                        <h2>Existing Graphs</h2>
                        {graphs.map(graph => (
                            <div
                                key={graph.id}
                                className={`graph-item ${selectedGraph?.id === graph.id ? 'active' : ''}`}
                                onClick={() => handleSelectGraph(graph)}
                            >
                                <span className="graph-item-name">{graph.name}</span>
                                <button
                                    className="btn btn-danger"
                                    onClick={e => {
                                        e.stopPropagation();
                                        handleDeleteGraph(graph.id);
                                    }}
                                    style={{ padding: '4px 8px', fontSize: '0.7rem' }}
                                >
                                    Delete
                                </button>
                            </div>
                        ))}
                    </div>

                    {selectedGraph && (
                        <>
                            <div className="node-list">
                                <h2>Add Node</h2>
                                <div className="form-group">
                                    <label>Node Name</label>
                                    <input
                                        type="text"
                                        value={newNodeName}
                                        onChange={e => setNewNodeName(e.target.value)}
                                        placeholder="Enter node name"
                                        onKeyPress={e => e.key === 'Enter' && handleCreateNode()}
                                    />
                                </div>
                                <button className="btn btn-primary" onClick={handleCreateNode}>
                                    Add Node
                                </button>
                            </div>

                            {nodes.length >= 2 && (
                                <div className="edge-form">
                                    <h2>Add Edge</h2>
                                    <div className="form-group">
                                        <label>From Node</label>
                                        <select value={edgeFrom} onChange={e => setEdgeFrom(e.target.value)}>
                                            <option value="">Select node...</option>
                                            {nodes.map(n => (
                                                <option key={n.id} value={n.id}>{n.name}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="form-group">
                                        <label>To Node</label>
                                        <select value={edgeTo} onChange={e => setEdgeTo(e.target.value)}>
                                            <option value="">Select node...</option>
                                            {nodes.map(n => (
                                                <option key={n.id} value={n.id}>{n.name}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <button className="btn btn-primary" onClick={handleAddEdge}>
                                        Add Edge
                                    </button>
                                </div>
                            )}

                            <div className="node-list" style={{ marginTop: '20px' }}>
                                <h2>Nodes ({nodes.length})</h2>
                                {nodes.map(node => (
                                    <div key={node.id} className="node-item">
                                        <span>{node.name}</span>
                                        <span style={{ color: '#7f8c8d', fontSize: '0.7rem' }}>
                                            {node.id.substring(0, 8)}...
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </>
                    )}
                </aside>
                <main className="graph-container" ref={graphContainerRef}>
                    {loading ? (
                        <div className="loading">Loading...</div>
                    ) : selectedGraph ? (
                        <GraphVisualization
                            nodes={nodes}
                            edges={edges}
                            selectedNode={selectedNode}
                            editingNode={editingNode}
                            interactionMode={interactionMode}
                            onNodeClick={handleNodeClick}
                            onCanvasClick={handleCanvasClick}
                            onEdgeCreate={handleEdgeCreate}
                            onEditStart={handleEditStart}
                            onDeleteNode={handleDeleteNode}
                        />
                    ) : (
                        <div className="loading">
                            Select a graph to visualize or create a new one
                        </div>
                    )}
                    {editingNode && (
                        <input
                            type="text"
                            className="node-edit-input"
                            value={editingName}
                            onChange={(e) => setEditingName(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') {
                                    handleNodeRename(editingName);
                                } else if (e.key === 'Escape') {
                                    setEditingNode(null);
                                }
                            }}
                            onBlur={() => handleNodeRename(editingName)}
                            style={{
                                left: editingPosition.x,
                                top: editingPosition.y
                            }}
                            autoFocus
                        />
                    )}
                </main>
            </div>
            <footer className="status-bar">
                {status}
            </footer>
        </div>
    );
}

// Initialize the application
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<GraphEditor />);
