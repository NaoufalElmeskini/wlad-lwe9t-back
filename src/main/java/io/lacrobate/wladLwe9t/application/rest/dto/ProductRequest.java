package io.lacrobate.wladLwe9t.application.rest.dto;

import io.lacrobate.wladLwe9t.domain.model.Product;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 100, message = "Product name must be less than 100 characters")
        String name,

        @Size(max = 500, message = "Description must be less than 500 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 6, fraction = 2, message = "Price format is invalid")
        BigDecimal price,

        @NotBlank(message = "Category is required")
        @Size(max = 50, message = "Category must be less than 50 characters")
        String category,

        boolean available
) {
    public Product toDomain() {
        return Product.create(name, description, price, category);
    }
}