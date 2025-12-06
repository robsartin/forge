package com.robsartin.graphs.application;

import com.robsartin.graphs.domain.models.Item;
import com.robsartin.graphs.domain.ports.out.ItemRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing items.
 * Handles HTTP requests for item operations.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /**
     * GET /items - Retrieves all items
     *
     * @return list of all items
     */
    @GetMapping
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /**
     * GET /items/{id} - Retrieves a specific item by ID
     *
     * @param id the item ID
     * @return the item if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Integer id) {
        return itemRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /items - Creates a new item
     *
     * @param request the item creation request
     * @return the created item with HTTP 201 status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Item createItem(@Valid @RequestBody CreateItemRequest request) {
        Item item = new Item(request.name());
        return itemRepository.save(item);
    }

    /**
     * DELETE /items/{id} - Deletes an item by ID
     *
     * @param id the item ID to delete
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Integer id) {
        if (!itemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        itemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /items/{id} - Updates an item's name
     *
     * @param id the item ID
     * @param request the update request containing the new name
     * @return the updated item if found, 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Integer id,
                                           @Valid @RequestBody UpdateItemRequest request) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setName(request.name());
                    Item updatedItem = itemRepository.save(item);
                    return ResponseEntity.ok(updatedItem);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Request DTO for creating an item
     */
    public record CreateItemRequest(@NotBlank(message = "Name is required") String name) {
    }

    /**
     * Request DTO for updating an item
     */
    public record UpdateItemRequest(@NotBlank(message = "Name is required") String name) {
    }
}
