package org.logistics.repository;

import org.logistics.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
    boolean existsByProductId(Long productId);
}