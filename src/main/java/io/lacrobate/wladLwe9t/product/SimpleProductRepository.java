package io.lacrobate.wladLwe9t.product;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class SimpleProductRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProductRepository.class);
	private static final Long CORRECT_ID = 1l;
	private HashMap<Long, Product> productMap = new HashMap<>();
	@PostConstruct
	public void init() {
		var aProductItem = StaticPriceFactory.createWithId(CORRECT_ID);
		productMap.put(aProductItem.getId(), aProductItem);
	}
	public Product getProduct(Long productId) {
		LOGGER.info("Getting Product from Product Repo With Product Id {}", productId);
		if(!productMap.containsKey(productId)){
			LOGGER.error("Product Not Found for Product Id {}", productId);
			throw new ProductNotFoundException("Product Not Found");
		}
		return productMap.get(productId);
	}

	private static class StaticPriceFactory {
		public static Product createWithId(Long id) {
			return Product.builder()
					.id(id)
					.name("cool product!")
					.build();
		}
	}
}
