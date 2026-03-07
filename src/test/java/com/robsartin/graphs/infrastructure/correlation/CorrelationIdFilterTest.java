package com.robsartin.graphs.infrastructure.correlation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorrelationIdFilter")
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void tearDown() {
        CorrelationIdContext.clear();
    }

    @Test
    @DisplayName("should generate correlation ID when none provided")
    void shouldGenerateCorrelationIdWhenNoneProvided() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        String responseCorrelationId = response.getHeader(CorrelationIdContext.CORRELATION_ID_HEADER);
        assertThat(responseCorrelationId).isNotNull();
        assertThat(CorrelationIdContext.isValidUuid(responseCorrelationId)).isTrue();
    }

    @Test
    @DisplayName("should use provided valid correlation ID")
    void shouldUseProvidedCorrelationId() throws Exception {
        String providedId = "550e8400-e29b-41d4-a716-446655440000";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.CORRELATION_ID_HEADER, providedId);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(CorrelationIdContext.CORRELATION_ID_HEADER)).isEqualTo(providedId);
    }

    @Test
    @DisplayName("should generate new ID when invalid correlation ID provided")
    void shouldGenerateNewIdWhenInvalidProvided() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdContext.CORRELATION_ID_HEADER, "not-a-valid-uuid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        String responseCorrelationId = response.getHeader(CorrelationIdContext.CORRELATION_ID_HEADER);
        assertThat(responseCorrelationId).isNotEqualTo("not-a-valid-uuid");
        assertThat(CorrelationIdContext.isValidUuid(responseCorrelationId)).isTrue();
    }

    @Test
    @DisplayName("should clear correlation ID after request completes")
    void shouldClearAfterRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(CorrelationIdContext.getCorrelationId()).isNull();
    }

    @Test
    @DisplayName("should add correlation ID to response header")
    void shouldAddToResponseHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(CorrelationIdContext.CORRELATION_ID_HEADER)).isNotNull();
    }
}
