const API_BASE = '';

let csrfToken = null;
let csrfHeaderName = 'X-CSRF-TOKEN';

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

export const graphApi = {
    async getGraphs() {
        const res = await fetch(`${API_BASE}/graphs`);
        return res.json();
    },

    async createGraph(name) {
        const res = await fetch(`${API_BASE}/graphs`, {
            method: 'POST',
            headers: await getHeaders(),
            body: JSON.stringify({ name }),
            credentials: 'same-origin'
        });
        return res.json();
    },

    async deleteGraph(id) {
        await fetch(`${API_BASE}/graphs/${id}`, {
            method: 'DELETE',
            headers: await getHeaders(false),
            credentials: 'same-origin'
        });
    },

    async getNodes(graphId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes`);
        return res.json();
    },

    async createNode(graphId, name) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes`, {
            method: 'POST',
            headers: await getHeaders(),
            body: JSON.stringify({ name }),
            credentials: 'same-origin'
        });
        return res.json();
    },

    async addEdge(graphId, fromId, toId) {
        await fetch(`${API_BASE}/graphs/${graphId}/nodes/${fromId}/${toId}`, {
            method: 'POST',
            headers: await getHeaders(false),
            credentials: 'same-origin'
        });
    },

    async getNodeWithLinks(graphId, nodeId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes/${nodeId}`);
        return res.json();
    },

    async updateNode(graphId, nodeId, name) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/nodes/${nodeId}`, {
            method: 'PATCH',
            headers: await getHeaders(),
            body: JSON.stringify({ name }),
            credentials: 'same-origin'
        });
        return res.json();
    },

    async deleteNode(graphId, nodeId) {
        await fetch(`${API_BASE}/graphs/${graphId}/nodes/${nodeId}`, {
            method: 'DELETE',
            headers: await getHeaders(false),
            credentials: 'same-origin'
        });
    },

    async deleteEdge(graphId, fromId, toId) {
        await fetch(`${API_BASE}/graphs/${graphId}/nodes/${fromId}/${toId}`, {
            method: 'DELETE',
            headers: await getHeaders(false),
            credentials: 'same-origin'
        });
    },

    async getGraphMetrics(graphId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/metrics`);
        if (!res.ok) return null;
        return res.json();
    },

    async getNodeMetrics(graphId, nodeId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/metrics/nodes/${nodeId}`);
        if (!res.ok) return null;
        return res.json();
    },

    async getDegreeDistribution(graphId) {
        const res = await fetch(`${API_BASE}/graphs/${graphId}/metrics/distribution`);
        if (!res.ok) return [];
        return res.json();
    }
};

export default graphApi;
