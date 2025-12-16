package com.robsartin.graphs.infrastructure.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.robsartin.graphs.infrastructure.correlation.CorrelationIdContext.CORRELATION_ID_HEADER;

/**
 * Servlet filter that extracts or generates a correlation ID for each incoming request.
 * The correlation ID is stored in the thread context and MDC for logging.
 * It is also added to the response headers for traceability.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String correlationId = extractOrGenerateCorrelationId(request);
            CorrelationIdContext.setCorrelationId(correlationId);

            // Add correlation ID to response headers for client visibility
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            log.debug("Request started with correlation ID: {}", correlationId);

            filterChain.doFilter(request, response);
        } finally {
            CorrelationIdContext.clear();
        }
    }

    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (CorrelationIdContext.isValidUuid(correlationId)) {
            log.debug("Using provided correlation ID: {}", correlationId);
            return correlationId;
        }

        if (correlationId != null && !correlationId.isBlank()) {
            log.warn("Invalid correlation ID provided: '{}', generating new one", correlationId);
        }

        String generatedId = CorrelationIdContext.generateCorrelationId();
        log.debug("Generated new correlation ID: {}", generatedId);
        return generatedId;
    }
}
