package com.robsartin.graphs.application;

import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestOpenFeatureConfiguration.class)
class GraphEditorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirectToGraphEditorPage() throws Exception {
        mockMvc.perform(get("/edit_graph"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/graph-editor.html"));
    }

    @Test
    void shouldReturnGraphEditorStaticPage() throws Exception {
        mockMvc.perform(get("/graph-editor.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("<!DOCTYPE html>")))
                .andExpect(content().string(containsString("Graph Editor")));
    }

    @Test
    void shouldIncludeReactAndD3Dependencies() throws Exception {
        mockMvc.perform(get("/graph-editor.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("react")))
                .andExpect(content().string(containsString("d3")));
    }

    @Test
    void shouldIncludeGraphVisualizationContainer() throws Exception {
        mockMvc.perform(get("/graph-editor.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"root\"")));
    }
}
