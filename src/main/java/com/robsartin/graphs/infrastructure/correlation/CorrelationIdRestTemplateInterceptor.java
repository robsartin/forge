package com.robsartin.graphs.infrastructure.correlation;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static com.robsartin.graphs.infrastructure.correlation.CorrelationIdContext.CORRELATION_ID_HEADER;

/**
 * RestTemplate interceptor that adds the correlation ID to outbound HTTP requests.
 * Use this interceptor when configuring RestTemplate beans to ensure correlation ID
 * propagation across service boundaries.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * @Bean
 * public RestTemplate restTemplate(CorrelationIdRestTemplateInterceptor interceptor) {
 *     RestTemplate restTemplate = new RestTemplate();
 *     restTemplate.getInterceptors().add(interceptor);
 *     return restTemplate;
 * }
 * }
 * </pre>
 */
public class CorrelationIdRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String correlationId = CorrelationIdContext.getCorrelationId();
        if (correlationId != null) {
            request.getHeaders().set(CORRELATION_ID_HEADER, correlationId);
        }
        return execution.execute(request, body);
    }
}
