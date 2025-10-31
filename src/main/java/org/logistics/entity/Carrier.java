package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.CarrierStatus;

@Entity
@Table(name = "carriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_email")
    private String contactEmail;

    private String phone;

    @Column(name = "max_daily_shipments")
    private Integer maxDailyShipments;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "cutoff_time")
    private Integer cutoffTime;

    @Column(name = "base_shipping_rate")
    private Double baseShippingRate;

    @Enumerated(EnumType.STRING)
    private CarrierStatus status;

    public static CarrierBuilder builder() {
        return new CarrierBuilder();
    }

    public static class CarrierBuilder {
        private Long id;
        private String code;
        private String name;
        private String contactEmail;
        private String phone;
        private Integer maxDailyShipments;
        private Integer maxCapacity;
        private Integer cutoffTime;
        private Double baseShippingRate;
        private CarrierStatus status;

        public CarrierBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CarrierBuilder code(String code) {
            this.code = code;
            return this;
        }

        public CarrierBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CarrierBuilder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        public CarrierBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public CarrierBuilder maxDailyShipments(Integer maxDailyShipments) {
            this.maxDailyShipments = maxDailyShipments;
            return this;
        }

        public CarrierBuilder maxCapacity(Integer maxCapacity) {
            this.maxCapacity = maxCapacity;
            return this;
        }

        public CarrierBuilder cutoffTime(Integer cutoffTime) {
            this.cutoffTime = cutoffTime;
            return this;
        }

        public CarrierBuilder baseShippingRate(Double baseShippingRate) {
            this.baseShippingRate = baseShippingRate;
            return this;
        }

        public CarrierBuilder status(CarrierStatus status) {
            this.status = status;
            return this;
        }

        public Carrier build() {
            return new Carrier(id, code, name, contactEmail, phone, maxDailyShipments, maxCapacity,
                    cutoffTime, baseShippingRate, status);
        }
    }
}
