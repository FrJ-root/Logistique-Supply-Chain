package org.logistics.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDTO {
    private Long id;
    private Long supplierId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expectedDelivery;
    private List<PurchaseOrderLineDTO> lines;
}
