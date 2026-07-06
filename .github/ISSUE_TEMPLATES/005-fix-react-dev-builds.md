# Fix frontend using development React builds

**Labels:** bug, priority:medium

## Type
Bug

## Priority
**Medium**

## Description
The graph editor page loads development React builds and uses Babel runtime transpilation, which is slow and includes development warnings in production.

## Current Behavior
In `graph-editor.html` (lines 9-13):
```html
<script src="https://unpkg.com/react@18/umd/react.development.js"></script>
<script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js"></script>
<script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
```

Issues:
- Development builds are larger and slower
- Console warnings about development mode
- Babel transpilation happens in browser (slow)
- No minification

## Proposed Solution

### Short-term fix
Switch to production React builds:
```html
<script src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
<script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
```

### Long-term fix
See issue for "Add frontend build pipeline with Vite" - proper bundling and build process.

## Files Affected
- `src/main/resources/static/graph-editor.html`

## Acceptance Criteria
- [ ] React production builds used
- [ ] No development warnings in console
- [ ] Page load time improved
- [ ] Functionality unchanged
