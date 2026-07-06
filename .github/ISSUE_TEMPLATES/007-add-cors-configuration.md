# Add CORS configuration for external API consumers

**Labels:** enhancement, priority:medium

## Type
Feature

## Priority
**Medium**

## Description
No CORS configuration exists. External frontends or mobile apps cannot consume the API due to browser same-origin policy restrictions.

## Current Behavior
- API requests from different origins are blocked
- No `Access-Control-Allow-Origin` headers
- Preflight requests fail

## Proposed Solution
Add CORS configuration via `WebMvcConfigurer`:

```java
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
    
    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/graphs/**")
            .allowedOrigins(allowedOrigins.toArray(new String[0]))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

### Configuration in application.yml
```yaml
app:
  cors:
    allowed-origins:
      - http://localhost:3000
      - https://your-frontend.com
```

## Files Affected
- New: `src/main/java/com/robsartin/graphs/config/CorsConfiguration.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-prod.yml`

## Acceptance Criteria
- [ ] CORS configuration implemented
- [ ] Allowed origins configurable via properties
- [ ] Preflight requests handled correctly
- [ ] Credentials supported for authenticated requests
- [ ] Tests verify CORS headers
