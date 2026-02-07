# Feature Implementation Skill

When implementing a new feature:
1. Identify all affected layers (controller, service, repository, frontend)
2. Check if the endpoint needs CSRF token handling
3. Check if any JPA entities involved have lazy-loaded fields that need initialization
4. Make all backend changes first, then frontend
5. Run `./mvnw test` and fix any failures
6. Verify frontend compiles with `npm run build`
7. Summarize all files changed
