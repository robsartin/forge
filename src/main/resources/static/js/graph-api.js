/**
 * Graph API client functions
 */
const API_BASE = '';

// CSRF token cache
let csrfToken = null;
let csrfHeaderName = 'X-CSRF-TOKEN';

/**
 * Fetch CSRF token from server
 */
async function fetchCsrfToken() {
    if (csrfToken) return csrfToken;
    const res = await fetch('/api/csrf', { credentials: 'same-origin' });
    if (res.ok) {
        const data = await res.json();
        csrfToken = data.token;
        csrfHeaderName = data.headerName || 'X-CSRF-TOKEN';
    }
    return csrfToken;
}

/**
 * Get headers with CSRF token for state-changing requests
 */
async function getHeaders(includeJson = true) {
    const token = await fetchCsrfToken();
    const headers = {};
    if (includeJson) {
        headers['Content-Type'] = 'application/json';
    }
    if (token) {
        headers[csrfHeaderName] = token;
    }
    return headers;
}

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
            headers: await getHeaders(),
            body: JSON.stringify({ name }),
            credentials: 'same-origin'
        });
        return res.json();
    },

    /**
     * Delete a graph
     * @param {string} id - Graph ID
     */
    async deleteGraph(id) {
        await fetch(`${API_BASE}/graphs/${id}`, {
            method: 'DELETE',
            headers: await getHeaders(false),
            credentials: 'same-origin'
        });
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
            headers: await getHeaders(),
            body: JSON.stringify({ name }),
            credentials: 'same-origin'
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
            method: 'POST',
            headers: await getHeaders(false),
            credentials: 'same-origin'
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
            headers: await getHeaders(),
            body: JSON.stringify({ name }),
            credentials: 'same-origin'
        });
        return res.json();
    },

    /**
     * Delete a node and its edges
     * @param {string} graphId - Graph ID
     * @param {string} nodeId - Node ID
     */
    async deleteNode(graphId, nodeId) {
        await fetch(`${API_BASE}/graphs/${graphId}/nodes/${nodeId}`, {
            method: 'DELETE',
            headers: await getHeaders(false),
            credentials: 'same-origin'
        });
    },

    /**
     * Delete an edge between two nodes
     * @param {string} graphId - Graph ID
     * @param {string} fromId - Source node ID
     * @param {string} toId - Target node ID
     */
    async deleteEdge(graphId, fromId, toId) {
        await fetch(`${API_BASE}/graphs/${graphId}/nodes/${fromId}/${toId}`, {
            method: 'DELETE',
            headers: await getHeaders(false),
            credentials: 'same-origin'
        });
    },

    /**
     * Get graph metrics
     * @param {string} graphId - Graph ID
     * @returns {Promise<Object|null>} Graph metrics or null if not available
     */
    async getGraphMetrics(graphId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/metrics`);
        if (!res.ok) return null;
        return res.json();
    },

    /**
     * Get node metrics
     * @param {string} graphId - Graph ID
     * @param {string} nodeId - Node ID
     * @returns {Promise<Object|null>} Node metrics or null if not available
     */
    async getNodeMetrics(graphId, nodeId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/metrics/nodes/${nodeId}`);
        if (!res.ok) return null;
        return res.json();
    },

    /**
     * Get degree distribution
     * @param {string} graphId - Graph ID
     * @returns {Promise<Array>} Degree distribution data
     */
    async getDegreeDistribution(graphId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/metrics/distribution`);
        if (!res.ok) return [];
        return res.json();
    }
};

// Export for use in other scripts
window.graphApi = graphApi;
