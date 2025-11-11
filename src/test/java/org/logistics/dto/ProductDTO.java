package org.logistics.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {

    private Long id;

    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Original price is required")
    @Positive(message = "Original price must be positive")
    private BigDecimal originalPrice;

    @NotNull(message = "Final price is required")
    @Positive(message = "Final price must be positive")
    private BigDecimal finalPrice;

    private boolean active = true;

}