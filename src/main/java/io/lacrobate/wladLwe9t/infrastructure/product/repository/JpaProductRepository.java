package io.lacrobate.wladLwe9t.infrastructure.product.repository;

import io.lacrobate.wladLwe9t.infrastructure.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByCategoryIgnoreCase(String category);
}