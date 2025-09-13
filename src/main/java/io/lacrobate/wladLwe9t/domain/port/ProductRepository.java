package io.lacrobate.wladLwe9t.domain.port;

import io.lacrobate.wladLwe9t.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    List<Product> findByCategory(String category);

    Product save(Product product);

    void deleteById(Long id);

    boolean existsById(Long id);
}