package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales_orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public static SalesOrderBuilder builder() { return new SalesOrderBuilder(); }

    public static class SalesOrderBuilder {
        private Long id;
        private Client client;
        private OrderStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime reservedAt;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
        private List<SalesOrderLine> lines;

        public SalesOrderBuilder id(Long id) { this.id = id; return this; }
        public SalesOrderBuilder client(Client client) { this.client = client; return this; }
        public SalesOrderBuilder status(OrderStatus status) { this.status = status; return this; }
        public SalesOrderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public SalesOrderBuilder reservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; return this; }
        public SalesOrderBuilder shippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; return this; }
        public SalesOrderBuilder deliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; return this; }
        public SalesOrderBuilder lines(List<SalesOrderLine> lines) { this.lines = lines; return this; }

        public SalesOrder build() {
            return new SalesOrder(id, client, status, createdAt, reservedAt, shippedAt, deliveredAt, lines);
        }
    }
}
