package com.robsartin.graphs.domain.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void shouldCreateItemWithName() {
        Item item = new Item("Test Item");

        assertNotNull(item);
        assertEquals("Test Item", item.getName());
    }

    @Test
    void shouldHaveNullIdWhenNotPersisted() {
        Item item = new Item("Test Item");

        assertNull(item.getId());
    }

    @Test
    void shouldAllowSettingId() {
        Item item = new Item("Test Item");
        item.setId(42);

        assertEquals(42, item.getId());
    }

    @Test
    void shouldAllowUpdatingName() {
        Item item = new Item("Original Name");
        item.setName("Updated Name");

        assertEquals("Updated Name", item.getName());
    }

    @Test
    void shouldImplementEqualsBasedOnId() {
        Item item1 = new Item("Item 1");
        item1.setId(1);

        Item item2 = new Item("Item 2");
        item2.setId(1);

        Item item3 = new Item("Item 3");
        item3.setId(2);

        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
    }

    @Test
    void shouldImplementHashCodeBasedOnId() {
        Item item1 = new Item("Item 1");
        item1.setId(1);

        Item item2 = new Item("Item 2");
        item2.setId(1);

        assertEquals(item1.hashCode(), item2.hashCode());
    }
}
