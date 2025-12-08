package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.domain.models.Graph;
import com.robsartin.graphs.domain.ports.out.GraphRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements the GraphRepository port using Spring Data JPA.
 * This adapter translates between the domain port interface and the JPA repository.
 */
@Component
public class GraphRepositoryAdapter implements GraphRepository {

    private final JpaGraphRepository jpaGraphRepository;

    public GraphRepositoryAdapter(JpaGraphRepository jpaGraphRepository) {
        this.jpaGraphRepository = jpaGraphRepository;
    }

    @Override
    public Graph save(Graph graph) {
        return jpaGraphRepository.save(graph);
    }

    @Override
    public Optional<Graph> findById(Integer id) {
        return jpaGraphRepository.findById(id);
    }

    @Override
    public List<Graph> findAll() {
        return jpaGraphRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        jpaGraphRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return jpaGraphRepository.existsById(id);
    }

    @Override
    public void deleteAll() {
        jpaGraphRepository.deleteAll();
    }
}
