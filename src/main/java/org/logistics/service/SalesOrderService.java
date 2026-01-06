package org.logistics.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.logistics.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.logistics.dto.SalesOrderLineDTO;
import org.logistics.enums.ShipmentStatus;
import org.logistics.enums.MovementType;
import org.logistics.dto.SalesOrderDTO;
import org.logistics.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.logistics.repository.*;
import java.time.LocalDateTime;
import org.logistics.entity.*;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final ShipmentRepository shipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InventoryMovementRepository movementRepository;

    private static final int CUT_OFF_HOUR = 15;
    private static final int RESERVATION_TTL_HOURS = 24;

    @Transactional
    public SalesOrder cancelOrder(Long orderId, boolean isAdmin) {
        if (!isAdmin) {
            throw new RuntimeException("Only admins can cancel orders");
        }

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        switch (order.getStatus()) {
            case CREATED:
                order.setStatus(OrderStatus.CANCELED);
                break;

            case RESERVED:
                for (SalesOrderLine line : order.getLines()) {
                    List<Inventory> inventories = inventoryRepository.findByProduct(line.getProduct());
                    for (Inventory inventory : inventories) {
                        int newReserved = inventory.getQtyReserved() - line.getQuantity();
                        inventory.setQtyReserved(Math.max(newReserved, 0));
                        inventoryRepository.save(inventory);
                    }
                }
                order.setStatus(OrderStatus.CANCELED);
                break;

            case SHIPPED:
            case DELIVERED:
                throw new RuntimeException("Cannot cancel a shipped or delivered order, use return process");
        }

        return salesOrderRepository.save(order);
    }

    @Transactional
    public SalesOrder createOrder(SalesOrderDTO dto, Long clientId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new RuntimeException("La commande doit contenir au moins une ligne.");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        SalesOrder order = SalesOrder.builder()
                .client(client)
                .status(OrderStatus.CREATED)
                .createdAt(java.time.LocalDateTime.now())
                .lines(new java.util.ArrayList<>())
                .build();

        for (SalesOrderLineDTO lineDTO : dto.getLines()) {
            Product product = productRepository.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit inexistant : " + lineDTO.getProductId()));

            if (!product.isActive()) {
                throw new RuntimeException("Produit inactif : " + product.getName());
            }

            SalesOrderLine line = new SalesOrderLine();
            line.setProduct(product);
            line.setQuantity(lineDTO.getQuantity());
            line.setUnitPrice(lineDTO.getUnitPrice());
            line.setSalesOrder(order);

            order.getLines().add(line);
        }

        return salesOrderRepository.save(order);
    }

    @Transactional
    public SalesOrder reserveOrder(Long orderId, boolean allowPartial) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("La commande doit être en statut CREATED pour être réservée");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plannedReservationTime = now;
        if (now.toLocalTime().isAfter(LocalTime.of(CUT_OFF_HOUR, 0))) {
            plannedReservationTime = nextWorkingDay(now).withHour(9).withMinute(0);
            order.setReservedAt(plannedReservationTime);
        } else {
            order.setReservedAt(now);
        }

        boolean allAvailable = true;
        for (SalesOrderLine line : order.getLines()) {
            List<Inventory> inventories = inventoryRepository.findByProduct(line.getProduct());
            int totalAvailable = inventories.stream().mapToInt(inv -> inv.getQtyOnHand() - inv.getQtyReserved()).sum();
            if (totalAvailable < line.getQuantity()) {
                allAvailable = false;
            }
        }

        if (!allAvailable) {
            throw new RuntimeException("Stock insuffisant pour toutes les lignes de la commande");
        }

        for (SalesOrderLine line : order.getLines()) {
            List<Inventory> inventories = inventoryRepository.findByProduct(line.getProduct());
            int qtyToReserve = line.getQuantity();
            for (Inventory inv : inventories) {
                int available = inv.getQtyOnHand() - inv.getQtyReserved();
                int reservedNow = Math.min(available, qtyToReserve);
                inv.setQtyReserved(inv.getQtyReserved() + reservedNow);
                inventoryRepository.save(inv);
                qtyToReserve -= reservedNow;
                if (qtyToReserve <= 0) break;
            }
        }

        order.setStatus(OrderStatus.RESERVED);
        order.setReservedAt(now);
        return salesOrderRepository.save(order);
    }

    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusHours(RESERVATION_TTL_HOURS);
        List<SalesOrder> reservedOrders = salesOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.RESERVED)
                .toList();

        for (SalesOrder order : reservedOrders) {
            if (order.getReservedAt() != null && order.getReservedAt().isBefore(expirationThreshold)) {
                releaseStock(order);
                order.setStatus(OrderStatus.CREATED);
                order.setReservedAt(null);
                salesOrderRepository.save(order);
            }
        }
    }

    private void releaseStock(SalesOrder order) {
        for (SalesOrderLine line : order.getLines()) {
            List<Inventory> inventories = inventoryRepository.findByProduct(line.getProduct());
            for (Inventory inv : inventories) {
                inv.setQtyReserved(Math.max(inv.getQtyReserved() - line.getQuantity(), 0));
                inventoryRepository.save(inv);
            }
        }
    }

    private LocalDateTime nextWorkingDay(LocalDateTime dateTime) {
        LocalDateTime nextDay = dateTime.plusDays(1);
        while (nextDay.getDayOfWeek().getValue() >= 6) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    @Transactional
    public SalesOrder reserveOrderWarehouse(Long orderId, boolean allowPartial) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("Seules les commandes CREATED peuvent être réservées");
        }

        boolean fullyCovered = true;

        for (SalesOrderLine line : order.getLines()) {
            List<Inventory> inventories = inventoryRepository.findByProduct(line.getProduct());
            int available = inventories.stream()
                    .mapToInt(inv -> inv.getQtyOnHand() - inv.getQtyReserved())
                    .sum();

            if (available < line.getQuantity()) {
                fullyCovered = false;
            }
        }

        if (!fullyCovered && !allowPartial) {
            throw new RuntimeException("Stock insuffisant, réservation partielle interdite");
        }

        for (SalesOrderLine line : order.getLines()) {
            int qtyToReserve = line.getQuantity();
            List<Inventory> inventories = inventoryRepository.findByProduct(line.getProduct());

            for (Inventory inv : inventories) {
                int available = inv.getQtyOnHand() - inv.getQtyReserved();
                int reservedNow = Math.min(available, qtyToReserve);

                inv.setQtyReserved(inv.getQtyReserved() + reservedNow);
                inventoryRepository.save(inv);

                qtyToReserve -= reservedNow;
                if (qtyToReserve <= 0) break;
            }

            if (qtyToReserve > 0) {
                line.setBackorderedQty(qtyToReserve);
            }
        }

        order.setStatus(fullyCovered ? OrderStatus.RESERVED : OrderStatus.CREATED);
        order.setReservedAt(LocalDateTime.now());

        return salesOrderRepository.save(order);
    }

    @Transactional
    public SalesOrder shipOrder(Long orderId, Long shipmentId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new RuntimeException("La commande doit être RESERVED pour être expédiée");
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));

        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setShippedDate(java.time.LocalDateTime.now());
        shipmentRepository.save(shipment);

        for (SalesOrderLine line : order.getLines()) {
            List<Inventory> inventories = inventoryRepository.findByProduct(line.getProduct());
            int qtyToShip = line.getQuantity();

            for (Inventory inv : inventories) {
                int availableReserved = inv.getQtyReserved();
                int toDecrease = Math.min(availableReserved, qtyToShip);
                if (toDecrease > 0) {
                    inv.setQtyReserved(inv.getQtyReserved() - toDecrease);
                    inv.setQtyOnHand(inv.getQtyOnHand() - toDecrease);
                    inventoryRepository.save(inv);

                    InventoryMovement movement = InventoryMovement.builder()
                            .type(MovementType.OUTBOUND)
                            .quantity(toDecrease)
                            .occurredAt(java.time.LocalDateTime.now())
                            .referenceDocument("Shipment-" + shipment.getId())
                            .description("Shipment of order " + order.getId())
                            .warehouse(inv.getWarehouse())
                            .product(inv.getProduct())
                            .build();
                    movementRepository.save(movement);

                    qtyToShip -= toDecrease;
                    if (qtyToShip <= 0) break;
                }
            }

            if (qtyToShip > 0) {
                throw new RuntimeException("Stock insuffisant pour expédier la ligne produit " + line.getProduct().getName());
            }
        }

        order.setStatus(OrderStatus.SHIPPED);
        return salesOrderRepository.save(order);
    }

    @Transactional
    public SalesOrder deliverOrder(Long orderId, Long shipmentId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Expédition non trouvée"));

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredDate(java.time.LocalDateTime.now());
        shipmentRepository.save(shipment);

        order.setStatus(OrderStatus.DELIVERED);
        return salesOrderRepository.save(order);
    }

    public void checkOwnership(SalesOrder order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            String keycloakId = jwt.getSubject();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !order.getClient().getUser().getKeycloakId().equals(keycloakId)) {
                throw new RuntimeException("Accès refusé : ce n'est pas votre commande !");
            }
        }
    }

    private void validateOwnership(SalesOrder order) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;

        if (auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            String currentKeycloakId = jwt.getSubject();

            if (!order.getClient().getUser().getKeycloakId().equals(currentKeycloakId)) {
                throw new RuntimeException("Accès refusé : ressource appartenant à un autre utilisateur.");
            }
        }
    }

    @Transactional
    public SalesOrder getOrderDetails(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée"));

        validateOwnership(order);

        return order;
    }

}