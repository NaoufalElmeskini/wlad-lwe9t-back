package io.lacrobate.wladLwe9t.application.payment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating a payment intent.
 * Validates input from API clients.
 */
@Data
public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1 cent")
    private Long amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code (e.g., EUR, USD)")
    private String currency;

    @NotNull(message = "Customer information is required")
    @Valid
    private CustomerInfoDTO customerInfo;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PaymentItemDTO> items;

    @Data
    public static class CustomerInfoDTO {

        @NotBlank(message = "Email is required")
        @Email(message = "Valid email address is required")
        private String email;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "Postal code is required")
        private String postalCode;

        private String phone;
    }

    @Data
    public static class PaymentItemDTO {

        @NotBlank(message = "Item ID is required")
        private String id;

        @NotBlank(message = "Item name is required")
        private String name;

        @NotNull(message = "Item quantity is required")
        @Min(value = 1, message = "Item quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Item price is required")
        @Min(value = 0, message = "Item price must be non-negative")
        private Long price;
    }
}
