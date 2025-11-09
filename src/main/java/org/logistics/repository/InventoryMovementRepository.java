package org.logistics.repository;

import org.logistics.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    boolean existsByWarehouseId(Long warehouseId);
    List<InventoryMovement> findByProductId(Long productId);
    List<InventoryMovement> findByWarehouseId(Long warehouseId);
    List<InventoryMovement> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}