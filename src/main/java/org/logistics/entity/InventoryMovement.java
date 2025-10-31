package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.MovementType;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private Integer quantity;

    private LocalDateTime occurredAt;

    private String referenceDocument;

    private String description;

    @ManyToOne(optional = false)
    private Warehouse warehouse;

    @ManyToOne(optional = false)
    private Product product;

    // Custom Builder
    public static InventoryMovementBuilder builder() { return new InventoryMovementBuilder(); }

    public static class InventoryMovementBuilder {
        private Long id;
        private MovementType type;
        private Integer quantity;
        private LocalDateTime occurredAt;
        private String referenceDocument;
        private String description;
        private Warehouse warehouse;
        private Product product;

        public InventoryMovementBuilder id(Long id) { this.id = id; return this; }
        public InventoryMovementBuilder type(MovementType type) { this.type = type; return this; }
        public InventoryMovementBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public InventoryMovementBuilder occurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; return this; }
        public InventoryMovementBuilder referenceDocument(String referenceDocument) { this.referenceDocument = referenceDocument; return this; }
        public InventoryMovementBuilder description(String description) { this.description = description; return this; }
        public InventoryMovementBuilder warehouse(Warehouse warehouse) { this.warehouse = warehouse; return this; }
        public InventoryMovementBuilder product(Product product) { this.product = product; return this; }

        public InventoryMovement build() {
            return new InventoryMovement(id, type, quantity, occurredAt, referenceDocument, description, warehouse, product);
        }
    }
}
