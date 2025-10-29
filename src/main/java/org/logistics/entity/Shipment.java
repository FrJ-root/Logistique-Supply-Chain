package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.ShipmentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    private LocalDateTime plannedDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;

    @ManyToOne(optional = false)
    private Carrier carrier;
}
