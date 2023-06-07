package cart.service;

import cart.domain.Product;
import cart.dto.ProductRequest;
import cart.dto.ProductResponse;
import cart.exception.ExceptionType;
import cart.exception.ProductException;
import cart.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Long register(ProductRequest productRequest) {
        Product product = new Product(
                productRequest.getName(),
                productRequest.getPrice(),
                productRequest.getImageUrl()
        );
        return productRepository.save(product).getId();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ExceptionType.NOT_FOUND_PRODUCT));
        return ProductResponse.of(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(ProductResponse::of)
                .collect(Collectors.toList());
    }

    public void modify(Long productId, ProductRequest productRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ExceptionType.NOT_FOUND_PRODUCT));
        product.update(productRequest.getName(), productRequest.getPrice(), productRequest.getImageUrl());
        productRepository.update(product);
    }

    public void remove(Long productId) {
        productRepository.delete(productId);
    }
}
