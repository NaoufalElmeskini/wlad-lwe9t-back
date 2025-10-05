package io.lacrobate.wladLwe9t.infrastructure.payment;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import io.lacrobate.wladLwe9t.domain.model.PaymentIntent;
import io.lacrobate.wladLwe9t.domain.port.PaymentPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe implementation of PaymentPort.
 * Infrastructure adapter for Stripe payment processing.
 */
@Component
public class StripePaymentAdapter implements PaymentPort {

    @Override
    public PaymentIntent createPaymentIntent(PaymentIntent paymentIntent) {
        try {
            // Create or retrieve Stripe customer
            String customerId = createStripeCustomer(paymentIntent.getCustomerInfo());

            // Build metadata with items information
            Map<String, String> metadata = buildMetadata(paymentIntent);

            // Create Stripe PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentIntent.getAmount())
                    .setCurrency(paymentIntent.getCurrency().toLowerCase())
                    .setCustomer(customerId)
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            com.stripe.model.PaymentIntent stripePaymentIntent =
                    com.stripe.model.PaymentIntent.create(params);

            // Map Stripe PaymentIntent to domain model
            return mapToDomain(stripePaymentIntent, paymentIntent);

        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to create payment intent: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentIntent getPaymentIntent(String paymentIntentId) {
        try {
            com.stripe.model.PaymentIntent stripePaymentIntent =
                    com.stripe.model.PaymentIntent.retrieve(paymentIntentId);

            return mapToDomain(stripePaymentIntent);

        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to retrieve payment intent: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentIntent confirmPaymentIntent(String paymentIntentId) {
        try {
            com.stripe.model.PaymentIntent stripePaymentIntent =
                    com.stripe.model.PaymentIntent.retrieve(paymentIntentId);

            PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder().build();
            com.stripe.model.PaymentIntent confirmed = stripePaymentIntent.confirm(params);

            return mapToDomain(confirmed);

        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to confirm payment intent: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) {
        try {
            com.stripe.model.PaymentIntent stripePaymentIntent =
                    com.stripe.model.PaymentIntent.retrieve(paymentIntentId);

            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();
            com.stripe.model.PaymentIntent canceled = stripePaymentIntent.cancel(params);

            return mapToDomain(canceled);

        } catch (StripeException e) {
            throw new PaymentProcessingException("Failed to cancel payment intent: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a Stripe customer from domain customer info.
     */
    private String createStripeCustomer(PaymentIntent.CustomerInfo customerInfo) throws StripeException {
        CustomerCreateParams.Address address = CustomerCreateParams.Address.builder()
                .setLine1(customerInfo.getAddress())
                .setCity(customerInfo.getCity())
                .setPostalCode(customerInfo.getPostalCode())
                .build();

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(customerInfo.getEmail())
                .setName(customerInfo.getFirstName() + " " + customerInfo.getLastName())
                .setAddress(address)
                .setPhone(customerInfo.getPhone())
                .build();

        Customer customer = Customer.create(params);
        return customer.getId();
    }

    /**
     * Builds metadata map from payment intent items.
     */
    private Map<String, String> buildMetadata(PaymentIntent paymentIntent) {
        Map<String, String> metadata = new HashMap<>();

        for (int i = 0; i < paymentIntent.getItems().size(); i++) {
            PaymentIntent.PaymentItem item = paymentIntent.getItems().get(i);
            metadata.put("item_" + i + "_id", item.getId());
            metadata.put("item_" + i + "_name", item.getName());
            metadata.put("item_" + i + "_quantity", String.valueOf(item.getQuantity()));
            metadata.put("item_" + i + "_price", String.valueOf(item.getPrice()));
        }

        metadata.put("customer_email", paymentIntent.getCustomerInfo().getEmail());
        metadata.put("customer_name",
                paymentIntent.getCustomerInfo().getFirstName() + " " +
                        paymentIntent.getCustomerInfo().getLastName());

        return metadata;
    }

    /**
     * Maps Stripe PaymentIntent to domain model (with original payment intent context).
     */
    private PaymentIntent mapToDomain(com.stripe.model.PaymentIntent stripePaymentIntent,
                                       PaymentIntent originalIntent) {
        return PaymentIntent.builder()
                .id(stripePaymentIntent.getId())
                .amount(stripePaymentIntent.getAmount())
                .currency(stripePaymentIntent.getCurrency().toUpperCase())
                .status(mapStatus(stripePaymentIntent.getStatus()))
                .clientSecret(stripePaymentIntent.getClientSecret())
                .customerInfo(originalIntent.getCustomerInfo())
                .items(originalIntent.getItems())
                .createdAt(Instant.ofEpochSecond(stripePaymentIntent.getCreated()))
                .build();
    }

    /**
     * Maps Stripe PaymentIntent to domain model (without original context).
     */
    private PaymentIntent mapToDomain(com.stripe.model.PaymentIntent stripePaymentIntent) {
        return PaymentIntent.builder()
                .id(stripePaymentIntent.getId())
                .amount(stripePaymentIntent.getAmount())
                .currency(stripePaymentIntent.getCurrency().toUpperCase())
                .status(mapStatus(stripePaymentIntent.getStatus()))
                .clientSecret(stripePaymentIntent.getClientSecret())
                .createdAt(Instant.ofEpochSecond(stripePaymentIntent.getCreated()))
                .build();
    }

    /**
     * Maps Stripe status to domain PaymentStatus enum.
     */
    private PaymentIntent.PaymentStatus mapStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "requires_payment_method" -> PaymentIntent.PaymentStatus.REQUIRES_PAYMENT_METHOD;
            case "requires_confirmation" -> PaymentIntent.PaymentStatus.REQUIRES_CONFIRMATION;
            case "requires_action" -> PaymentIntent.PaymentStatus.REQUIRES_ACTION;
            case "processing" -> PaymentIntent.PaymentStatus.PROCESSING;
            case "succeeded" -> PaymentIntent.PaymentStatus.SUCCEEDED;
            case "canceled" -> PaymentIntent.PaymentStatus.CANCELED;
            default -> PaymentIntent.PaymentStatus.FAILED;
        };
    }
}
