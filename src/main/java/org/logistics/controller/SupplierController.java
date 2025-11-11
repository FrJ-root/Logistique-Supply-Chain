package org.logistics.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.logistics.service.SupplierService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.logistics.dto.SupplierDTO;
import org.logistics.entity.Supplier;
import org.logistics.enums.Role;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService service;

    @PostMapping
    public ResponseEntity<?> createSupplier(@RequestBody SupplierDTO dto, HttpSession session) {
        Object role = session.getAttribute("role");
        if (role == null || !role.equals(Role.ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
        }

        Supplier supplier = service.create(dto);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping
    public ResponseEntity<List<Supplier>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

}