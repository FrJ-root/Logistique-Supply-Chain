package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.CarrierStatus;

@Entity
@Table(name = "carriers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Carrier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String name;
    private String contactEmail;
    private String phone;

    private Integer maxDailyShipments;
    private Integer maxCapacity;

    private Integer cutoffTime;

    private Double baseShippingRate;

    @Enumerated(EnumType.STRING)
    private CarrierStatus status;
}
