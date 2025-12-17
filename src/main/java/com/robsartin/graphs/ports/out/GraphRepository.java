package com.robsartin.graphs.ports.out;

import com.robsartin.graphs.models.Graph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for Graph persistence operations.
 * This interface defines the contract for storing and retrieving graphs,
 * independent of the actual persistence mechanism.
 */
public interface GraphRepository {

    /**
     * Saves a graph to the repository.
     *
     * @param graph the graph to save
     * @return the saved graph with generated ID
     */
    Graph save(Graph graph);

    /**
     * Finds a graph by its ID.
     *
     * @param id the graph ID
     * @return an Optional containing the graph if found, or empty if not found
     */
    Optional<Graph> findById(UUID id);

    /**
     * Retrieves all graphs from the repository.
     *
     * @return a list of all graphs
     */
    List<Graph> findAll();

    /**
     * Deletes a graph by its ID.
     *
     * @param id the graph ID to delete
     */
    void deleteById(UUID id);

    /**
     * Checks if a graph exists with the given ID.
     *
     * @param id the graph ID
     * @return true if a graph exists, false otherwise
     */
    boolean existsById(UUID id);

    /**
     * Deletes all graphs from the repository.
     */
    void deleteAll();

    /**
     * Flush repository changes to the database.
     */
    default void flush() {}
}
