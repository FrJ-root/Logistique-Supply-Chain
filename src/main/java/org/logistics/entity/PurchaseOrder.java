package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.PurchaseOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    private PurchaseOrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime expectedDelivery;

    @OneToMany(mappedBy = "purchaseOrder")
    private List<PurchaseOrderLine> lines;
}
