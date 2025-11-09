package org.logistics.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.logistics.dto.SalesOrderDTO;
import org.logistics.entity.SalesOrder;
import org.logistics.service.SalesOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            boolean isAdmin = true; // simulate admin, replace with actual auth check
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
        // Check client role
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
    public ResponseEntity<?> reserveOrder(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean allowPartial, HttpSession session) {

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

}