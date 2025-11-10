package org.logistics.repository;

import org.logistics.entity.SalesOrderLine;
import org.logistics.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
    boolean existsByProductId(Long productId);
}