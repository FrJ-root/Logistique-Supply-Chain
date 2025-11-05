package org.logistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceptionBatchDTO {
    private Long purchaseOrderId;
    private Long warehouseId;
    private List<PurchaseReceptionItemDTO> items;
}