package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales_orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SalesOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime reservedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "salesOrder")
    private List<SalesOrderLine> lines;
}
