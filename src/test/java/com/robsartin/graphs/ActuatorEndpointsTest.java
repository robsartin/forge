package com.robsartin.graphs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ActuatorEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @WithMockUser
    void metricsEndpointShouldBeAccessibleWithAuth() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    @WithMockUser
    void threaddumpEndpointShouldBeAccessibleWithAuth() throws Exception {
        mockMvc.perform(get("/actuator/threaddump"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threads").isArray());
    }

    @Test
    @WithMockUser
    void envEndpointShouldBeAccessibleWithAuth() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources").isArray());
    }

    @Test
    @WithMockUser
    void lazyInitializationShouldBeEnabled() throws Exception {
        mockMvc.perform(get("/actuator/env/spring.main.lazy-initialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.property.value").value("true"));
    }
}
