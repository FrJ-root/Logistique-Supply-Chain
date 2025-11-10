package org.logistics.repository;

import org.logistics.entity.Inventory;
import org.logistics.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
    List<Inventory> findByProduct(Product product);
}