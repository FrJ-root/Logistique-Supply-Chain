package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id","product_id"})
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Warehouse warehouse;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Product product;

    private Integer qtyOnHand = 0;
    private Integer qtyReserved = 0;

    // Custom Builder
    public static InventoryBuilder builder() { return new InventoryBuilder(); }

    public static class InventoryBuilder {
        private Long id;
        private Warehouse warehouse;
        private Product product;
        private Integer qtyOnHand = 0;
        private Integer qtyReserved = 0;

        public InventoryBuilder id(Long id) { this.id = id; return this; }
        public InventoryBuilder warehouse(Warehouse warehouse) { this.warehouse = warehouse; return this; }
        public InventoryBuilder product(Product product) { this.product = product; return this; }
        public InventoryBuilder qtyOnHand(Integer qtyOnHand) { this.qtyOnHand = qtyOnHand; return this; }
        public InventoryBuilder qtyReserved(Integer qtyReserved) { this.qtyReserved = qtyReserved; return this; }

        public Inventory build() {
            return new Inventory(id, warehouse, product, qtyOnHand, qtyReserved);
        }
    }
}
