package io.lacrobate.wladLwe9t.application.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lacrobate.wladLwe9t.application.payment.dto.PaymentRequest;
import io.lacrobate.wladLwe9t.domain.model.PaymentIntent;
import io.lacrobate.wladLwe9t.domain.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for PaymentController.
 * Tests HTTP API endpoints with mocked service layer.
 */
@WebMvcTest(PaymentController.class)
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Should create payment intent when authenticated with valid data")
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldCreatePaymentIntentWhenAuthenticatedWithValidData() throws Exception {
        // Given
        PaymentRequest request = createValidPaymentRequest();

        PaymentIntent createdIntent = PaymentIntent.builder()
                .id("pi_test_123")
                .amount(2500L)
                .currency("EUR")
                .status(PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD)
                .clientSecret("pi_test_123_secret_abc")
                .customerInfo(createCustomerInfo())
                .items(List.of(createPaymentItem()))
                .createdAt(Instant.now())
                .build();

        when(paymentService.createPaymentIntent(any(PaymentIntent.class)))
                .thenReturn(createdIntent);

        // When & Then
        mockMvc.perform(post("/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret").value("pi_test_123_secret_abc"))
                .andExpect(jsonPath("$.paymentIntentId").value("pi_test_123"))
                .andExpect(jsonPath("$.amount").value(2500))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.status").value("REQUIRES_PAYMENT_METHOD"));
    }

    @Test
    @DisplayName("Should return 401 when creating payment intent without authentication")
    void shouldReturn401WhenCreatingPaymentIntentWithoutAuthentication() throws Exception {
        // Given
        PaymentRequest request = createValidPaymentRequest();

        // When & Then
        mockMvc.perform(post("/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when creating payment intent with invalid amount")
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldReturn400WhenCreatingPaymentIntentWithInvalidAmount() throws Exception {
        // Given
        PaymentRequest request = createValidPaymentRequest();
        request.setAmount(-100L); // Invalid amount

        // When & Then
        mockMvc.perform(post("/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when creating payment intent with missing customer info")
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldReturn400WhenCreatingPaymentIntentWithMissingCustomerInfo() throws Exception {
        // Given
        PaymentRequest request = createValidPaymentRequest();
        request.setCustomerInfo(null); // Missing customer info

        // When & Then
        mockMvc.perform(post("/payments/create-intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should retrieve payment intent when authenticated")
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldRetrievePaymentIntentWhenAuthenticated() throws Exception {
        // Given
        String paymentIntentId = "pi_test_123";

        PaymentIntent paymentIntent = PaymentIntent.builder()
                .id(paymentIntentId)
                .amount(1000L)
                .currency("EUR")
                .status(PaymentIntent.PaymentStatus.SUCCEEDED)
                .createdAt(Instant.now())
                .build();

        when(paymentService.getPaymentIntent(paymentIntentId))
                .thenReturn(paymentIntent);

        // When & Then
        mockMvc.perform(get("/payments/" + paymentIntentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentIntentId").value(paymentIntentId))
                .andExpect(jsonPath("$.amount").value(1000))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));
    }

    @Test
    @DisplayName("Should process webhook without authentication (public endpoint)")
    void shouldProcessWebhookWithoutAuthentication() throws Exception {
        // Given
        String payload = "{\"id\":\"evt_test_123\",\"type\":\"payment_intent.succeeded\"}";
        String signature = "t=1234567890,v1=fake_signature";

        // Note: This test will fail with real webhook validation
        // In real scenario, you would need to mock Webhook.constructEvent

        // When & Then
        mockMvc.perform(post("/payments/webhook")
                        .header("Stripe-Signature", signature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest()); // Expected due to invalid signature
    }

    /**
     * Helper method to create a valid payment request.
     */
    private PaymentRequest createValidPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(2500L);
        request.setCurrency("EUR");

        PaymentRequest.CustomerInfoDTO customerInfo = new PaymentRequest.CustomerInfoDTO();
        customerInfo.setEmail("test@example.com");
        customerInfo.setFirstName("John");
        customerInfo.setLastName("Doe");
        customerInfo.setAddress("123 Test St");
        customerInfo.setCity("Paris");
        customerInfo.setPostalCode("75001");
        customerInfo.setPhone("+33612345678");
        request.setCustomerInfo(customerInfo);

        PaymentRequest.PaymentItemDTO item = new PaymentRequest.PaymentItemDTO();
        item.setId("prod_001");
        item.setName("Test Product");
        item.setQuantity(1);
        item.setPrice(2500L);
        request.setItems(List.of(item));

        return request;
    }

    private PaymentIntent.CustomerInfo createCustomerInfo() {
        return PaymentIntent.CustomerInfo.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .address("123 Test St")
                .city("Paris")
                .postalCode("75001")
                .phone("+33612345678")
                .build();
    }

    private PaymentIntent.PaymentItem createPaymentItem() {
        return PaymentIntent.PaymentItem.builder()
                .id("prod_001")
                .name("Test Product")
                .quantity(1)
                .price(2500L)
                .build();
    }
}
