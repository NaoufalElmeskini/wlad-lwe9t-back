package io.lacrobate.wladLwe9t.application.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.lacrobate.wladLwe9t.application.payment.dto.PaymentRequest;
import io.lacrobate.wladLwe9t.application.payment.dto.PaymentResponse;
import io.lacrobate.wladLwe9t.domain.model.PaymentIntent;
import io.lacrobate.wladLwe9t.domain.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for payment operations.
 * Primary adapter (driving side) in hexagonal architecture.
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.api.webhook-secret}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Creates a payment intent.
     *
     * POST /api/payments/create-intent
     */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@Valid @RequestBody PaymentRequest request) {
        // Map DTO to domain model
        PaymentIntent paymentIntent = mapToDomain(request);

        // Create payment intent via domain service
        PaymentIntent created = paymentService.createPaymentIntent(paymentIntent);

        // Map domain model to response DTO
        PaymentResponse response = PaymentResponse.builder()
                .clientSecret(created.getClientSecret())
                .paymentIntentId(created.getId())
                .amount(created.getAmount())
                .currency(created.getCurrency())
                .status(created.getStatus().name())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves payment intent status.
     *
     * GET /api/payments/{paymentIntentId}
     */
    @GetMapping("/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> getPaymentIntent(@PathVariable String paymentIntentId) {
        PaymentIntent paymentIntent = paymentService.getPaymentIntent(paymentIntentId);

        PaymentResponse response = PaymentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .amount(paymentIntent.getAmount())
                .currency(paymentIntent.getCurrency())
                .status(paymentIntent.getStatus().name())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Confirms a payment intent (server-side confirmation).
     *
     * POST /api/payments/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPaymentIntent(@RequestBody Map<String, String> request) {
        String paymentIntentId = request.get("paymentIntentId");

        if (paymentIntentId == null || paymentIntentId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        PaymentIntent confirmed = paymentService.confirmPaymentIntent(paymentIntentId);

        PaymentResponse response = PaymentResponse.builder()
                .paymentIntentId(confirmed.getId())
                .status(confirmed.getStatus().name())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Handles Stripe webhook events.
     *
     * POST /api/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            // Verify webhook signature
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            // Extract payment intent ID from event
            String paymentIntentId = extractPaymentIntentId(event);

            // Process webhook event
            paymentService.processWebhookEvent(event.getType(), paymentIntentId);

            return ResponseEntity.ok("Webhook processed");

        } catch (SignatureVerificationException e) {
            // Invalid signature
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            // Other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing failed: " + e.getMessage());
        }
    }

    /**
     * Maps PaymentRequest DTO to domain PaymentIntent.
     */
    private PaymentIntent mapToDomain(PaymentRequest request) {
        PaymentIntent.CustomerInfo customerInfo = PaymentIntent.CustomerInfo.builder()
                .email(request.getCustomerInfo().getEmail())
                .firstName(request.getCustomerInfo().getFirstName())
                .lastName(request.getCustomerInfo().getLastName())
                .address(request.getCustomerInfo().getAddress())
                .city(request.getCustomerInfo().getCity())
                .postalCode(request.getCustomerInfo().getPostalCode())
                .phone(request.getCustomerInfo().getPhone())
                .build();

        var items = request.getItems().stream()
                .map(item -> PaymentIntent.PaymentItem.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());

        return PaymentIntent.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .customerInfo(customerInfo)
                .items(items)
                .build();
    }

    /**
     * Extracts payment intent ID from Stripe webhook event.
     */
    private String extractPaymentIntentId(Event event) {
        var dataObject = event.getDataObjectDeserializer().getObject();

        if (dataObject.isPresent() && dataObject.get() instanceof com.stripe.model.PaymentIntent) {
            com.stripe.model.PaymentIntent paymentIntent = (com.stripe.model.PaymentIntent) dataObject.get();
            return paymentIntent.getId();
        }

        throw new IllegalArgumentException("Invalid webhook event: no payment intent found");
    }
}
