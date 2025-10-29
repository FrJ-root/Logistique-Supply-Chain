package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id","product_id"})
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(nullable = false)
    private Warehouse warehouse;

    @ManyToOne(optional = false) @JoinColumn(nullable = false)
    private Product product;

    private Integer qtyOnHand = 0;

    private Integer qtyReserved = 0;
}
