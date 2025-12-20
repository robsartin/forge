/**
 * D3 Graph Visualization Component
 * Renders an interactive force-directed graph using D3.js
 */
function GraphVisualization({ nodes, edges, selectedNode, editingNode, onNodeClick, onCanvasClick, onEdgeCreate, onEditStart }) {
    const { useRef, useEffect } = React;

    const svgRef = useRef(null);
    const simulationRef = useRef(null);
    const dragLineRef = useRef(null);
    const dragSourceRef = useRef(null);
    const transformRef = useRef(d3.zoomIdentity);
    const nodePositionsRef = useRef(new Map());

    useEffect(() => {
        if (!svgRef.current) return;

        const svg = d3.select(svgRef.current);
        const width = svgRef.current.clientWidth;
        const height = svgRef.current.clientHeight;

        // Preserve positions from previous render
        nodes.forEach(n => {
            const storedPos = nodePositionsRef.current.get(n.id);
            if (storedPos) {
                n.x = storedPos.x;
                n.y = storedPos.y;
            }
        });

        svg.selectAll('*').remove();

        // Add arrow marker definition
        const defs = svg.append('defs');
        defs.append('marker')
            .attr('id', 'arrowhead')
            .attr('viewBox', '-0 -5 10 10')
            .attr('refX', 30)
            .attr('refY', 0)
            .attr('orient', 'auto')
            .attr('markerWidth', 8)
            .attr('markerHeight', 8)
            .append('path')
            .attr('d', 'M 0,-5 L 10,0 L 0,5')
            .attr('class', 'link-arrow');

        const g = svg.append('g');

        // Add drag line (initially hidden)
        const dragLine = g.append('line')
            .attr('class', 'drag-line')
            .style('display', 'none');
        dragLineRef.current = dragLine;

        // Add zoom behavior
        const zoom = d3.zoom()
            .scaleExtent([0.1, 4])
            .filter((event) => {
                return event.type === 'wheel' || event.button === 2;
            })
            .on('zoom', (event) => {
                transformRef.current = event.transform;
                g.attr('transform', event.transform);
            });
        svg.call(zoom);

        // Handle canvas click to create node
        svg.on('click', (event) => {
            if (event.target === svgRef.current) {
                const [x, y] = d3.pointer(event, g.node());
                onCanvasClick(x, y);
            }
        });

        // Check if we have stored positions
        const hasStoredPositions = nodes.some(n => nodePositionsRef.current.has(n.id));

        // Create force simulation
        const simulation = d3.forceSimulation(nodes)
            .force('link', d3.forceLink(edges).id(d => d.id).distance(150))
            .force('charge', d3.forceManyBody().strength(-400))
            .force('center', d3.forceCenter(width / 2, height / 2))
            .force('collision', d3.forceCollide().radius(50));

        // Reduce simulation intensity if positions exist
        if (hasStoredPositions) {
            simulation.alpha(0.1).alphaDecay(0.05);
        }

        simulationRef.current = simulation;

        // Draw edges
        const link = g.append('g')
            .selectAll('line')
            .data(edges)
            .enter()
            .append('line')
            .attr('class', 'link')
            .attr('marker-end', 'url(#arrowhead)');

        // Helper function to find node at position
        const findNodeAtPosition = (x, y, excludeId) => {
            const nodeRadius = 25;
            for (const n of nodes) {
                if (n.id === excludeId) continue;
                const dx = n.x - x;
                const dy = n.y - y;
                if (Math.sqrt(dx * dx + dy * dy) <= nodeRadius) {
                    return n;
                }
            }
            return null;
        };

        // Draw nodes
        const node = g.append('g')
            .selectAll('.node')
            .data(nodes)
            .enter()
            .append('g')
            .attr('class', d => `node ${selectedNode === d.id ? 'selected' : ''} ${editingNode === d.id ? 'editing' : ''}`)
            .call(d3.drag()
                .on('start', (event, d) => {
                    if (!event.active) simulation.alphaTarget(0.3).restart();
                    d.fx = d.x;
                    d.fy = d.y;
                    dragSourceRef.current = d;
                    dragLine
                        .style('display', null)
                        .attr('x1', d.x)
                        .attr('y1', d.y)
                        .attr('x2', d.x)
                        .attr('y2', d.y);
                    d3.select(event.sourceEvent.target.closest('.node')).classed('drag-source', true);
                })
                .on('drag', (event, d) => {
                    dragLine
                        .attr('x2', event.x)
                        .attr('y2', event.y);
                    const targetNode = findNodeAtPosition(event.x, event.y, d.id);
                    node.classed('drag-target', n => targetNode && n.id === targetNode.id);
                })
                .on('end', (event, d) => {
                    if (!event.active) simulation.alphaTarget(0);
                    d.fx = null;
                    d.fy = null;
                    dragLine.style('display', 'none');
                    node.classed('drag-source', false);
                    node.classed('drag-target', false);
                    const targetNode = findNodeAtPosition(event.x, event.y, d.id);
                    if (targetNode && dragSourceRef.current) {
                        onEdgeCreate(dragSourceRef.current.id, targetNode.id);
                    }
                    dragSourceRef.current = null;
                })
            )
            .on('click', (event, d) => {
                event.stopPropagation();
                if (editingNode === d.id) return;
                if (event.defaultPrevented) return;
                onEditStart(d);
            });

        node.append('circle')
            .attr('r', 25);

        node.append('text')
            .text(d => d.name.length > 8 ? d.name.substring(0, 7) + '...' : d.name);

        // Update positions on each tick
        simulation.on('tick', () => {
            link
                .attr('x1', d => d.source.x)
                .attr('y1', d => d.source.y)
                .attr('x2', d => d.target.x)
                .attr('y2', d => d.target.y);

            node.attr('transform', d => `translate(${d.x},${d.y})`);

            // Save positions for persistence
            nodes.forEach(n => {
                nodePositionsRef.current.set(n.id, { x: n.x, y: n.y });
            });
        });

        return () => simulation.stop();
    }, [nodes, edges, selectedNode, editingNode, onNodeClick, onCanvasClick, onEdgeCreate, onEditStart]);

    return <svg ref={svgRef} className="graph-svg" />;
}

// Export for use in other scripts
window.GraphVisualization = GraphVisualization;
