package org.logistics.service;

import org.logistics.repository.SalesOrderLineRepository;
import org.logistics.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.logistics.dto.ProductDTO;
import org.logistics.entity.Product;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final SalesOrderLineRepository orderLineRepo;

    public ProductService(ProductRepository repo, SalesOrderLineRepository orderLineRepo) {
        this.repo = repo;
        this.orderLineRepo = orderLineRepo;
    }

    private String generateSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Product create(ProductDTO dto) {
        Product p = Product.builder()
                .sku(generateSku())
                .name(dto.getName())
                .category(dto.getCategory())
                .originalPrice(dto.getOriginalPrice())
                .finalPrice(dto.getFinalPrice())
                .active(true)
                .build();

        return repo.save(p);
    }

    public List<Product> findAll() {
        return repo.findAll();
    }

    public Optional<Product> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<Product> update(Long id, ProductDTO dto) {
        return repo.findById(id)
                .map(p -> {
                    if (dto.getName() != null) p.setName(dto.getName());
                    if (dto.getCategory() != null) p.setCategory(dto.getCategory());
                    if (dto.getOriginalPrice() != null) p.setOriginalPrice(dto.getOriginalPrice());
                    if (dto.getFinalPrice() != null) p.setFinalPrice(dto.getFinalPrice());
                    return repo.save(p);
                });
    }

    public Optional<Product> deactivate(Long id) {
        return repo.findById(id).map(product -> {
            if (!product.isActive()) {
                throw new IllegalStateException("Product already inactive");
            }

            boolean hasOrders = orderLineRepo.existsByProductId(product.getId());
            if (hasOrders) {
                product.setActive(false);
            } else {
                product.setActive(false);
            }

            return repo.save(product);
        });
    }

    public Optional<Product> activate(Long id) {
        return repo.findById(id).map(product -> {
            if (product.isActive()) {
                throw new IllegalStateException("Product already active");
            }
            product.setActive(true);
            return repo.save(product);
        });
    }

    public Optional<ProductDTO> findBySku(String sku) {
        return repo.findBySku(sku)
                .map(p -> ProductDTO.builder()
                        .id(p.getId())
                        .sku(p.getSku())
                        .name(p.getName())
                        .category(p.getCategory())
                        .active(p.isActive())
                        .build());
    }

}