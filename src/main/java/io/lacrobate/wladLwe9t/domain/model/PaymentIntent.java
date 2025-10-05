package io.lacrobate.wladLwe9t.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

/**
 * Domain model representing a payment intention.
 * Encapsulates business rules for payment processing.
 */
@Value
@Builder
public class PaymentIntent {
    String id;
    Long amount; // Amount in cents
    String currency;
    PaymentStatus status;
    CustomerInfo customerInfo;
    List<PaymentItem> items;
    String clientSecret;
    Instant createdAt;

    /**
     * Validates payment intent business rules.
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (!currency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Currency must be a valid 3-letter ISO code (e.g., EUR, USD)");
        }

        if (customerInfo == null) {
            throw new IllegalArgumentException("Customer information is required");
        }

        customerInfo.validate();

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }

        long calculatedAmount = items.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();

        if (!amount.equals(calculatedAmount)) {
            throw new IllegalArgumentException(
                    String.format("Amount mismatch: expected %d but got %d", calculatedAmount, amount)
            );
        }
    }

    /**
     * Checks if payment is in a final state (succeeded, canceled, or failed).
     */
    public boolean isFinalState() {
        return status == PaymentStatus.SUCCEEDED ||
                status == PaymentStatus.CANCELED ||
                status == PaymentStatus.FAILED;
    }

    @Value
    @Builder
    public static class CustomerInfo {
        String email;
        String firstName;
        String lastName;
        String address;
        String city;
        String postalCode;
        String phone;

        public void validate() {
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new IllegalArgumentException("Valid email is required");
            }

            if (firstName == null || firstName.trim().isEmpty()) {
                throw new IllegalArgumentException("First name is required");
            }

            if (lastName == null || lastName.trim().isEmpty()) {
                throw new IllegalArgumentException("Last name is required");
            }

            if (address == null || address.trim().isEmpty()) {
                throw new IllegalArgumentException("Address is required");
            }

            if (city == null || city.trim().isEmpty()) {
                throw new IllegalArgumentException("City is required");
            }

            if (postalCode == null || postalCode.trim().isEmpty()) {
                throw new IllegalArgumentException("Postal code is required");
            }
        }
    }

    @Value
    @Builder
    public static class PaymentItem {
        String id;
        String name;
        Integer quantity;
        Long price; // Price in cents

        public void validate() {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Item ID is required");
            }

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Item name is required");
            }

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than zero");
            }

            if (price == null || price < 0) {
                throw new IllegalArgumentException("Item price must be non-negative");
            }
        }
    }

    public enum PaymentStatus {
        REQUIRES_PAYMENT_METHOD,
        REQUIRES_CONFIRMATION,
        REQUIRES_ACTION,
        PROCESSING,
        SUCCEEDED,
        CANCELED,
        FAILED
    }
}
