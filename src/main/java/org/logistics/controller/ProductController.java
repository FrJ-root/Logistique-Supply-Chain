package org.logistics.controller;

import jakarta.servlet.http.HttpSession;
import org.logistics.dto.ProductDTO;
import org.logistics.enums.Role;
import org.logistics.mapper.ProductMapper;
import org.logistics.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    private final ProductMapper mapper;

    public ProductController(ProductService service, ProductMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    private boolean requireAdmin(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && role.toString().equals(Role.ADMIN.name());
    }

    private boolean requireClient(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && role.toString().equals("CLIENT");
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<?> getBySku(@PathVariable String sku, HttpSession session) {
        if (!requireClient(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès client requis"));
        }

        return service.findBySku(sku)
                .map(dto -> {
                    if (!dto.isActive()) {
                        return ResponseEntity.ok(Map.of(
                                "sku", dto.getSku(),
                                "name", dto.getName(),
                                "category", dto.getCategory(),
                                "active", false,
                                "availability", "Indisponible à la vente"
                        ));
                    }

                    // Active product
                    return ResponseEntity.ok(Map.of(
                            "sku", dto.getSku(),
                            "name", dto.getName(),
                            "category", dto.getCategory(),
                            "active", true,
                            "availability", "Disponible"
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Produit non trouvé")));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductDTO dto, HttpSession session) {
        if (!requireAdmin(session))
            return ResponseEntity.status(403).body(Map.of("error", "Admins only"));

        return ResponseEntity.ok(mapper.toDTO(service.create(dto)));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(
                service.findAll().stream().map(mapper::toDTO).toList()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductDTO dto, HttpSession session) {
        if (!requireAdmin(session))
            return ResponseEntity.status(403).body(Map.of("error", "Admins only"));

        return service.update(id, dto)
                .map(mapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body((ProductDTO) Map.of("error", "Product not found")));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Long id, HttpSession session) {
        if (!requireAdmin(session))
            return ResponseEntity.status(403).body(Map.of("error", "Not allowed"));

        try {
            var result = service.deactivate(id);
            if (result.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
            }
            return ResponseEntity.ok(Map.of(
                    "message", "Product deactivated successfully",
                    "product", result.get()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id, HttpSession session) {
        if (!requireAdmin(session))
            return ResponseEntity.status(403).body(Map.of("error", "Not allowed"));

        try {
            var result = service.activate(id);
            if (result.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
            }
            return ResponseEntity.ok(result.get());

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}