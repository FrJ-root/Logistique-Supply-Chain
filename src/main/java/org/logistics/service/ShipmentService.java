package org.logistics.service;

import org.logistics.repository.SalesOrderRepository;
import org.logistics.repository.ShipmentRepository;
import org.logistics.repository.CarrierRepository;
import org.springframework.stereotype.Service;
import org.logistics.enums.ShipmentStatus;
import jakarta.transaction.Transactional;
import org.logistics.entity.SalesOrder;
import org.logistics.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.logistics.entity.Shipment;
import org.logistics.entity.Carrier;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final SalesOrderRepository salesOrderRepository;
    private final ShipmentRepository shipmentRepository;
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

    private String generateTrackingNumber(Long orderId) {
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

    public Shipment updateStatus(Long shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));

        shipment.setStatus(newStatus);

        if (newStatus == ShipmentStatus.IN_TRANSIT) {
            shipment.setShippedDate(java.time.LocalDateTime.now());
        } else if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredDate(java.time.LocalDateTime.now());
        }

        return shipmentRepository.save(shipment);
    }

}