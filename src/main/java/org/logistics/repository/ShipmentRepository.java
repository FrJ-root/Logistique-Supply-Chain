package org.logistics.repository;

import org.logistics.entity.SalesOrder;
import org.logistics.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findBySalesOrder(SalesOrder order);
    Optional<Shipment> findBySalesOrderId(Long orderId);
}
