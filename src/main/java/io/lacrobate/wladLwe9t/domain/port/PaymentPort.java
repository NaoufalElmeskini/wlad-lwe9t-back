package io.lacrobate.wladLwe9t.domain.port;

import io.lacrobate.wladLwe9t.domain.model.PaymentIntent;

/**
 * Port interface for payment processing operations.
 * Secondary port (driven adapter) in hexagonal architecture.
 * Implementations handle integration with external payment providers (e.g., Stripe).
 */
public interface PaymentPort {

    /**
     * Creates a payment intent with the payment provider.
     *
     * @param paymentIntent Domain model containing payment details
     * @return Created payment intent with provider-specific ID and client secret
     * @throws PaymentProcessingException if creation fails
     */
    PaymentIntent createPaymentIntent(PaymentIntent paymentIntent);

    /**
     * Retrieves the current status of a payment intent.
     *
     * @param paymentIntentId Provider-specific payment intent ID
     * @return Payment intent with current status
     * @throws PaymentProcessingException if retrieval fails
     */
    PaymentIntent getPaymentIntent(String paymentIntentId);

    /**
     * Confirms a payment intent (optional operation).
     *
     * @param paymentIntentId Provider-specific payment intent ID
     * @return Confirmed payment intent
     * @throws PaymentProcessingException if confirmation fails
     */
    PaymentIntent confirmPaymentIntent(String paymentIntentId);

    /**
     * Cancels a payment intent.
     *
     * @param paymentIntentId Provider-specific payment intent ID
     * @return Canceled payment intent
     * @throws PaymentProcessingException if cancellation fails
     */
    PaymentIntent cancelPaymentIntent(String paymentIntentId);

    /**
     * Exception thrown when payment processing fails.
     */
    class PaymentProcessingException extends RuntimeException {
        public PaymentProcessingException(String message) {
            super(message);
        }

        public PaymentProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
