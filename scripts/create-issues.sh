#!/bin/bash
# Run this script locally to create all 10 improvement issues
# Requires: gh CLI authenticated (gh auth login)

set -e

echo "Creating 10 improvement issues for robsartin/forge..."
echo ""

# Issue 1: Critical - Security fix
echo "Creating issue 1/10: SECURITY: Fix dev user credentials enabled by default"
gh issue create \
  --title "SECURITY: Fix dev user credentials enabled by default" \
  --label "bug" --label "security" \
  --body "## Type
Bug / Security

## Priority
**Critical**

## Description
In \`SecurityConfiguration.java\` (line 30), the dev user is enabled by default:
\`\`\`java
@Value(\"\${app.dev-user.enabled:true}\")
\`\`\`

This enables development credentials (\`dev/dev\`) by default, which is a **security risk** if deployed without explicit configuration.

## Proposed Solution
1. Change default to \`false\`: \`\${app.dev-user.enabled:false}\`
2. Add warning log when dev user is enabled
3. Document in README that this is for development only

## Files Affected
- \`src/main/java/com/robsartin/graphs/config/SecurityConfiguration.java\`

## Acceptance Criteria
- [ ] Default value changed to \`false\`
- [ ] Warning log emitted when dev user is enabled
- [ ] README updated with security note
- [ ] Tests updated to explicitly enable dev user where needed"

# Issue 2: High - N+1 API calls
echo "Creating issue 2/10: Fix N+1 API calls in frontend graph loading"
gh issue create \
  --title "Fix N+1 API calls in frontend graph loading" \
  --label "bug" --label "performance" \
  --body "## Type
Bug / Performance

## Priority
**High**

## Description
The \`loadGraphData\` function in \`graph-editor-app.jsx\` makes one API call per node to fetch edges, causing severe performance issues for graphs with many nodes.

## Current Behavior
For a graph with 100 nodes, this results in 101 API calls (1 for nodes + 100 for edges).

## Proposed Solution
Add \`GET /graphs/{id}/full\` returning complete graph structure:
\`\`\`json
{
  \"id\": \"...\",
  \"name\": \"...\",
  \"nodes\": [...],
  \"edges\": [{\"source\": \"...\", \"target\": \"...\"}]
}
\`\`\`

## Files Affected
- \`src/main/resources/static/js/graph-editor-app.jsx\`
- \`src/main/java/com/robsartin/graphs/application/GraphController.java\`

## Acceptance Criteria
- [ ] New endpoint returns full graph data in single call
- [ ] Frontend updated to use new endpoint
- [ ] Graph loading time reduced by ~90% for large graphs
- [ ] Tests added for new endpoint"

# Issue 3: High - Pagination
echo "Creating issue 3/10: Add pagination to graph and node list endpoints"
gh issue create \
  --title "Add pagination to graph and node list endpoints" \
  --label "enhancement" \
  --body "## Type
Feature

## Priority
**High**

## Description
The \`GET /graphs\` and \`GET /graphs/{id}/nodes\` endpoints return all results without pagination.

## Proposed Solution
Add pagination support using Spring Data's \`Pageable\`:
- \`page\` (default: 0) - Page number
- \`size\` (default: 20, max: 100) - Items per page

### Response Format
\`\`\`json
{
  \"content\": [...],
  \"page\": 0,
  \"size\": 20,
  \"totalElements\": 150,
  \"totalPages\": 8
}
\`\`\`

## Acceptance Criteria
- [ ] \`GET /graphs\` supports pagination
- [ ] \`GET /graphs/{id}/nodes\` supports pagination
- [ ] Default page size is 20, max is 100
- [ ] Response includes pagination metadata
- [ ] Tests cover pagination scenarios"

# Issue 4: High - Exception handler
echo "Creating issue 4/10: Add global exception handler with structured error responses"
gh issue create \
  --title "Add global exception handler with structured error responses" \
  --label "enhancement" \
  --body "## Type
Feature

## Priority
**High**

## Description
Currently, exceptions like \`IllegalArgumentException\` return HTTP 500 with no body. API consumers need consistent, structured error responses.

## Proposed Solution
Create a \`@ControllerAdvice\` class with \`@ExceptionHandler\` methods.

### Error Response Format
\`\`\`json
{
  \"error\": \"BAD_REQUEST\",
  \"message\": \"Both nodes must exist in the graph\",
  \"timestamp\": \"2026-07-06T10:30:00Z\",
  \"path\": \"/graphs/123/nodes/456/789\"
}
\`\`\`

## Acceptance Criteria
- [ ] Global exception handler created
- [ ] \`IllegalArgumentException\` → 400 Bad Request
- [ ] \`EntityNotFoundException\` → 404 Not Found
- [ ] All errors include timestamp and path
- [ ] Correlation ID included in error responses"

# Issue 5: Medium - React dev builds
echo "Creating issue 5/10: Fix frontend using development React builds"
gh issue create \
  --title "Fix frontend using development React builds" \
  --label "bug" \
  --body "## Type
Bug

## Priority
**Medium**

## Description
The graph editor page loads development React builds and uses Babel runtime transpilation.

## Current Behavior
\`\`\`html
<script src=\"https://unpkg.com/react@18/umd/react.development.js\"></script>
\`\`\`

## Proposed Solution
Switch to production React builds:
\`\`\`html
<script src=\"https://unpkg.com/react@18/umd/react.production.min.js\"></script>
\`\`\`

## Acceptance Criteria
- [ ] React production builds used
- [ ] No development warnings in console
- [ ] Page load time improved"

# Issue 6: Medium - Database indexes
echo "Creating issue 6/10: Add missing database indexes on foreign keys"
gh issue create \
  --title "Add missing database indexes on foreign keys" \
  --label "bug" --label "performance" \
  --body "## Type
Bug / Performance

## Priority
**Medium**

## Description
Foreign key columns lack indexes, causing slow JOIN queries on large graphs.

## Proposed Solution
Create migration \`V2__add_foreign_key_indexes.sql\`:
\`\`\`sql
CREATE INDEX idx_graph_nodes_graph_id ON graph_nodes(graph_id);
CREATE INDEX idx_graph_edges_from_node ON graph_edges(from_node_id);
CREATE INDEX idx_graph_edges_to_node ON graph_edges(to_node_id);
\`\`\`

## Acceptance Criteria
- [ ] Migration file created
- [ ] Indexes created on all foreign key columns
- [ ] Query performance improved"

# Issue 7: Medium - CORS
echo "Creating issue 7/10: Add CORS configuration for external API consumers"
gh issue create \
  --title "Add CORS configuration for external API consumers" \
  --label "enhancement" \
  --body "## Type
Feature

## Priority
**Medium**

## Description
No CORS configuration exists. External frontends cannot consume the API.

## Proposed Solution
Add CORS configuration via \`WebMvcConfigurer\` with configurable allowed origins.

## Acceptance Criteria
- [ ] CORS configuration implemented
- [ ] Allowed origins configurable via properties
- [ ] Preflight requests handled correctly
- [ ] Tests verify CORS headers"

# Issue 8: Medium - Export/Import
echo "Creating issue 8/10: Add graph export/import functionality (JSON format)"
gh issue create \
  --title "Add graph export/import functionality (JSON format)" \
  --label "enhancement" \
  --body "## Type
Feature

## Priority
**Medium**

## Description
Users cannot backup, share, or migrate graphs.

## Proposed Solution
- \`GET /graphs/{id}/export\` - Export graph as JSON
- \`POST /graphs/import\` - Import graph from JSON

## Acceptance Criteria
- [ ] Export endpoint returns full graph as JSON
- [ ] Import endpoint creates graph from JSON
- [ ] Import generates new UUIDs
- [ ] Frontend has export/import buttons"

# Issue 9: Medium - Spring Boot upgrade
echo "Creating issue 9/10: Upgrade Spring Boot to latest 3.5.x/3.6.x"
gh issue create \
  --title "Upgrade Spring Boot to latest 3.5.x/3.6.x" \
  --label "dependencies" \
  --body "## Type
Dependency Update

## Priority
**Medium**

## Description
Current version: **Spring Boot 3.4.1** (December 2024)

Newer versions include security patches, performance improvements, and new features.

## Tasks
- [ ] Check latest stable Spring Boot version
- [ ] Review release notes for breaking changes
- [ ] Update \`spring-boot-starter-parent\` in pom.xml
- [ ] Update related dependencies (Resilience4j, SpringDoc, OpenTelemetry)
- [ ] Run full test suite
- [ ] Verify OAuth2 flow still works"

# Issue 10: Medium - Vite build pipeline
echo "Creating issue 10/10: Add frontend build pipeline with Vite"
gh issue create \
  --title "Add frontend build pipeline with Vite" \
  --label "enhancement" --label "infrastructure" \
  --body "## Type
Infrastructure

## Priority
**Medium**

## Description
Currently, JSX files are transpiled in-browser with Babel. This is slow and not production-ready.

## Proposed Solution
Add Vite + React + TypeScript build pipeline integrated with Maven via \`frontend-maven-plugin\`.

## Benefits
- TypeScript for type safety
- Fast HMR during development
- Optimized production builds
- Tree-shaking removes unused code

## Acceptance Criteria
- [ ] Vite project set up with React + TypeScript
- [ ] Existing JSX code migrated to TypeScript
- [ ] Maven builds frontend during \`mvn package\`
- [ ] Development mode with HMR works
- [ ] Production build is minified and optimized"

echo ""
echo "All 10 issues created successfully!"
echo "View them at: https://github.com/robsartin/forge/issues"
