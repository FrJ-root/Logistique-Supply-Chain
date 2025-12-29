package org.logistics.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Transactional;
import org.logistics.repository.InventoryMovementRepository;
import org.logistics.repository.InventoryRepository;
import org.logistics.repository.WarehouseRepository;
import org.logistics.repository.ProductRepository;
import org.logistics.entity.InventoryMovement;
import org.logistics.dto.InventoryMovementDTO;
import org.springframework.stereotype.Service;
import org.logistics.enums.MovementType;
import lombok.RequiredArgsConstructor;
import org.logistics.entity.Inventory;
import org.logistics.entity.Warehouse;
import org.logistics.entity.Product;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    @Transactional
    public InventoryMovement recordInbound(InventoryMovementDTO dto) {

        // Ajout au contexte MDC pour Elasticsearch
        MDC.put("productId", dto.getProductId().toString());
        log.info("Réception de stock effectuée. Quantité: {}", dto.getQuantity());

        if (dto.getQuantity() <= 0) {
            throw new RuntimeException("La quantité doit être positive");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));

        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndProductId(warehouse.getId(), product.getId())
                .orElseGet(() -> Inventory.builder()
                        .warehouse(warehouse)
                        .product(product)
                        .qtyOnHand(0)
                        .qtyReserved(0)
                        .build()
                );

        inventory.setQtyOnHand(inventory.getQtyOnHand() + dto.getQuantity());
        inventoryRepository.save(inventory);

        InventoryMovement movement = InventoryMovement.builder()
                .type(MovementType.INBOUND)
                .quantity(dto.getQuantity())
                .occurredAt(LocalDateTime.now())
                .referenceDocument(dto.getReferenceDocument())
                .description(dto.getDescription())
                .warehouse(warehouse)
                .product(product)
                .build();

        return movementRepository.save(movement);
        // 4. Suppression du deuxième return qui bloquait la compilation
    }

    @Transactional
    public InventoryMovement recordOutbound(InventoryMovementDTO dto) {
        if (dto.getQuantity() <= 0) {
            throw new RuntimeException("La quantité doit être positive");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));

        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndProductId(warehouse.getId(), product.getId())
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour ce produit dans cet entrepôt"));

        if (inventory.getQtyOnHand() < dto.getQuantity()) {
            throw new RuntimeException("Stock insuffisant");
        }

        inventory.setQtyOnHand(inventory.getQtyOnHand() - dto.getQuantity());
        inventoryRepository.save(inventory);

        InventoryMovement movement = InventoryMovement.builder()
                .type(MovementType.OUTBOUND)
                .quantity(dto.getQuantity())
                .occurredAt(LocalDateTime.now())
                .referenceDocument(dto.getReferenceDocument())
                .description(dto.getDescription())
                .warehouse(warehouse)
                .product(product)
                .build();

        return movementRepository.save(movement);
    }

    @Transactional
    public InventoryMovement recordAdjustment(InventoryMovementDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Entrepôt non trouvé"));

        Inventory inventory = inventoryRepository
                .findByWarehouseIdAndProductId(warehouse.getId(), product.getId())
                .orElseThrow(() -> new RuntimeException("Aucun stock trouvé pour ce produit dans cet entrepôt"));

        int current = inventory.getQtyOnHand();
        int reserved = inventory.getQtyReserved();
        int adjusted = current + dto.getQuantity();

        if (adjusted < reserved) {
            throw new RuntimeException(
                    "Impossible: qtyOnHand ne peut pas être < qtyReserved (" + reserved + ")"
            );
        }

        inventory.setQtyOnHand(adjusted);
        inventoryRepository.save(inventory);

        InventoryMovement movement = InventoryMovement.builder()
                .type(MovementType.ADJUSTMENT)
                .quantity(dto.getQuantity())
                .occurredAt(LocalDateTime.now())
                .referenceDocument(dto.getReferenceDocument())
                .description(dto.getDescription())
                .warehouse(warehouse)
                .product(product)
                .build();

        return movementRepository.save(movement);
    }

}