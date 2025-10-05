package io.lacrobate.wladLwe9t.domain.service;

import io.lacrobate.wladLwe9t.domain.model.PaymentIntent;
import io.lacrobate.wladLwe9t.domain.port.PaymentPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService.
 * Tests business logic and validation rules.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentPort paymentPort;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentPort);
    }

    @Test
    @DisplayName("Should create payment intent when data is valid")
    void shouldCreatePaymentIntentWhenDataIsValid() {
        // Given
        PaymentIntent validIntent = createValidPaymentIntent();

        PaymentIntent expectedResult = PaymentIntent.builder()
                .id("pi_test_123")
                .amount(2500L)
                .currency("EUR")
                .status(PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD)
                .clientSecret("pi_test_123_secret_abc")
                .customerInfo(validIntent.getCustomerInfo())
                .items(validIntent.getItems())
                .createdAt(Instant.now())
                .build();

        when(paymentPort.createPaymentIntent(any(PaymentIntent.class)))
                .thenReturn(expectedResult);

        // When
        PaymentIntent result = paymentService.createPaymentIntent(validIntent);

        // Then
        assertNotNull(result);
        assertEquals("pi_test_123", result.getId());
        assertEquals("pi_test_123_secret_abc", result.getClientSecret());
        assertEquals(PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD, result.getStatus());

        verify(paymentPort, times(1)).createPaymentIntent(validIntent);
    }

    @Test
    @DisplayName("Should throw exception when amount is invalid")
    void shouldThrowExceptionWhenAmountIsInvalid() {
        // Given
        PaymentIntent invalidIntent = createValidPaymentIntent()
                .builder()
                .amount(-100L)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPaymentIntent(invalidIntent));

        verify(paymentPort, never()).createPaymentIntent(any());
    }

    @Test
    @DisplayName("Should throw exception when customer info is missing")
    void shouldThrowExceptionWhenCustomerInfoIsMissing() {
        // Given
        PaymentIntent invalidIntent = createValidPaymentIntent()
                .builder()
                .customerInfo(null)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPaymentIntent(invalidIntent));

        verify(paymentPort, never()).createPaymentIntent(any());
    }

    @Test
    @DisplayName("Should retrieve payment intent by ID")
    void shouldRetrievePaymentIntentById() {
        // Given
        String paymentIntentId = "pi_test_123";
        PaymentIntent expectedIntent = PaymentIntent.builder()
                .id(paymentIntentId)
                .amount(1000L)
                .currency("EUR")
                .status(PaymentIntent.PaymentStatus.SUCCEEDED)
                .createdAt(Instant.now())
                .build();

        when(paymentPort.getPaymentIntent(paymentIntentId))
                .thenReturn(expectedIntent);

        // When
        PaymentIntent result = paymentService.getPaymentIntent(paymentIntentId);

        // Then
        assertNotNull(result);
        assertEquals(paymentIntentId, result.getId());
        assertEquals(PaymentIntent.PaymentStatus.SUCCEEDED, result.getStatus());

        verify(paymentPort, times(1)).getPaymentIntent(paymentIntentId);
    }

    @Test
    @DisplayName("Should throw exception when retrieving with null ID")
    void shouldThrowExceptionWhenRetrievingWithNullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.getPaymentIntent(null));

        verify(paymentPort, never()).getPaymentIntent(any());
    }

    @Test
    @DisplayName("Should confirm payment intent when not in final state")
    void shouldConfirmPaymentIntentWhenNotInFinalState() {
        // Given
        String paymentIntentId = "pi_test_123";

        PaymentIntent currentIntent = PaymentIntent.builder()
                .id(paymentIntentId)
                .status(PaymentIntent.PaymentStatus.REQUIRES_CONFIRMATION)
                .build();

        PaymentIntent confirmedIntent = currentIntent.builder()
                .status(PaymentIntent.PaymentStatus.PROCESSING)
                .build();

        when(paymentPort.getPaymentIntent(paymentIntentId))
                .thenReturn(currentIntent);
        when(paymentPort.confirmPaymentIntent(paymentIntentId))
                .thenReturn(confirmedIntent);

        // When
        PaymentIntent result = paymentService.confirmPaymentIntent(paymentIntentId);

        // Then
        assertEquals(PaymentIntent.PaymentStatus.PROCESSING, result.getStatus());

        verify(paymentPort, times(1)).getPaymentIntent(paymentIntentId);
        verify(paymentPort, times(1)).confirmPaymentIntent(paymentIntentId);
    }

    @Test
    @DisplayName("Should throw exception when confirming already succeeded payment")
    void shouldThrowExceptionWhenConfirmingAlreadySucceededPayment() {
        // Given
        String paymentIntentId = "pi_test_123";

        PaymentIntent succeededIntent = PaymentIntent.builder()
                .id(paymentIntentId)
                .status(PaymentIntent.PaymentStatus.SUCCEEDED)
                .build();

        when(paymentPort.getPaymentIntent(paymentIntentId))
                .thenReturn(succeededIntent);

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> paymentService.confirmPaymentIntent(paymentIntentId));

        verify(paymentPort, times(1)).getPaymentIntent(paymentIntentId);
        verify(paymentPort, never()).confirmPaymentIntent(any());
    }

    @Test
    @DisplayName("Should cancel payment intent when not in final state")
    void shouldCancelPaymentIntentWhenNotInFinalState() {
        // Given
        String paymentIntentId = "pi_test_123";

        PaymentIntent currentIntent = PaymentIntent.builder()
                .id(paymentIntentId)
                .status(PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD)
                .build();

        PaymentIntent canceledIntent = currentIntent.builder()
                .status(PaymentIntent.PaymentStatus.CANCELED)
                .build();

        when(paymentPort.getPaymentIntent(paymentIntentId))
                .thenReturn(currentIntent);
        when(paymentPort.cancelPaymentIntent(paymentIntentId))
                .thenReturn(canceledIntent);

        // When
        PaymentIntent result = paymentService.cancelPaymentIntent(paymentIntentId);

        // Then
        assertEquals(PaymentIntent.PaymentStatus.CANCELED, result.getStatus());

        verify(paymentPort, times(1)).getPaymentIntent(paymentIntentId);
        verify(paymentPort, times(1)).cancelPaymentIntent(paymentIntentId);
    }

    @Test
    @DisplayName("Should throw exception when canceling already succeeded payment")
    void shouldThrowExceptionWhenCancelingAlreadySucceededPayment() {
        // Given
        String paymentIntentId = "pi_test_123";

        PaymentIntent succeededIntent = PaymentIntent.builder()
                .id(paymentIntentId)
                .status(PaymentIntent.PaymentStatus.SUCCEEDED)
                .build();

        when(paymentPort.getPaymentIntent(paymentIntentId))
                .thenReturn(succeededIntent);

        // When & Then
        assertThrows(IllegalStateException.class,
                () -> paymentService.cancelPaymentIntent(paymentIntentId));

        verify(paymentPort, times(1)).getPaymentIntent(paymentIntentId);
        verify(paymentPort, never()).cancelPaymentIntent(any());
    }

    @Test
    @DisplayName("Should process webhook event")
    void shouldProcessWebhookEvent() {
        // Given
        String eventType = "payment_intent.succeeded";
        String paymentIntentId = "pi_test_123";

        PaymentIntent paymentIntent = PaymentIntent.builder()
                .id(paymentIntentId)
                .status(PaymentIntent.PaymentStatus.SUCCEEDED)
                .build();

        when(paymentPort.getPaymentIntent(paymentIntentId))
                .thenReturn(paymentIntent);

        // When
        assertDoesNotThrow(() ->
                paymentService.processWebhookEvent(eventType, paymentIntentId));

        // Then
        verify(paymentPort, times(1)).getPaymentIntent(paymentIntentId);
    }

    /**
     * Helper method to create a valid payment intent for testing.
     */
    private PaymentIntent createValidPaymentIntent() {
        PaymentIntent.CustomerInfo customerInfo = PaymentIntent.CustomerInfo.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .address("123 Test St")
                .city("Paris")
                .postalCode("75001")
                .phone("+33612345678")
                .build();

        PaymentIntent.PaymentItem item = PaymentIntent.PaymentItem.builder()
                .id("prod_001")
                .name("Test Product")
                .quantity(1)
                .price(2500L)
                .build();

        return PaymentIntent.builder()
                .amount(2500L)
                .currency("EUR")
                .customerInfo(customerInfo)
                .items(List.of(item))
                .build();
    }
}
