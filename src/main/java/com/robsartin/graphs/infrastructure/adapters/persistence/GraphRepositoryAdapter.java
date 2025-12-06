package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.domain.models.Graph;
import com.robsartin.graphs.domain.ports.out.GraphRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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
    public Optional<Graph> findById(Long id) {
        return jpaGraphRepository.findById(id);
    }

    @Override
    public List<Graph> findAll() {
        return jpaGraphRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        jpaGraphRepository.deleteById(id);
    }

    @Override
    public List<Graph> findByParentId(Long parentId) {
        return jpaGraphRepository.findByParentId(parentId);
    }
}
