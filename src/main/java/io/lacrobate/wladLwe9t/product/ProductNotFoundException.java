package io.lacrobate.wladLwe9t.product;

public class ProductNotFoundException extends RuntimeException {
	public ProductNotFoundException(String priceNotFound) {
		super(priceNotFound);
	}
}
