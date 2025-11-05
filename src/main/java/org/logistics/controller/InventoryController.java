package org.logistics.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.logistics.dto.InventoryMovementDTO;
import org.logistics.entity.InventoryMovement;
import org.logistics.enums.Role;
import org.logistics.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    private boolean requireWarehouseManager(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && role.toString().equals(Role.WAREHOUSE_MANAGER.name());
    }

    @PostMapping("/inbound")
    public ResponseEntity<?> recordInbound(@RequestBody InventoryMovementDTO dto, HttpSession session) {
        if (!requireWarehouseManager(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès gestionnaire entrepôt requis"));
        }

        try {
            InventoryMovement movement = inventoryService.recordInbound(dto);
            return ResponseEntity.ok(movement);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/outbound")
    public ResponseEntity<?> recordOutbound(@RequestBody InventoryMovementDTO dto, HttpSession session) {
        if (!requireWarehouseManager(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès gestionnaire entrepôt requis"));
        }

        try {
            InventoryMovement movement = inventoryService.recordOutbound(dto);
            return ResponseEntity.ok(movement);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/adjustment")
    public ResponseEntity<?> recordAdjustment(@RequestBody InventoryMovementDTO dto, HttpSession session) {
        if (!requireWarehouseManager(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès gestionnaire entrepôt requis"));
        }

        try {
            InventoryMovement movement = inventoryService.recordAdjustment(dto);
            return ResponseEntity.ok(movement);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}