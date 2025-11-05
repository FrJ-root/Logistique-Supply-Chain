package org.logistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceptionItemDTO {
    private Long lineId;
    private Integer quantityReceived;
}
