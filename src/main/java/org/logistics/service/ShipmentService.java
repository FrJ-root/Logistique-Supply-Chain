package org.logistics.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.logistics.entity.*;
import org.logistics.enums.MovementType;
import org.logistics.enums.OrderStatus;
import org.logistics.enums.ShipmentStatus;
import org.logistics.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryRepository inventoryRepository;
    private final ShipmentRepository shipmentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CarrierRepository carrierRepository;
    private static final int CUT_OFF_HOUR = 15;

    @Transactional
    public Shipment createShipment(Long orderId, Long carrierId) {

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new RuntimeException("La commande doit être RESERVED pour créer une expédition");
        }

        shipmentRepository.findBySalesOrder(order).ifPresent(s -> {
            throw new RuntimeException("Un Shipment existe déjà pour cette commande");
        });

        Carrier carrier = carrierRepository.findById(carrierId)
                .orElseThrow(() -> new RuntimeException("Transporteur non trouvé"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime planned = now;

        int cutoff = carrier.getCutoffTime() != null ? carrier.getCutoffTime() : 15;

        if (now.toLocalTime().isAfter(LocalTime.of(cutoff, 0))) {
            planned = nextWorkingDay(now).withHour(9).withMinute(0);
        }

        Shipment shipment = Shipment.builder()
                .trackingNumber(generateTrackingNumber(orderId))
                .status(ShipmentStatus.PLANNED)
                .plannedDate(planned)
                .carrier(carrier)
                .salesOrder(order)
                .build();

        return shipmentRepository.save(shipment);
    }

    String generateTrackingNumber(Long orderId) {
        return "TRK-" + orderId + "-" + System.currentTimeMillis();
    }

    private LocalDateTime nextWorkingDay(LocalDateTime dt) {
        LocalDateTime next = dt.plusDays(1);
        while (next.getDayOfWeek().getValue() >= 6) {
            next = next.plusDays(1);
        }
        return next;
    }

    public Shipment getShipmentForOrder(Long orderId, Long clientId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (!order.getClient().getId().equals(clientId)) {
            throw new RuntimeException("Accès interdit : cette commande n'appartient pas au client");
        }

        return order.getLines().isEmpty()
                ? null
                : shipmentRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Aucune expédition associée à cette commande"));
    }

    @Transactional
    public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));

        SalesOrder order = shipment.getSalesOrder();

        switch (newStatus) {

            case IN_TRANSIT:
                if (order.getStatus() != OrderStatus.RESERVED) {
                    throw new RuntimeException("La commande doit être RESERVED pour commencer l'expédition");
                }

                // Consume inventory reservation
                for (SalesOrderLine line : order.getLines()) {
                    int qty = line.getQuantity();

                    // Fetch inventories holding reservations of the product
                    List<Inventory> inventories = inventoryRepository.findByProduct(line.getProduct());

                    for (Inventory inv : inventories) {

                        int reservedToConsume = Math.min(inv.getQtyReserved(), qty);

                        inv.setQtyReserved(inv.getQtyReserved() - reservedToConsume);
                        inv.setQtyOnHand(inv.getQtyOnHand() - reservedToConsume);

                        // Historize outbound movement
                        inventoryMovementRepository.save(
                                InventoryMovement.builder()
                                        .type(MovementType.OUTBOUND)
                                        .quantity(reservedToConsume)
                                        .occurredAt(LocalDateTime.now())
                                        .referenceDocument(shipment.getTrackingNumber())
                                        .description("Shipment dispatch")
                                        .warehouse(inv.getWarehouse())
                                        .product(line.getProduct())
                                        .inventory(inv)
                                        .build()
                        );

                        qty -= reservedToConsume;
                        inventoryRepository.save(inv);

                        if (qty <= 0) break;
                    }
                }

                order.setStatus(OrderStatus.SHIPPED);
                shipment.setShippedDate(LocalDateTime.now());
                break;

            case DELIVERED:
                if (order.getStatus() != OrderStatus.SHIPPED) {
                    throw new RuntimeException("La commande doit être SHIPPED avant d'être livrée");
                }

                order.setStatus(OrderStatus.DELIVERED);
                shipment.setDeliveredDate(LocalDateTime.now());
                break;

            default:
                throw new RuntimeException("Transition non supportée");
        }

        shipment.setStatus(newStatus);

        salesOrderRepository.save(order);
        return shipmentRepository.save(shipment);
    }

}