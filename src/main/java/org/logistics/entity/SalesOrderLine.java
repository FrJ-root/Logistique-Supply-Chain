package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_lines")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesOrderLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    private Integer quantity;
    private BigDecimal unitPrice;

    @ManyToOne(optional = false)
    private SalesOrder salesOrder;
}
