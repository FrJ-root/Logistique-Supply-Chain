package org.logistics.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.logistics.dto.ShipmentDTO;
import org.logistics.entity.Shipment;
import org.logistics.enums.ShipmentStatus;
import org.logistics.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getShipmentByOrder(@PathVariable Long orderId, HttpSession session) {
        Object role = session.getAttribute("role");
        Object clientId = session.getAttribute("userId");

        if (role == null || (!role.toString().equals("CLIENT") && !role.toString().equals("WAREHOUSE_MANAGER"))) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès client ou gestionnaire entrepôt requis"));
        }

        try {
            Shipment shipment = shipmentService.getShipmentForOrder(orderId,
                    role.toString().equals("CLIENT") ? (Long) clientId : null);
            ShipmentDTO dto = ShipmentDTO.builder()
                    .id(shipment.getId())
                    .trackingNumber(shipment.getTrackingNumber())
                    .status(shipment.getStatus().name())
                    .plannedDate(shipment.getPlannedDate())
                    .shippedDate(shipment.getShippedDate())
                    .deliveredDate(shipment.getDeliveredDate())
                    .carrierId(shipment.getCarrier().getId())
                    .build();
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{id}/update-status")
    public ResponseEntity<?> updateShipmentStatus(@PathVariable Long id, @RequestParam ShipmentStatus status) {
        try {
            Shipment updated = shipmentService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createShipment(@RequestParam Long orderId, @RequestParam Long carrierId, HttpSession session) {

        Object role = session.getAttribute("role");
        if (role == null || !role.toString().equals("WAREHOUSE_MANAGER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès gestionnaire requis"));
        }

        try {
            Shipment shipment = shipmentService.createShipment(orderId, carrierId);
            return ResponseEntity.ok(shipment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}