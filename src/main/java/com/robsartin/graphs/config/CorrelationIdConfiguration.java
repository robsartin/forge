package com.robsartin.graphs.config;

import com.robsartin.graphs.infrastructure.correlation.CorrelationIdRestTemplateInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for correlation ID propagation in HTTP clients.
 * Provides pre-configured HTTP clients that automatically include
 * the correlation ID in outbound requests.
 *
 * <p>For WebClient (if spring-webflux is added as a dependency):
 * <pre>
 * {@code
 * @Bean
 * public WebClient webClient() {
 *     return WebClient.builder()
 *         .filter((request, next) -> {
 *             String correlationId = CorrelationIdContext.getCorrelationId();
 *             if (correlationId != null) {
 *                 return next.exchange(
 *                     ClientRequest.from(request)
 *                         .header(CorrelationIdContext.CORRELATION_ID_HEADER, correlationId)
 *                         .build());
 *             }
 *             return next.exchange(request);
 *         })
 *         .build();
 * }
 * }
 * </pre>
 */
@Configuration
public class CorrelationIdConfiguration {

    @Bean
    public CorrelationIdRestTemplateInterceptor correlationIdRestTemplateInterceptor() {
        return new CorrelationIdRestTemplateInterceptor();
    }

    /**
     * Provides a RestTemplate with correlation ID propagation enabled.
     * All outbound requests made through this RestTemplate will automatically
     * include the Correlation-ID header.
     *
     * @param builder the RestTemplateBuilder injected by Spring
     * @param interceptor the correlation ID interceptor
     * @return a configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     CorrelationIdRestTemplateInterceptor interceptor) {
        return builder
                .additionalInterceptors(interceptor)
                .build();
    }
}
