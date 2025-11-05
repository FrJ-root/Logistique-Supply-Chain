package org.logistics.repository;

import org.logistics.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    boolean existsByWarehouseId(Long warehouseId);
}
