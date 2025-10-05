package io.lacrobate.wladLwe9t.infrastructure.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe SDK configuration.
 * Initializes Stripe with secret API key from environment.
 */
@Configuration
public class StripeConfig {

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
