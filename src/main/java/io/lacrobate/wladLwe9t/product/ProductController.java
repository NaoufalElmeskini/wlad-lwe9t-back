package io.lacrobate.wladLwe9t.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("product")
public class ProductController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

	@Autowired
	private PriceClient priceClient;

	@Autowired
	private SimpleProductRepository productRepository;

	@GetMapping(path = "/{id}")
	public Product getProductDetails(@PathVariable("id") long productId){
		LOGGER.info("Getting Product and Price Details with Product Id {}", productId);
		Product product = productRepository.getProduct(productId);
		product.setPrice(priceClient.getPrice(productId));
		return product;
	}
}
