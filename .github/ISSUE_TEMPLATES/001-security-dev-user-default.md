# SECURITY: Fix dev user credentials enabled by default

**Labels:** bug, security, priority:critical

## Type
Bug / Security

## Priority
**Critical**

## Description
In `SecurityConfiguration.java` (line 30), the dev user is enabled by default:
```java
@Value("${app.dev-user.enabled:true}")
```

This enables development credentials (`dev/dev`) by default, which is a **security risk** if deployed without explicit configuration.

## Current Behavior
- Dev user with credentials `dev/dev` is enabled unless explicitly disabled
- This could allow unauthorized access in production environments

## Proposed Solution
1. Change default to `false`: `${app.dev-user.enabled:false}`
2. Add warning log when dev user is enabled
3. Document in README that this is for development only

## Files Affected
- `src/main/java/com/robsartin/graphs/config/SecurityConfiguration.java`

## Acceptance Criteria
- [ ] Default value changed to `false`
- [ ] Warning log emitted when dev user is enabled
- [ ] README updated with security note
- [ ] Tests updated to explicitly enable dev user where needed
