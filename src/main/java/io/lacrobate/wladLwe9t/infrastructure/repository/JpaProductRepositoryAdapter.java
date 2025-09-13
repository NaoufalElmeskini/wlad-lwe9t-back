package io.lacrobate.wladLwe9t.infrastructure.repository;

import io.lacrobate.wladLwe9t.domain.model.Product;
import io.lacrobate.wladLwe9t.domain.port.ProductRepository;
import io.lacrobate.wladLwe9t.infrastructure.persistence.entity.ProductEntity;
import io.lacrobate.wladLwe9t.infrastructure.persistence.repository.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Primary
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;

    @Override
    public List<Product> findAll() {
        return jpaProductRepository.findAll()
                .stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return jpaProductRepository.findById(id)
                .map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> findByCategory(String category) {
        return jpaProductRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductEntity.fromDomain(product);
        ProductEntity savedEntity = jpaProductRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaProductRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaProductRepository.existsById(id);
    }
}