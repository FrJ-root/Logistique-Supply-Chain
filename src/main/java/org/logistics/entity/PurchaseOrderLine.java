package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_lines")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    private Integer quantity;
    private BigDecimal unitPrice;

    @ManyToOne(optional = false)
    private PurchaseOrder purchaseOrder;

    // Custom Builder
    public static PurchaseOrderLineBuilder builder() { return new PurchaseOrderLineBuilder(); }

    public static class PurchaseOrderLineBuilder {
        private Long id;
        private Product product;
        private Integer quantity;
        private BigDecimal unitPrice;
        private PurchaseOrder purchaseOrder;

        public PurchaseOrderLineBuilder id(Long id) { this.id = id; return this; }
        public PurchaseOrderLineBuilder product(Product product) { this.product = product; return this; }
        public PurchaseOrderLineBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public PurchaseOrderLineBuilder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public PurchaseOrderLineBuilder purchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; return this; }

        public PurchaseOrderLine build() {
            return new PurchaseOrderLine(id, product, quantity, unitPrice, purchaseOrder);
        }
    }
}
