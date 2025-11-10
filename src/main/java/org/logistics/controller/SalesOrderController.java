package org.logistics.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.logistics.dto.SalesOrderDTO;
import org.logistics.entity.SalesOrder;
import org.logistics.repository.SalesOrderRepository;
import org.logistics.service.SalesOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final SalesOrderRepository salesOrderRepository;

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            boolean isAdmin = true;
            SalesOrder canceledOrder = salesOrderService.cancelOrder(id, isAdmin);
            return ResponseEntity.ok(canceledOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody SalesOrderDTO dto, HttpSession session) {
        Object role = session.getAttribute("role");
        Object clientId = session.getAttribute("userId");
        if (role == null || !role.toString().equals("CLIENT")) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès client requis"));
        }

        try {
            SalesOrder order = salesOrderService.createOrder(dto, (Long) clientId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<?> reserveOrder(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean allowPartial,
                                          HttpSession session) {

        Object role = session.getAttribute("role");
        Object clientId = session.getAttribute("userId");
        if (role == null || !role.toString().equals("CLIENT")) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès client requis"));
        }

        try {
            SalesOrder order = salesOrderService.reserveOrder(id, allowPartial);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/warehouse-reserve")
    public ResponseEntity<?> reserveOrderWarehouse(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "false") boolean allowPartial,
                                                   HttpSession session) {

        Object role = session.getAttribute("role");
        if (role == null || !role.toString().equals("WAREHOUSE_MANAGER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès gestionnaire entrepôt requis"));
        }

        try {
            SalesOrder order = salesOrderService.reserveOrderWarehouse(id, allowPartial);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long orderId,
                                       @RequestParam Long shipmentId,
                                       HttpSession session) {

        Object role = session.getAttribute("role");
        if (role == null || !role.toString().equals("WAREHOUSE_MANAGER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès gestionnaire requis"));
        }

        try {
            SalesOrder order = salesOrderService.shipOrder(orderId, shipmentId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<?> deliverOrder(@PathVariable Long orderId,
                                          @RequestParam Long shipmentId,
                                          HttpSession session) {

        Object role = session.getAttribute("role");
        if (role == null || !role.toString().equals("WAREHOUSE_MANAGER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès gestionnaire requis"));
        }

        try {
            SalesOrder order = salesOrderService.deliverOrder(orderId, shipmentId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, HttpSession session) {
        Object roleObj = session.getAttribute("role");
        Object userIdObj = session.getAttribute("userId");

        if (roleObj == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Non connecté"));
        }

        String role = roleObj.toString();
        Long userId = (userIdObj instanceof Number) ? ((Number) userIdObj).longValue() : null;

        SalesOrder order = salesOrderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if ("CLIENT".equals(role)) {
            if (!order.getClient().getId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Accès interdit : cette commande n'appartient pas au client"));
            }
        } else if ("WAREHOUSE_MANAGER".equals(role)) {
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Accès non autorisé"));
        }

        return ResponseEntity.ok(order);
    }

}