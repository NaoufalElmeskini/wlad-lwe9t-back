package io.lacrobate.wladLwe9t.product;

import lombok.Builder;
import lombok.Getter;

@Builder
public class Price {
	@Getter
	private long productId;
	@Getter private double priceAmount;
	@Getter private double discount;
}