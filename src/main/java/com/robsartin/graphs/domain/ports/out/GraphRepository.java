package com.robsartin.graphs.domain.ports.out;

import com.robsartin.graphs.domain.models.Graph;

import java.util.List;
import java.util.Optional;

public interface GraphRepository {
    Graph save(Graph graph);
    Optional<Graph> findById(Long id);
    List<Graph> findAll();
    void deleteById(Long id);
    List<Graph> findByParentId(Long parentId);
}
