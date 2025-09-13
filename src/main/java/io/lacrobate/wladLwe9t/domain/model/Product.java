package io.lacrobate.wladLwe9t.domain.model;

import java.math.BigDecimal;

public record Product(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available
) {
    public Product {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Product category cannot be blank");
        }
        name = name.trim();
        category = category.trim();
    }

    public static Product create(String name, String description, BigDecimal price, String category) {
        return new Product(null, name, description, price, category, true);
    }

    public Product withId(Long id) {
        return new Product(id, name, description, price, category, available);
    }
}