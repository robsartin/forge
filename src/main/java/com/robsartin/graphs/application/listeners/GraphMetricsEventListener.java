package com.robsartin.graphs.application.listeners;

import com.robsartin.graphs.application.services.GraphMetricsComputationService;
import com.robsartin.graphs.events.GraphSavedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event listener that triggers graph metrics computation
 * when a graph is saved.
 */
@Component
public class GraphMetricsEventListener {

    private static final Logger log = LoggerFactory.getLogger(GraphMetricsEventListener.class);

    private final GraphMetricsComputationService metricsService;

    public GraphMetricsEventListener(GraphMetricsComputationService metricsService) {
        this.metricsService = metricsService;
    }

    @Async
    @EventListener
    public void handleGraphSavedEvent(GraphSavedEvent event) {
        log.info("Received GraphSavedEvent for graph {}", event.graphId());
        try {
            // Small delay to ensure transaction commits before we read
            Thread.sleep(100);
            metricsService.computeAndSaveMetrics(event.graphId());
            log.info("Completed metrics computation for graph {}", event.graphId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Metrics computation interrupted for graph {}", event.graphId());
        } catch (Exception e) {
            // Log but don't rethrow - metrics computation failure should not
            // affect the original graph save operation
            log.error("Failed to compute metrics for graph {}: {}",
                    event.graphId(), e.getMessage(), e);
        }
    }
}
