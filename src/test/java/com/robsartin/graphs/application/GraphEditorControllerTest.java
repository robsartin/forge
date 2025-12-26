package com.robsartin.graphs.application;

import com.robsartin.graphs.config.TestOpenFeatureConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
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
    void shouldRedirectUnauthenticatedUserToLogin() throws Exception {
        mockMvc.perform(get("/edit_graph"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    void shouldRedirectToGraphEditorPageWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/edit_graph"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/graph-editor.html"));
    }

    @Test
    @WithMockUser
    void shouldReturnGraphEditorStaticPage() throws Exception {
        mockMvc.perform(get("/graph-editor.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("<!DOCTYPE html>")))
                .andExpect(content().string(containsString("Graph Editor")));
    }

    @Test
    @WithMockUser
    void shouldIncludeReactAndD3Dependencies() throws Exception {
        mockMvc.perform(get("/graph-editor.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("react")))
                .andExpect(content().string(containsString("d3")));
    }

    @Test
    @WithMockUser
    void shouldIncludeGraphVisualizationContainer() throws Exception {
        mockMvc.perform(get("/graph-editor.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"root\"")));
    }

    @Test
    void shouldAllowAccessToLoginPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAccessToIndexHtml() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sign in with Google")));
    }

    @Test
    @WithMockUser
    void shouldReturnUserInfoWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.name").value("user"));
    }

    @Test
    void shouldRequireAuthenticationForUserEndpoint() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().is3xxRedirection());
    }
}
