# CLAUDE.md - AI Assistant Guide for Forge

**Last Updated:** 2025-11-29
**Repository:** robsartin/forge
**License:** Apache License 2.0

## Repository Overview

**Forge** is a trial/experimental project repository currently in its initial phase. The repository is set up with Java-specific configurations but does not yet contain implementation code.

### Current State
- **Status:** Active Development
- **Primary Language:** Java 17
- **Build Tools:** Maven
- **Dependencies:** Spring Boot 3.4.1, JPA, Caffeine, Resilience4J, OpenFeature
- **Test Framework:** JUnit 5, MockMvc, ArchUnit

### Repository Structure

```
forge/
├── .git/                   # Git version control
├── .gitignore             # Java-specific ignore patterns
├── LICENSE                # Apache License 2.0
├── README.md              # Brief project description
└── CLAUDE.md              # This file - AI assistant guide
```

## Technology Stack

## Spring Boot 
When modifying Spring Boot configuration (application.yml, application-test.yml), always verify that test profiles remain compatible. Run tests after any actuator, security, or datasource configuration changes.

When implementing Spring event-based features, prefer @EventListener over @TransactionalEventListener unless there is an explicit transactional requirement. Always verify lazy-loaded entity fields are initialized before event publication.

For any endpoint that accepts POST/PUT/DELETE requests in a Spring Security + React app, always include CSRF token handling. On the backend, configure CSRF token repository (CookieCsrfTokenRepository). On the frontend, read the XSRF-TOKEN cookie and send it as X-XSRF-TOKEN header.

## Workflow Rules

