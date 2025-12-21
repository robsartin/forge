/**
 * D3 Graph Visualization Component
 * Renders an interactive force-directed graph using D3.js
 */
function GraphVisualization({ nodes, edges, selectedNode, editingNode, interactionMode, onNodeClick, onCanvasClick, onEdgeCreate, onEditStart, onDeleteNode }) {
    const { useRef, useEffect } = React;

    const svgRef = useRef(null);
    const simulationRef = useRef(null);
    const dragLineRef = useRef(null);
    const dragSourceRef = useRef(null);
    const transformRef = useRef(d3.zoomIdentity);
    const nodePositionsRef = useRef(new Map());
    const mouseDownRef = useRef(false);

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

        // Add drag line (initially hidden) - used in link mode
        const dragLine = g.append('line')
            .attr('class', 'drag-line')
            .style('display', 'none');
        dragLineRef.current = dragLine;

        // Add zoom behavior
        const zoom = d3.zoom()
            .scaleExtent([0.1, 4])
            .filter((event) => {
                // In move mode, allow left-click drag for panning
                if (interactionMode === 'move') {
                    return event.type === 'wheel' || event.button === 0 || event.button === 2;
                }
                // Otherwise, only wheel and right-click
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

        // Track mouse down state for freezing layout
        svg.on('mousedown', () => {
            mouseDownRef.current = true;
            if (simulationRef.current) {
                simulationRef.current.stop();
            }
        });

        svg.on('mouseup', () => {
            mouseDownRef.current = false;
            if (simulationRef.current) {
                simulationRef.current.alpha(0.1).restart();
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

        // Create drag behavior based on interaction mode
        const createDragBehavior = () => {
            if (interactionMode === 'link') {
                // Link mode: drag from node to node to create edge
                return d3.drag()
                    .on('start', (event, d) => {
                        simulation.stop();
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
                        if (!mouseDownRef.current) {
                            simulation.alpha(0.1).restart();
                        }
                    });
            } else if (interactionMode === 'move') {
                // Move mode: drag to reposition individual nodes
                return d3.drag()
                    .on('start', (event, d) => {
                        event.sourceEvent.stopPropagation();
                        simulation.stop();
                        d.fx = d.x;
                        d.fy = d.y;
                    })
                    .on('drag', (event, d) => {
                        d.fx = event.x;
                        d.fy = event.y;
                        d.x = event.x;
                        d.y = event.y;
                        // Update node position immediately
                        d3.select(event.sourceEvent.target.closest('.node'))
                            .attr('transform', `translate(${event.x},${event.y})`);
                        // Update connected edges
                        link.each(function(l) {
                            if (l.source.id === d.id) {
                                d3.select(this).attr('x1', event.x).attr('y1', event.y);
                            }
                            if (l.target.id === d.id) {
                                d3.select(this).attr('x2', event.x).attr('y2', event.y);
                            }
                        });
                        // Save position
                        nodePositionsRef.current.set(d.id, { x: event.x, y: event.y });
                    })
                    .on('end', (event, d) => {
                        d.fx = null;
                        d.fy = null;
                    });
            } else {
                // Other modes: no node dragging
                return d3.drag()
                    .on('start', () => {})
                    .on('drag', () => {})
                    .on('end', () => {});
            }
        };

        // Draw nodes
        const node = g.append('g')
            .selectAll('.node')
            .data(nodes)
            .enter()
            .append('g')
            .attr('class', d => {
                let classes = 'node';
                if (selectedNode === d.id) classes += ' selected';
                if (editingNode === d.id) classes += ' editing';
                if (interactionMode === 'delete') classes += ' delete-mode';
                if (interactionMode === 'move') classes += ' move-mode';
                return classes;
            })
            .call(createDragBehavior())
            .on('click', (event, d) => {
                event.stopPropagation();
                if (editingNode === d.id) return;
                if (event.defaultPrevented) return;

                if (interactionMode === 'rename') {
                    onEditStart(d);
                } else if (interactionMode === 'delete') {
                    onDeleteNode(d.id);
                }
            });

        node.append('circle')
            .attr('r', 25);

        node.append('text')
            .text(d => d.name.length > 8 ? d.name.substring(0, 7) + '...' : d.name);

        // Update positions on each tick
        simulation.on('tick', () => {
            // Don't update positions if mouse is down (frozen)
            if (mouseDownRef.current) return;

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
    }, [nodes, edges, selectedNode, editingNode, interactionMode, onNodeClick, onCanvasClick, onEdgeCreate, onEditStart, onDeleteNode]);

    return <svg ref={svgRef} className="graph-svg" />;
}

// Export for use in other scripts
window.GraphVisualization = GraphVisualization;
