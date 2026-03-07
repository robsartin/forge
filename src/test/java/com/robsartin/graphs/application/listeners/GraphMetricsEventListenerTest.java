package com.robsartin.graphs.application.listeners;

import com.robsartin.graphs.application.services.GraphMetricsComputationService;
import com.robsartin.graphs.events.GraphSavedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@DisplayName("GraphMetricsEventListener")
@ExtendWith(MockitoExtension.class)
class GraphMetricsEventListenerTest {

    @Mock
    private GraphMetricsComputationService metricsService;

    @InjectMocks
    private GraphMetricsEventListener listener;

    @Test
    @DisplayName("should invoke metrics computation for graph")
    void shouldInvokeMetricsComputation() {
        UUID graphId = UUID.randomUUID();
        GraphSavedEvent event = new GraphSavedEvent(graphId);

        listener.handleGraphSavedEvent(event);

        verify(metricsService).computeAndSaveMetrics(graphId);
    }

    @Test
    @DisplayName("should not propagate exception from metrics computation")
    void shouldNotPropagateException() {
        UUID graphId = UUID.randomUUID();
        GraphSavedEvent event = new GraphSavedEvent(graphId);
        doThrow(new RuntimeException("computation failed"))
                .when(metricsService).computeAndSaveMetrics(graphId);

        // Should not throw
        listener.handleGraphSavedEvent(event);

        verify(metricsService).computeAndSaveMetrics(graphId);
    }
}
