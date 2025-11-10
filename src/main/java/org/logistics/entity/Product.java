package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    private String category;

    private BigDecimal originalPrice;
    private BigDecimal finalPrice;

    private boolean active = true;

    public static ProductBuilder builder() { return new ProductBuilder(); }

    public static class ProductBuilder {
        private Long id;
        private String sku;
        private String name;
        private String category;
        private BigDecimal originalPrice;
        private BigDecimal finalPrice;
        private boolean active = true;

        public ProductBuilder id(Long id) { this.id = id; return this; }
        public ProductBuilder sku(String sku) { this.sku = sku; return this; }
        public ProductBuilder name(String name) { this.name = name; return this; }
        public ProductBuilder category(String category) { this.category = category; return this; }
        public ProductBuilder originalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; return this; }
        public ProductBuilder finalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; return this; }
        public ProductBuilder active(boolean active) { this.active = active; return this; }

        public Product build() {
            return new Product(id, sku, name, category, originalPrice, finalPrice, active);
        }
    }
}
