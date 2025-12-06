package com.robsartin.graphs.domain.ports.out;

import com.robsartin.graphs.domain.models.Item;

import java.util.List;
import java.util.Optional;

/**
 * Port for Item persistence operations.
 * This interface defines the contract for storing and retrieving items,
 * independent of the actual persistence mechanism.
 */
public interface ItemRepository {

    /**
     * Saves an item to the repository.
     *
     * @param item the item to save
     * @return the saved item with generated ID
     */
    Item save(Item item);

    /**
     * Finds an item by its ID.
     *
     * @param id the item ID
     * @return an Optional containing the item if found, or empty if not found
     */
    Optional<Item> findById(Integer id);

    /**
     * Retrieves all items from the repository.
     *
     * @return a list of all items
     */
    List<Item> findAll();

    /**
     * Deletes an item by its ID.
     *
     * @param id the item ID to delete
     */
    void deleteById(Integer id);

    /**
     * Checks if an item exists with the given ID.
     *
     * @param id the item ID
     * @return true if an item exists, false otherwise
     */
    boolean existsById(Integer id);
}
