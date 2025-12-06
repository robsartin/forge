package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.domain.models.Item;
import com.robsartin.graphs.domain.ports.out.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for ItemController that tests the full stack
 * including the database layer.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        itemRepository.findAll().forEach(item -> itemRepository.deleteById(item.getId()));
    }

    @Test
    void shouldCreateAndRetrieveItem() throws Exception {
        // Create an item
        String createResponse = mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test Item\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Item createdItem = objectMapper.readValue(createResponse, Item.class);

        // Retrieve the item by ID
        mockMvc.perform(get("/items/" + createdItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdItem.getId()))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void shouldRetrieveAllItems() throws Exception {
        // Create multiple items
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Item 1\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Item 2\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Item 3\"}"))
                .andExpect(status().isCreated());

        // Retrieve all items
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Item 1", "Item 2", "Item 3")));
    }

    @Test
    void shouldReturn404ForNonExistentItem() throws Exception {
        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectItemWithoutName() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectItemWithBlankName() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
