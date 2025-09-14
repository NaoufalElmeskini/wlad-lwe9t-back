package io.lacrobate.wladLwe9t.application.product;

import io.lacrobate.wladLwe9t.application.product.dto.ProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
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

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    private TestRestTemplate authenticatedRestTemplate() {
        return new TestRestTemplate(restTemplateBuilder)
                .withBasicAuth("tintin", "acrobate");
    }

    @Test
    void shouldGetAllProductsSuccessfully() {
        String url = "http://localhost:" + port + "/api/produits";
        ResponseEntity<Object[]> response = authenticatedRestTemplate().getForEntity(url, Object[].class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().length);
    }

    @Test
    void shouldGetProductByIdSuccessfully() {
        String url = "http://localhost:" + port + "/api/produits/1";
        ResponseEntity<Object> response = authenticatedRestTemplate().getForEntity(url, Object.class);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        String url = "http://localhost:" + port + "/api/produits/999";
        ResponseEntity<Object> response = authenticatedRestTemplate().getForEntity(url, Object.class);

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

        String url = "http://localhost:" + port + "/api/produits";
        ResponseEntity<Object> response = authenticatedRestTemplate().postForEntity(url, request, Object.class);

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

        String url = "http://localhost:" + port + "/api/produits";
        ResponseEntity<Object> response = authenticatedRestTemplate().postForEntity(url, invalidRequest, Object.class);

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

        String updateUrl = "http://localhost:" + port + "/api/produits/1";
        authenticatedRestTemplate().put(updateUrl, updateRequest);

        ResponseEntity<Object> response = authenticatedRestTemplate().getForEntity(updateUrl, Object.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        String deleteUrl = "http://localhost:" + port + "/api/produits/1";
        authenticatedRestTemplate().delete(deleteUrl);

        // Verify product is deleted
        ResponseEntity<Object> response = authenticatedRestTemplate().getForEntity(deleteUrl, Object.class);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoAuthentication() {
        String url = "http://localhost:" + port + "/api/produits";
        ResponseEntity<Object[]> response = restTemplate.getForEntity(url, Object[].class);

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void shouldReturnUnauthorizedForPostWithoutAuthentication() {
        ProductRequest request = new ProductRequest(
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                "Test Category",
                true
        );

        String url = "http://localhost:" + port + "/api/produits";
        ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);

        assertEquals(401, response.getStatusCodeValue());
    }

}