package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_lines")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    private Integer quantity;
    private BigDecimal unitPrice;

    @ManyToOne(optional = false)
    private SalesOrder salesOrder;

    // Custom Builder
    public static SalesOrderLineBuilder builder() { return new SalesOrderLineBuilder(); }

    public static class SalesOrderLineBuilder {
        private Long id;
        private Product product;
        private Integer quantity;
        private BigDecimal unitPrice;
        private SalesOrder salesOrder;

        public SalesOrderLineBuilder id(Long id) { this.id = id; return this; }
        public SalesOrderLineBuilder product(Product product) { this.product = product; return this; }
        public SalesOrderLineBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public SalesOrderLineBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public SalesOrderLineBuilder salesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; return this; }

        public SalesOrderLine build() {
            return new SalesOrderLine(id, product, quantity, unitPrice, salesOrder);
        }
    }
}
