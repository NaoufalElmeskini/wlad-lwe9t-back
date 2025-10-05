package io.lacrobate.wladLwe9t.application.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for payment intent operations.
 * Contains client secret for frontend to complete payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String clientSecret;
    private String paymentIntentId;
    private Long amount;
    private String currency;
    private String status;
}