After making changes, always run `./mvnw test` (or the project's test command) before reporting completion. For frontend changes, verify the dev server compiles without errors.

### Detected from Configuration
- **Language:** Java (inferred from .gitignore)
- **Ignored Artifacts:** .class, .jar, .war, .ear, .log files

### Expected Future Structure
When code is added, typical Java project structures may include:
- `src/main/java/` - Main source code
- `src/main/resources/` - Application resources
- `src/test/java/` - Test code
- `src/test/resources/` - Test resources
- Build configuration files (pom.xml, build.gradle, etc.)

## Development Workflow

### Branch Strategy

**Current Branch:** `claude/claude-md-mikviqci5g9xcibv-01YHYJRGCycCTVUsUuTNJ94d`

**Branch Naming Conventions:**
- Feature branches: `feature/<description>`
- Bug fixes: `bugfix/<description>`
- AI assistant branches: `claude/<session-id>` (auto-generated)
- Hotfixes: `hotfix/<description>`

### Git Operations

**Critical Git Requirements:**

1. **Push Operations:**
   - Always use: `git push -u origin <branch-name>`
   - Branch names MUST start with `claude/` and end with matching session ID
   - Retry logic: If network errors occur, retry up to 4 times with exponential backoff (2s, 4s, 8s, 16s)
   - Never force push to main/master without explicit permission

2. **Fetch/Pull Operations:**
   - Prefer specific branches: `git fetch origin <branch-name>`
   - Use retry logic for network failures (same backoff as push)
   - For pulls: `git pull origin <branch-name>`

3. **Commit Guidelines:**
   - Use clear, descriptive commit messages
   - Follow conventional commits format (optional but recommended):
     - `feat:` for new features
     - `fix:` for bug fixes
     - `docs:` for documentation
     - `refactor:` for code refactoring
     - `test:` for test additions/changes
     - `chore:` for maintenance tasks

### Working with GitHub

**Note:** The GitHub CLI (`gh`) is not available in this environment.

For GitHub operations:
- Request issue details directly from the user
- Request PR information from the user
- Use git commands for all version control operations

## Code Conventions

### Java Conventions (When Implemented)

**Style Guidelines:**
- Follow standard Java naming conventions
- Package names: lowercase (e.g., `com.example.forge`)
- Class names: PascalCase (e.g., `UserManager`)
- Method names: camelCase (e.g., `getUserById`)
- Methods short, preferably less than 20 lines
- Use 
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- Never use @Autowired fields. Do DI with constructor.

**Documentation:**
- Add Javadoc comments for public classes and methods
- Include @param, @return, and @throws tags as appropriate
- Keep comments concise and meaningful

**Error Handling:**
- Validate inputs at system boundaries only
- Don't add defensive code for scenarios that can't happen
- Use specific exception types rather than generic Exception

**Testing:**
- Write unit tests for business logic
- Integration tests for component interactions
- Follow arrange-act-assert pattern
- Use descriptive test method names
- Write test before implementing code
- Make test independent of implemenation as much as possible

### General Development Principles

**Simplicity First:**
- Avoid over-engineering solutions
- Only implement what is explicitly requested
- Don't add features or refactoring beyond the task scope
- Three similar lines are better than premature abstraction

**Code Quality:**
- Remove unused code completely (no commented-out code)
- No backwards-compatibility hacks for unused features
- Keep security in mind (avoid SQL injection, XSS, command injection, etc.)
- Validate at boundaries, trust internal code

**File Operations:**
- Prefer editing existing files over creating new ones
- Always read files before editing them
- Use absolute paths, not relative paths
- Don't create documentation files unless explicitly requested

## Security Considerations

### OWASP Top 10 Awareness
When implementing code, be vigilant about:
- SQL Injection
- Cross-Site Scripting (XSS)
- Command Injection
- Insecure Deserialization
- Authentication/Authorization issues
- Security Misconfiguration
- Sensitive Data Exposure

### Dependency Security
- Keep dependencies up to date
- Review security advisories for used libraries
- Avoid dependencies with known vulnerabilities

## Build and Test Workflow

The project uses Maven as the build system.

### Common Commands
- Build: `mvn clean compile`
- Run Tests: `mvn test`
- Package: `mvn package`

### Address any failures
If tests fail, investigate logs and fix the root cause before committing.

**Expected workflow:**
1. Build the project
2. Run tests
3. Check for compilation errors
4. Address any failures
5. Commit changes only when tests pass

**Retry Logic for Flaky Tests:**
- Investigate the root cause before retrying
- Don't mask real failures with retries
- Document known flaky tests

## AI Assistant Specific Guidelines

### Tool Usage
- Use Read tool before Edit/Write operations
- Use specialized tools over bash commands for file operations
- Prefer parallel tool calls when operations are independent
- Use Task tool for complex, multi-step operations

### Communication
- Be concise and direct in responses
- Avoid emojis unless explicitly requested
- Output text directly, not via echo or comments
- Focus on facts over validation

### Task Management
- Use TodoWrite for multi-step or complex tasks
- Mark tasks in_progress before starting work
- Mark tasks completed immediately after finishing
- Only one task should be in_progress at a time
- Remove irrelevant tasks from the list

### Planning
- Provide concrete implementation steps without time estimates
- Break work into actionable steps
- Let users decide scheduling
- Ask for clarification on ambiguous requirements

### Code Changes
- Always read files before modifying them
- Understand existing code before suggesting changes
- Only make requested changes, avoid scope creep
- Test changes when possible
- Commit with clear messages

## Project-Specific Notes

### Current Status
This is a greenfield project. When adding new functionality:
1. Establish project structure first (src directories, build config)
2. Choose and configure build tool (Maven/Gradle)
3. Set up testing framework (JUnit 5 recommended)
4. Add dependencies as needed
5. Implement functionality iteratively

### Future Considerations
As the project evolves, this document should be updated to reflect:
- Chosen build system (Maven/Gradle/etc.)
- Dependency management approach
- Testing strategy and frameworks
- Package/module structure
- Deployment procedures
- Environment-specific configurations

## Quick Reference

### Common Commands

```bash
# Check repository status
git status

# Create and checkout new branch
git checkout -b feature/my-feature

# Add and commit changes
git add .
git commit -m "feat: add new feature"

# Push to remote (with retry logic built into workflow)
git push -u origin <branch-name>

# View commit history
git log --oneline --graph

# Check for uncommitted changes
git diff
```

### File Locations
- Source code: (To be established)
- Tests: (To be established)
- Configuration: (To be established)
- Documentation: `README.md`, `CLAUDE.md`

## Contact and Support

For questions or issues related to this repository:
- Repository owner: robsartin
- Create issues in the GitHub repository
- Review existing documentation in README.md

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2025-11-29 | 1.0.0 | Initial CLAUDE.md creation, documented repository skeleton state |

---

**Note to AI Assistants:** This repository is in early stages. When implementing new features, establish proper project structure and conventions. Update this document as the project evolves. Always prioritize simplicity and clarity over premature optimization.
