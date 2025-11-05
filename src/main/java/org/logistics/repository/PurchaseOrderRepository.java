package org.logistics.repository;

import org.logistics.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.product", "supplier"})
    Optional<PurchaseOrder> findById(Long id);
}
