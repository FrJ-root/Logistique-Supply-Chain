package org.logistics.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.logistics.dto.PurchaseOrderDTO;
import org.logistics.dto.PurchaseReceptionBatchDTO;
import org.logistics.enums.Role;
import org.logistics.service.PurchaseOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    private boolean requireAdmin(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && role.toString().equals(Role.ADMIN.name());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PurchaseOrderDTO dto, HttpSession session) {
        if (!requireAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }
        try {
            PurchaseOrderDTO created = service.create(dto);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, HttpSession session) {
        if (!requireAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }
        try {
            PurchaseOrderDTO approved = service.approve(id);
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/receive")
    public ResponseEntity<?> receive(@RequestBody PurchaseReceptionBatchDTO batch, HttpSession session) {
        if (!requireAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }
        try {
            PurchaseOrderDTO received = service.receive(batch);
            return ResponseEntity.ok(received);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
