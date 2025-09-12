package io.lacrobate.wladLwe9t.product;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class PriceClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(PriceClient.class);
	private static final String baseUrl = "http://price-api:8082/api2";
	private final RestTemplate restTemplate;

	public Price getPrice(long productId){
		LOGGER.info("Fetching Price Details With Product Id {}", productId);
		String url = String.format("%s/price/%d", baseUrl, productId);
		ResponseEntity<Price> price = restTemplate.getForEntity(url, Price.class);
		return price.getBody();
	}
}
