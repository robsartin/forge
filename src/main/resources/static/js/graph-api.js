/**
 * Graph API client functions
 */
const API_BASE = '';

const graphApi = {
    /**
     * Get all graphs
     * @returns {Promise<Array>} List of graphs
     */
    async getGraphs() {
        const res = await fetch(`${API_BASE}/graphs`);
        return res.json();
    },

    /**
     * Create a new graph
     * @param {string} name - Graph name
     * @returns {Promise<Object>} Created graph
     */
    async createGraph(name) {
        const res = await fetch(`${API_BASE}/graphs`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });
        return res.json();
    },

    /**
     * Delete a graph
     * @param {string} id - Graph ID
     */
    async deleteGraph(id) {
        await fetch(`${API_BASE}/graphs/${id}`, { method: 'DELETE' });
    },

    /**
     * Get all nodes in a graph
     * @param {string} graphId - Graph ID
     * @returns {Promise<Array>} List of nodes
     */
    async getNodes(graphId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes`);
        return res.json();
    },

    /**
     * Create a new node in a graph
     * @param {string} graphId - Graph ID
     * @param {string} name - Node name
     * @returns {Promise<Object>} Created node
     */
    async createNode(graphId, name) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });
        return res.json();
    },

    /**
     * Add an edge between two nodes
     * @param {string} graphId - Graph ID
     * @param {string} fromId - Source node ID
     * @param {string} toId - Target node ID
     */
    async addEdge(graphId, fromId, toId) {
        await fetch(`${API_BASE}/graphs/${graphId}/nodes/${fromId}/${toId}`, {
            method: 'POST'
        });
    },

    /**
     * Get a node with its linked nodes
     * @param {string} graphId - Graph ID
     * @param {string} nodeId - Node ID
     * @returns {Promise<Object>} Node with links
     */
    async getNodeWithLinks(graphId, nodeId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes/${nodeId}`);
        return res.json();
    },

    /**
     * Update a node's name
     * @param {string} graphId - Graph ID
     * @param {string} nodeId - Node ID
     * @param {string} name - New node name
     * @returns {Promise<Object>} Updated node
     */
    async updateNode(graphId, nodeId, name) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes/${nodeId}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });
        return res.json();
    }
};

// Export for use in other scripts
window.graphApi = graphApi;
