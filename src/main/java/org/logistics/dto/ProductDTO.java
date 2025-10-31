package org.logistics.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    private boolean active;
}
