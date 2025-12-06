package com.robsartin.graphs.infrastructure.adapters.persistence;

import com.robsartin.graphs.domain.models.Item;
import com.robsartin.graphs.domain.ports.out.ItemRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements the ItemRepository port using Spring Data JPA.
 * This adapter translates between the domain port interface and the JPA repository.
 */
@Component
public class ItemRepositoryAdapter implements ItemRepository {

    private final JpaItemRepository jpaItemRepository;

    public ItemRepositoryAdapter(JpaItemRepository jpaItemRepository) {
        this.jpaItemRepository = jpaItemRepository;
    }

    @Override
    public Item save(Item item) {
        return jpaItemRepository.save(item);
    }

    @Override
    public Optional<Item> findById(Integer id) {
        return jpaItemRepository.findById(id);
    }

    @Override
    public List<Item> findAll() {
        return jpaItemRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        jpaItemRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return jpaItemRepository.existsById(id);
    }
}
