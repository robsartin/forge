package com.robsartin.graphs.ports.out;

import com.robsartin.graphs.models.ImmutableGraphEntity;

import java.util.List;
import java.util.Optional;

/**
 * Port for ImmutableGraph persistence operations.
 * This interface defines the contract for storing and retrieving immutable graphs,
 * independent of the actual persistence mechanism.
 */
public interface ImmutableGraphRepository {

    /**
     * Saves an immutable graph to the repository.
     *
     * @param graph the graph to save
     * @return the saved graph with generated ID
     */
    ImmutableGraphEntity save(ImmutableGraphEntity graph);

    /**
     * Finds an immutable graph by its ID.
     *
     * @param graphId the graph ID
     * @return an Optional containing the graph if found, or empty if not found
     */
    Optional<ImmutableGraphEntity> findById(Integer graphId);

    /**
     * Retrieves all immutable graphs from the repository.
     *
     * @return a list of all graphs
     */
    List<ImmutableGraphEntity> findAll();

    /**
     * Deletes an immutable graph by its ID.
     *
     * @param graphId the graph ID to delete
     */
    void deleteById(Integer graphId);

    /**
     * Checks if an immutable graph exists with the given ID.
     *
     * @param graphId the graph ID
     * @return true if a graph exists, false otherwise
     */
    boolean existsById(Integer graphId);

    /**
     * Deletes all immutable graphs from the repository.
     */
    void deleteAll();
}
