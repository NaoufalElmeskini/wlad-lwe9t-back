package io.lacrobate.wladLwe9t.application.rest;

import io.lacrobate.wladLwe9t.application.rest.dto.ProductRequest;
import io.lacrobate.wladLwe9t.application.rest.dto.ProductResponse;
import io.lacrobate.wladLwe9t.domain.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product catalog management API")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve the complete product catalog")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public List<ProductResponse> getAllProducts() {
        log.debug("Retrieving all products");
        return productService.getAllProducts()
                .stream()
                .map(ProductResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product unique identifier", required = true)
            @PathVariable Long id) {
        log.debug("Retrieving product with id: {}", id);

        return productService.getProductById(id)
                .map(ProductResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new product", description = "Add a new product to the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data")
    })
    public ProductResponse createProduct(
            @Parameter(description = "Product information", required = true)
            @Valid @RequestBody ProductRequest request) {
        log.info("Creating new product: {}", request.name());

        var product = productService.createProduct(request.toDomain());
        return ProductResponse.fromDomain(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid product data")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product unique identifier", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated product information", required = true)
            @Valid @RequestBody ProductRequest request) {
        log.info("Updating product with id: {}", id);

        return productService.updateProduct(id, request.toDomain())
                .map(ProductResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Remove a product from the catalog")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product unique identifier", required = true)
            @PathVariable Long id) {
        log.info("Deleting product with id: {}", id);

        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}