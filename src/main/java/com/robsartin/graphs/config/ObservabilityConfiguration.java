package com.robsartin.graphs.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for observability features including metrics, tracing, and API documentation.
 */
@Configuration
public class ObservabilityConfiguration {

    /**
     * Enables the @Timed annotation for method-level timing metrics.
     * Without this bean, @Timed annotations on methods will be ignored.
     *
     * @param registry the meter registry for recording metrics
     * @return the configured TimedAspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Configures OpenAPI documentation with application metadata.
     *
     * @return the configured OpenAPI specification
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Graph Experiment API")
                        .version("0.1-SNAPSHOT")
                        .description("REST API for managing immutable and mutable graph structures. " +
                                "Supports graph creation, node management, edge operations, and graph traversals (DFS/BFS).")
                        .contact(new Contact()
                                .name("robsartin")
                                .url("https://github.com/robsartin/forge"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")));
    }
}
