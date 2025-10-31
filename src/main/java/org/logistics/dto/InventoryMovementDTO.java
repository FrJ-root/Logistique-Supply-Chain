package org.logistics.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementDTO {
    private Long id;
    private String type; // MovementType as String
    private Integer quantity;
    private LocalDateTime occurredAt;
    private String referenceDocument;
    private String description;
    private Long warehouseId;
    private Long productId;
}
