package com.robsartin.graphs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ActuatorEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void metricsEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    void threaddumpEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/threaddump"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threads").isArray());
    }

    @Test
    void envEndpointShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources").isArray());
    }

    @Test
    void lazyInitializationShouldBeEnabled() throws Exception {
        mockMvc.perform(get("/actuator/env/spring.main.lazy-initialization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.property.value").value("true"));
    }
}
