package io.lacrobate.wladLwe9t.infrastructure.product.entity;

import io.lacrobate.wladLwe9t.domain.model.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Boolean available = true;

    public static ProductEntity fromDomain(Product product) {
        return ProductEntity.builder()
                .id(product.id())
                .name(product.name())
                .description(product.description())
                .price(product.price())
                .category(product.category())
                .available(product.available())
                .build();
    }

    public Product toDomain() {
        return new Product(id, name, description, price, category, available);
    }
}