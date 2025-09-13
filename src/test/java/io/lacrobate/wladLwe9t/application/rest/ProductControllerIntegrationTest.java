package io.lacrobate.wladLwe9t.application.rest;

import io.lacrobate.wladLwe9t.application.rest.dto.ProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldGetAllProductsSuccessfully() {
        ResponseEntity<Object[]> response = restTemplate.getForEntity("/produits", Object[].class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().length);
    }

    @Test
    void shouldGetProductByIdSuccessfully() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/produits/1", Object.class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        ResponseEntity<Object> response = restTemplate.getForEntity("/produits/999", Object.class);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldCreateProductSuccessfully() {
        ProductRequest request = new ProductRequest(
                "Nouveau Produit",
                "Description du nouveau produit",
                new BigDecimal("89.99"),
                "Test",
                true
        );

        ResponseEntity<Object> response = restTemplate.postForEntity("/produits", request, Object.class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldValidateProductCreationWithInvalidData() {
        ProductRequest invalidRequest = new ProductRequest(
                "",  // Invalid: empty name
                "Description",
                new BigDecimal("-10"),  // Invalid: negative price
                "",  // Invalid: empty category
                true
        );

        ResponseEntity<Object> response = restTemplate.postForEntity("/produits", invalidRequest, Object.class);

        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        ProductRequest updateRequest = new ProductRequest(
                "Produit Modifié",
                "Description mise à jour",
                new BigDecimal("199.99"),
                "Informatique",
                false
        );

        restTemplate.put("/produits/1", updateRequest);

        ResponseEntity<Object> response = restTemplate.getForEntity("/produits/1", Object.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        restTemplate.delete("/produits/1");

        // Verify product is deleted
        ResponseEntity<Object> response = restTemplate.getForEntity("/produits/1", Object.class);
        assertEquals(404, response.getStatusCodeValue());
    }

}