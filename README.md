# Forge

Forge - A graph management trial project built with Spring Boot.

## Overview

Forge provides a REST API for managing graphs with nodes and edges, supporting depth-first and breadth-first traversals using an immutable graph data structure.

## Features

- Create, read, and delete graphs
- Add nodes to graphs
- Create edges between nodes
- Perform depth-first search (DFS) traversals
- Perform breadth-first search (BFS) traversals
- Immutable graph data structure for thread-safety

## API Endpoints

### Graphs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/graphs` | List all graphs (id, name) |
| GET | `/graphs/{id}` | Get a specific graph by ID |
| POST | `/graphs` | Create a new graph with a name |
| DELETE | `/graphs/{id}` | Delete a graph by ID |

### Nodes

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/graphs/{id}/nodes` | List all nodes in a graph |
| POST | `/graphs/{id}/nodes` | Create a new node in a graph |
| GET | `/graphs/{id}/nodes/{nodeId}` | Get a specific node by ID |

### Edges

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/graphs/{id}/nodes/{fromId}/{toId}` | Add an edge from node fromId to toId |

### Graph Traversals

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/graphs/{id}/dfs/{nodeId}` | Depth-first search from nodeId, returns list of visited nodes |
| GET | `/graphs/{id}/bfs/{nodeId}` | Breadth-first search from nodeId, returns list of visited nodes |

## Request/Response Examples

### Create a Graph

```bash
curl -X POST http://localhost:8080/graphs \
  -H "Content-Type: application/json" \
  -d '{"name": "My Graph"}'
```

Response:
```json
{"id": 1, "name": "My Graph"}
```

### Add Nodes

```bash
curl -X POST http://localhost:8080/graphs/1/nodes \
  -H "Content-Type: application/json" \
  -d '{"name": "Node A"}'
```

### Add Edge

```bash
curl -X POST http://localhost:8080/graphs/1/nodes/0/1
```

### Perform DFS

```bash
curl http://localhost:8080/graphs/1/dfs/0
```

Response:
```json
[{"id": 1, "name": "Node A"}, {"id": 2, "name": "Node B"}]
```

## Technology Stack

- Java 17
- Spring Boot 3.4.1
- Spring Data JPA
- H2 Database (in-memory)
- jMolecules DDD

## Building and Running

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## License

Apache License 2.0
