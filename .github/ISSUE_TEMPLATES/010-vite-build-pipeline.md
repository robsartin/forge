# Add frontend build pipeline with Vite

**Labels:** enhancement, infrastructure, priority:medium

## Type
Infrastructure / Dependencies

## Priority
**Medium**

## Description
Currently, JSX files are served raw and transpiled in-browser with Babel. This is slow, lacks modern tooling benefits, and is not production-ready.

## Current Issues
- Babel runtime transpilation in browser (slow)
- No TypeScript support
- No minification or tree-shaking
- No hot module replacement during development
- Large bundle sizes (full React/D3 from CDN)

## Proposed Solution
Add a proper frontend build pipeline using Vite:

### Directory Structure
```
frontend/
├── package.json
├── vite.config.js
├── tsconfig.json
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── components/
│   │   ├── GraphVisualization.tsx
│   │   ├── Sidebar.tsx
│   │   └── ...
│   ├── api/
│   │   └── graphApi.ts
│   └── types/
│       └── graph.ts
└── public/
```

### Maven Integration
Use `frontend-maven-plugin` to:
1. Install Node.js
2. Run `npm install`
3. Run `npm run build`
4. Copy output to `src/main/resources/static/dist/`

### Benefits
- TypeScript for type safety
- Fast HMR during development
- Optimized production builds
- Tree-shaking removes unused code
- Code splitting for faster initial load

## Files Affected
- New: `frontend/` directory with Vite project
- `pom.xml` - add frontend-maven-plugin
- `src/main/resources/static/graph-editor.html` - reference built files

## Acceptance Criteria
- [ ] Vite project set up with React + TypeScript
- [ ] Existing JSX code migrated to TypeScript
- [ ] Maven builds frontend during `mvn package`
- [ ] Development mode with HMR works
- [ ] Production build is minified and optimized
- [ ] All existing functionality preserved
