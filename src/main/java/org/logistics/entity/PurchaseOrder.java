package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.PurchaseOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime expectedDelivery;

    @OneToMany(mappedBy = "purchaseOrder")
    private List<PurchaseOrderLine> lines;

    public static PurchaseOrderBuilder builder() { return new PurchaseOrderBuilder(); }

    public static class PurchaseOrderBuilder {
        private Long id;
        private Supplier supplier;
        private PurchaseOrderStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime expectedDelivery;
        private List<PurchaseOrderLine> lines;

        public PurchaseOrderBuilder id(Long id) { this.id = id; return this; }
        public PurchaseOrderBuilder supplier(Supplier supplier) { this.supplier = supplier; return this; }
        public PurchaseOrderBuilder status(PurchaseOrderStatus status) { this.status = status; return this; }
        public PurchaseOrderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PurchaseOrderBuilder expectedDelivery(LocalDateTime expectedDelivery) { this.expectedDelivery = expectedDelivery; return this; }
        public PurchaseOrderBuilder lines(List<PurchaseOrderLine> lines) { this.lines = lines; return this; }

        public PurchaseOrder build() {
            return new PurchaseOrder(id, supplier, status, createdAt, expectedDelivery, lines);
        }
    }
}
