package com.robsartin.graphs.application;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the graph visualization editor web client.
 * Serves a React + D3 based graph editing interface.
 */
@RestController
@Tag(name = "Graph Editor", description = "Web-based graph visualization and editing interface")
public class GraphEditorController {

    @GetMapping(value = "/edit_graph", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "Graph Editor UI", description = "Returns the graph visualization editor web client built with React and D3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Graph editor page returned successfully")
    })
    public String getGraphEditor() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Graph Editor</title>
                    <script src="https://unpkg.com/react@18/umd/react.development.js" crossorigin></script>
                    <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js" crossorigin></script>
                    <script src="https://unpkg.com/d3@7/dist/d3.min.js"></script>
                    <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                            background: #f5f5f5;
                            min-height: 100vh;
                        }
                        .graph-editor {
                            display: flex;
                            flex-direction: column;
                            height: 100vh;
                        }
                        .header {
                            background: #2c3e50;
                            color: white;
                            padding: 16px 24px;
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                        }
                        .header h1 {
                            font-size: 1.5rem;
                            font-weight: 600;
                        }
                        .main-content {
                            display: flex;
                            flex: 1;
                            overflow: hidden;
                        }
                        .sidebar {
                            width: 300px;
                            background: white;
                            border-right: 1px solid #e0e0e0;
                            padding: 20px;
                            overflow-y: auto;
                        }
                        .sidebar h2 {
                            font-size: 1rem;
                            color: #333;
                            margin-bottom: 16px;
                        }
                        .form-group {
                            margin-bottom: 16px;
                        }
                        .form-group label {
                            display: block;
                            margin-bottom: 6px;
                            font-size: 0.875rem;
                            color: #555;
                        }
                        .form-group input, .form-group select {
                            width: 100%;
                            padding: 10px 12px;
                            border: 1px solid #ddd;
                            border-radius: 6px;
                            font-size: 0.875rem;
                        }
                        .form-group input:focus, .form-group select:focus {
                            outline: none;
                            border-color: #3498db;
                            box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
                        }
                        .btn {
                            padding: 10px 16px;
                            border: none;
                            border-radius: 6px;
                            cursor: pointer;
                            font-size: 0.875rem;
                            font-weight: 500;
                            transition: all 0.2s;
                        }
                        .btn-primary {
                            background: #3498db;
                            color: white;
                            width: 100%;
                        }
                        .btn-primary:hover {
                            background: #2980b9;
                        }
                        .btn-secondary {
                            background: #95a5a6;
                            color: white;
                            margin-top: 8px;
                            width: 100%;
                        }
                        .btn-secondary:hover {
                            background: #7f8c8d;
                        }
                        .btn-danger {
                            background: #e74c3c;
                            color: white;
                        }
                        .btn-danger:hover {
                            background: #c0392b;
                        }
                        .graph-container {
                            flex: 1;
                            background: white;
                            position: relative;
                        }
                        .graph-svg {
                            width: 100%;
                            height: 100%;
                        }
                        .node {
                            cursor: pointer;
                        }
                        .node circle {
                            fill: #3498db;
                            stroke: #2980b9;
                            stroke-width: 2px;
                            transition: all 0.2s;
                        }
                        .node:hover circle {
                            fill: #2980b9;
                            stroke: #1a5276;
                        }
                        .node.selected circle {
                            fill: #e74c3c;
                            stroke: #c0392b;
                        }
                        .node text {
                            font-size: 12px;
                            fill: white;
                            text-anchor: middle;
                            dominant-baseline: central;
                            pointer-events: none;
                            font-weight: 500;
                        }
                        .link {
                            stroke: #95a5a6;
                            stroke-width: 2px;
                            fill: none;
                        }
                        .link-arrow {
                            fill: #95a5a6;
                        }
                        .graph-list {
                            margin-top: 20px;
                            border-top: 1px solid #eee;
                            padding-top: 20px;
                        }
                        .graph-item {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            padding: 10px;
                            background: #f8f9fa;
                            border-radius: 6px;
                            margin-bottom: 8px;
                            cursor: pointer;
                            transition: all 0.2s;
                        }
                        .graph-item:hover {
                            background: #e9ecef;
                        }
                        .graph-item.active {
                            background: #d4edda;
                            border: 1px solid #28a745;
                        }
                        .graph-item-name {
                            font-size: 0.875rem;
                            font-weight: 500;
                        }
                        .node-list {
                            margin-top: 16px;
                        }
                        .node-item {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                            padding: 8px 10px;
                            background: #f8f9fa;
                            border-radius: 4px;
                            margin-bottom: 6px;
                            font-size: 0.8rem;
                        }
                        .edge-form {
                            margin-top: 20px;
                            padding-top: 20px;
                            border-top: 1px solid #eee;
                        }
                        .status-bar {
                            background: #34495e;
                            color: #bdc3c7;
                            padding: 8px 16px;
                            font-size: 0.75rem;
                        }
                        .loading {
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100%;
                            color: #7f8c8d;
                        }
                        .error {
                            color: #e74c3c;
                            padding: 10px;
                            background: #fce4e4;
                            border-radius: 6px;
                            margin-bottom: 16px;
                            font-size: 0.875rem;
                        }
                        .instructions {
                            background: #e8f4fd;
                            padding: 12px;
                            border-radius: 6px;
                            margin-bottom: 16px;
                            font-size: 0.8rem;
                            color: #2c3e50;
                        }
                        .instructions ul {
                            margin-left: 16px;
                            margin-top: 8px;
                        }
                    </style>
                </head>
                <body>
                    <div id="root"></div>
                    <script type="text/babel" data-type="module">
                        const { useState, useEffect, useRef, useCallback } = React;

                        const API_BASE = '';

                        // API functions
                        const api = {
                            async getGraphs() {
                                const res = await fetch(`${API_BASE}/graphs`);
                                return res.json();
                            },
                            async createGraph(name) {
                                const res = await fetch(`${API_BASE}/graphs`, {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ name })
                                });
                                return res.json();
                            },
                            async deleteGraph(id) {
                                await fetch(`${API_BASE}/graphs/${id}`, { method: 'DELETE' });
                            },
                            async getNodes(graphId) {
                                const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes`);
                                return res.json();
                            },
                            async createNode(graphId, name) {
                                const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes`, {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ name })
                                });
                                return res.json();
                            },
                            async addEdge(graphId, fromId, toId) {
                                await fetch(`${API_BASE}/graphs/${graphId}/nodes/${fromId}/${toId}`, {
                                    method: 'POST'
                                });
                            },
                            async getNodeWithLinks(graphId, nodeId) {
                                const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes/${nodeId}`);
                                return res.json();
                            }
                        };

                        // D3 Graph Visualization Component
                        function GraphVisualization({ nodes, edges, selectedNode, onNodeClick }) {
                            const svgRef = useRef(null);
                            const simulationRef = useRef(null);

                            useEffect(() => {
                                if (!svgRef.current) return;

                                const svg = d3.select(svgRef.current);
                                const width = svgRef.current.clientWidth;
                                const height = svgRef.current.clientHeight;

                                svg.selectAll('*').remove();

                                // Add arrow marker definition
                                svg.append('defs').append('marker')
                                    .attr('id', 'arrowhead')
                                    .attr('viewBox', '-0 -5 10 10')
                                    .attr('refX', 25)
                                    .attr('refY', 0)
                                    .attr('orient', 'auto')
                                    .attr('markerWidth', 8)
                                    .attr('markerHeight', 8)
                                    .append('path')
                                    .attr('d', 'M 0,-5 L 10,0 L 0,5')
                                    .attr('class', 'link-arrow');

                                const g = svg.append('g');

                                // Add zoom behavior
                                const zoom = d3.zoom()
                                    .scaleExtent([0.1, 4])
                                    .on('zoom', (event) => g.attr('transform', event.transform));
                                svg.call(zoom);

                                // Create force simulation
                                const simulation = d3.forceSimulation(nodes)
                                    .force('link', d3.forceLink(edges).id(d => d.id).distance(120))
                                    .force('charge', d3.forceManyBody().strength(-300))
                                    .force('center', d3.forceCenter(width / 2, height / 2))
                                    .force('collision', d3.forceCollide().radius(40));

                                simulationRef.current = simulation;

                                // Draw edges
                                const link = g.append('g')
                                    .selectAll('line')
                                    .data(edges)
                                    .enter()
                                    .append('line')
                                    .attr('class', 'link')
                                    .attr('marker-end', 'url(#arrowhead)');

                                // Draw nodes
                                const node = g.append('g')
                                    .selectAll('.node')
                                    .data(nodes)
                                    .enter()
                                    .append('g')
                                    .attr('class', d => `node ${selectedNode === d.id ? 'selected' : ''}`)
                                    .call(d3.drag()
                                        .on('start', (event, d) => {
                                            if (!event.active) simulation.alphaTarget(0.3).restart();
                                            d.fx = d.x;
                                            d.fy = d.y;
                                        })
                                        .on('drag', (event, d) => {
                                            d.fx = event.x;
                                            d.fy = event.y;
                                        })
                                        .on('end', (event, d) => {
                                            if (!event.active) simulation.alphaTarget(0);
                                            d.fx = null;
                                            d.fy = null;
                                        })
                                    )
                                    .on('click', (event, d) => {
                                        event.stopPropagation();
                                        onNodeClick(d.id);
                                    });

                                node.append('circle')
                                    .attr('r', 20);

                                node.append('text')
                                    .text(d => d.name.substring(0, 3).toUpperCase());

                                // Update positions on each tick
                                simulation.on('tick', () => {
                                    link
                                        .attr('x1', d => d.source.x)
                                        .attr('y1', d => d.source.y)
                                        .attr('x2', d => d.target.x)
                                        .attr('y2', d => d.target.y);

                                    node.attr('transform', d => `translate(${d.x},${d.y})`);
                                });

                                return () => simulation.stop();
                            }, [nodes, edges, selectedNode, onNodeClick]);

                            return <svg ref={svgRef} className="graph-svg" />;
                        }

                        // Main App Component
                        function GraphEditor() {
                            const [graphs, setGraphs] = useState([]);
                            const [selectedGraph, setSelectedGraph] = useState(null);
                            const [nodes, setNodes] = useState([]);
                            const [edges, setEdges] = useState([]);
                            const [selectedNode, setSelectedNode] = useState(null);
                            const [newGraphName, setNewGraphName] = useState('');
                            const [newNodeName, setNewNodeName] = useState('');
                            const [edgeFrom, setEdgeFrom] = useState('');
                            const [edgeTo, setEdgeTo] = useState('');
                            const [loading, setLoading] = useState(false);
                            const [error, setError] = useState(null);
                            const [status, setStatus] = useState('Ready');

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

                                    // Load edges for each node
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
                                                    <li>Add nodes to the graph</li>
                                                    <li>Create edges between nodes</li>
                                                    <li>Drag nodes to reposition</li>
                                                    <li>Scroll to zoom</li>
                                                </ul>
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
                                        <main className="graph-container">
                                            {loading ? (
                                                <div className="loading">Loading...</div>
                                            ) : selectedGraph ? (
                                                <GraphVisualization
                                                    nodes={nodes}
                                                    edges={edges}
                                                    selectedNode={selectedNode}
                                                    onNodeClick={handleNodeClick}
                                                />
                                            ) : (
                                                <div className="loading">
                                                    Select a graph to visualize or create a new one
                                                </div>
                                            )}
                                        </main>
                                    </div>
                                    <footer className="status-bar">
                                        {status}
                                    </footer>
                                </div>
                            );
                        }

                        const root = ReactDOM.createRoot(document.getElementById('root'));
                        root.render(<GraphEditor />);
                    </script>
                </body>
                </html>
                """;
    }
}
