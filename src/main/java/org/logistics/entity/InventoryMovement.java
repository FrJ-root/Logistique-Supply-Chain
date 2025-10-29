package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.MovementType;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}
