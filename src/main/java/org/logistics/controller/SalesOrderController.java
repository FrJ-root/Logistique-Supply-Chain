package org.logistics.controller;

import lombok.RequiredArgsConstructor;
import org.logistics.dto.SalesOrderDTO;
import org.logistics.entity.Client;
import org.logistics.entity.SalesOrder;
import org.logistics.entity.User;
import org.logistics.repository.ClientRepository;
import org.logistics.repository.UserRepository;
import org.logistics.security.CustomUserDetails;
import org.logistics.service.SalesOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    /**
     * Utilitaire pour récupérer le Client lié à l'utilisateur JWT actuel
     */
    private Client getCurrentClient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return clientRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé pour cet utilisateur"));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody SalesOrderDTO dto) {
        try {
            // On récupère l'ID du client depuis le token, pas depuis le body ou la session
            Client client = getCurrentClient();
            SalesOrder order = salesOrderService.createOrder(dto, client.getId());
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            // Vérifie si l'utilisateur est ADMIN via les autorités Spring Security
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            SalesOrder canceledOrder = salesOrderService.cancelOrder(id, isAdmin);
            return ResponseEntity.ok(canceledOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<?> reserveOrder(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean allowPartial) {
        try {
            // L'accès est déjà filtré par SecurityConfig (Role CLIENT requis)
            SalesOrder order = salesOrderService.reserveOrder(id, allowPartial);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/warehouse-reserve")
    public ResponseEntity<?> reserveOrderWarehouse(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "false") boolean allowPartial) {
        try {
            // L'accès est déjà filtré par SecurityConfig (Role WAREHOUSE_MANAGER requis)
            SalesOrder order = salesOrderService.reserveOrderWarehouse(id, allowPartial);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long orderId,
                                       @RequestParam Long shipmentId) {
        try {
            SalesOrder order = salesOrderService.shipOrder(orderId, shipmentId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<?> deliverOrder(@PathVariable Long orderId,
                                          @RequestParam Long shipmentId) {
        try {
            SalesOrder order = salesOrderService.deliverOrder(orderId, shipmentId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}