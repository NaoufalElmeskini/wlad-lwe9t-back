package io.lacrobate.wladLwe9t.application.product.dto;

import io.lacrobate.wladLwe9t.domain.model.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available
) {
    public static ProductResponse fromDomain(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.description(),
                product.price(),
                product.category(),
                product.available()
        );
    }
}