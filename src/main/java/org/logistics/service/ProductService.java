package org.logistics.service;

import org.logistics.repository.InventoryMovementRepository;
import org.logistics.exception.ResourceNotFoundException;
import org.logistics.repository.SalesOrderLineRepository;
import org.logistics.repository.InventoryRepository;
import org.logistics.repository.ProductRepository;
import org.logistics.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.logistics.enums.OrderStatus;
import org.logistics.entity.Inventory;
import org.logistics.dto.ProductDTO;
import org.logistics.entity.Product;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository repo;
    private final SalesOrderLineRepository orderLineRepo;
    private InventoryRepository inventoryRepository;

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

    //   -> mise en situation

    public void deactivateProduct(String sku) {
        Product product = repo.findBySku(sku).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + sku));

        if (!product.isActive()) {
            throw new BusinessException("Product deja inactive");
        }

        long activeOrdersCount = orderLineRepo.countByProduct_SkuAndOrder_StatusIn(sku, List.of(OrderStatus.CREATED, OrderStatus.RESERVED));

        if (activeOrdersCount > 0) {
            throw new BusinessException("Ooops! Cannot deactivate cs product is associated with active orders");
        }

        List<Inventory> inventoryList = inventoryRepository.findByProduct_Sku(sku);

        int totalReserved = inventoryList.stream()
                .mapToInt(Inventory::getQtyReserved)
                .sum();

        if (totalReserved > 0) {
            throw new BusinessException("Oops! Cannot deactivate cs reserved stock exists");
        }

        product.setActive(false);
        repo.save(product);
    }

}