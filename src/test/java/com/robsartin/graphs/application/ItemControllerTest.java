package com.robsartin.graphs.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robsartin.graphs.domain.models.Item;
import com.robsartin.graphs.domain.ports.out.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private ItemRepository itemRepository;

    @Autowired
    ItemControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void shouldReturnAllItems() throws Exception {
        Item item1 = new Item("Item 1");
        item1.setId(1);
        Item item2 = new Item("Item 2");
        item2.setId(2);
        List<Item> items = Arrays.asList(item1, item2);

        when(itemRepository.findAll()).thenReturn(items);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Item 2"));
    }

    @Test
    void shouldReturnEmptyListWhenNoItems() throws Exception {
        when(itemRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldCreateNewItem() throws Exception {
        Item newItem = new Item("New Item");
        Item savedItem = new Item("New Item");
        savedItem.setId(1);

        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Item"));
    }

    @Test
    void shouldReturnItemById() throws Exception {
        Item item = new Item("Test Item");
        item.setId(45);

        when(itemRepository.findById(45)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/items/45"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(45))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void shouldReturn404WhenItemNotFound() throws Exception {
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectPostWithoutName() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteItemById() throws Exception {
        when(itemRepository.existsById(1)).thenReturn(true);
        doNothing().when(itemRepository).deleteById(1);

        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isNoContent());

        verify(itemRepository).deleteById(1);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentItem() throws Exception {
        when(itemRepository.existsById(999)).thenReturn(false);

        mockMvc.perform(delete("/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateItemName() throws Exception {
        Item existingItem = new Item("Old Name");
        existingItem.setId(1);
        Item updatedItem = new Item("New Name");
        updatedItem.setId(1);

        when(itemRepository.findById(1)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        String requestBody = objectMapper.writeValueAsString(
                new ItemController.UpdateItemRequest("New Name"));

        mockMvc.perform(put("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentItem() throws Exception {
        when(itemRepository.findById(999)).thenReturn(Optional.empty());

        String requestBody = objectMapper.writeValueAsString(
                new ItemController.UpdateItemRequest("New Name"));

        mockMvc.perform(put("/items/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectPutWithoutName() throws Exception {
        mockMvc.perform(put("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
