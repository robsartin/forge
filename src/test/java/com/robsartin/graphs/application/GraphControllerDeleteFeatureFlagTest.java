package com.robsartin.graphs.application;

import com.robsartin.graphs.models.Graph;
import com.robsartin.graphs.ports.out.GraphRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the delete graph endpoint with the feature flag disabled (default).
 *
 * This test uses the default OpenFeature configuration where the delete flag is disabled,
 * ensuring that delete operations are properly protected by the feature flag.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GraphControllerDeleteFeatureFlagTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GraphRepository graphRepository;

    @BeforeEach
    void setUp() {
        graphRepository.deleteAll();
    }

    @Test
    void shouldReturn403WhenDeleteFlagIsDisabled() throws Exception {
        Graph graph = new Graph("Test Graph");
        Graph savedGraph = graphRepository.save(graph);

        mockMvc.perform(delete("/graphs/" + savedGraph.getId()))
                .andExpect(status().isForbidden());

        // Verify graph was NOT deleted
        assert graphRepository.findById(savedGraph.getId()).isPresent();
    }

    @Test
    void shouldReturn403WhenDeletingNonExistentGraphAndFlagIsDisabled() throws Exception {
        // Even for non-existent graphs, the flag check should come first
        mockMvc.perform(delete("/graphs/999"))
                .andExpect(status().isForbidden());
    }
}
