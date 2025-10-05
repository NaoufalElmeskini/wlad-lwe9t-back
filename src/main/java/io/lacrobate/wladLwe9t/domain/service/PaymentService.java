package io.lacrobate.wladLwe9t.domain.service;

import io.lacrobate.wladLwe9t.domain.model.PaymentIntent;
import io.lacrobate.wladLwe9t.domain.port.PaymentPort;
import org.springframework.stereotype.Service;

/**
 * Domain service for payment business logic.
 * Orchestrates payment operations and enforces business rules.
 */
@Service
public class PaymentService {

    private final PaymentPort paymentPort;

    public PaymentService(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }

    /**
     * Creates a new payment intent after validating business rules.
     *
     * @param paymentIntent Payment intent with customer and item details
     * @return Created payment intent with client secret for frontend
     */
    public PaymentIntent createPaymentIntent(PaymentIntent paymentIntent) {
        // Enforce business validation rules
        paymentIntent.validate();

        // Delegate to payment provider adapter
        return paymentPort.createPaymentIntent(paymentIntent);
    }

    /**
     * Retrieves payment intent status.
     *
     * @param paymentIntentId Payment intent identifier
     * @return Current payment intent state
     */
    public PaymentIntent getPaymentIntent(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment intent ID is required");
        }

        return paymentPort.getPaymentIntent(paymentIntentId);
    }

    /**
     * Confirms a payment intent (server-side confirmation if needed).
     *
     * @param paymentIntentId Payment intent identifier
     * @return Confirmed payment intent
     */
    public PaymentIntent confirmPaymentIntent(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment intent ID is required");
        }

        PaymentIntent current = paymentPort.getPaymentIntent(paymentIntentId);

        // Business rule: Cannot confirm already finalized payments
        if (current.isFinalState()) {
            throw new IllegalStateException(
                    String.format("Payment intent %s is already in final state: %s",
                            paymentIntentId, current.getStatus())
            );
        }

        return paymentPort.confirmPaymentIntent(paymentIntentId);
    }

    /**
     * Cancels a payment intent if not yet finalized.
     *
     * @param paymentIntentId Payment intent identifier
     * @return Canceled payment intent
     */
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment intent ID is required");
        }

        PaymentIntent current = paymentPort.getPaymentIntent(paymentIntentId);

        // Business rule: Cannot cancel already finalized payments
        if (current.isFinalState()) {
            throw new IllegalStateException(
                    String.format("Cannot cancel payment intent %s in state: %s",
                            paymentIntentId, current.getStatus())
            );
        }

        return paymentPort.cancelPaymentIntent(paymentIntentId);
    }

    /**
     * Processes webhook events from payment provider.
     * Validates event and updates payment status accordingly.
     *
     * @param eventType Type of webhook event
     * @param paymentIntentId Related payment intent ID
     */
    public void processWebhookEvent(String eventType, String paymentIntentId) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type is required");
        }

        // Retrieve current state
        PaymentIntent paymentIntent = paymentPort.getPaymentIntent(paymentIntentId);

        // Log webhook event for audit trail
        System.out.printf("Webhook received: %s for payment %s (status: %s)%n",
                eventType, paymentIntentId, paymentIntent.getStatus());

        // Business logic for specific events can be added here
        // e.g., send confirmation email, update inventory, etc.
    }
}
