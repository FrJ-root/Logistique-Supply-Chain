package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.ShipmentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    private LocalDateTime plannedDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;

    @ManyToOne
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @ManyToOne
    @JoinColumn(name = "carrier_id")
    private Carrier carrier;

    public static ShipmentBuilder builder() { return new ShipmentBuilder(); }

    public static class ShipmentBuilder {
        private Long id;
        private String trackingNumber;
        private ShipmentStatus status;
        private LocalDateTime plannedDate;
        private LocalDateTime shippedDate;
        private LocalDateTime deliveredDate;
        private SalesOrder salesOrder;
        private Carrier carrier;

        public ShipmentBuilder salesOrder(SalesOrder salesOrder) {
            this.salesOrder = salesOrder;
            return this;
        }
        public ShipmentBuilder id(Long id) { this.id = id; return this; }
        public ShipmentBuilder trackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return this; }
        public ShipmentBuilder status(ShipmentStatus status) { this.status = status; return this; }
        public ShipmentBuilder plannedDate(LocalDateTime plannedDate) { this.plannedDate = plannedDate; return this; }
        public ShipmentBuilder shippedDate(LocalDateTime shippedDate) { this.shippedDate = shippedDate; return this; }
        public ShipmentBuilder deliveredDate(LocalDateTime deliveredDate) { this.deliveredDate = deliveredDate; return this; }
        public ShipmentBuilder carrier(Carrier carrier) { this.carrier = carrier; return this; }

        public Shipment build() {
            return new Shipment(id, trackingNumber, status, plannedDate, shippedDate, deliveredDate, salesOrder, carrier);
        }
    }
}
