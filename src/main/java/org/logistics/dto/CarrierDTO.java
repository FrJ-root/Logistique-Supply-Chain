package org.logistics.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarrierDTO {
    private Long id;
    private String code;
    private String name;
    private String contactEmail;
    private String phone;
    private Integer maxDailyShipments;
    private Integer maxCapacity;
    private Integer cutoffTime;
    private Double baseShippingRate;
    private String status;
}
