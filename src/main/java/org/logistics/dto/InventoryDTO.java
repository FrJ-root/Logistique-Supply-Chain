package org.logistics.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDTO {
    private Long id;
    private Long warehouseId;
    private Long productId;
    private Integer qtyOnHand;
    private Integer qtyReserved;
}
