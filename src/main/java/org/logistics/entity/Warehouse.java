package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouses")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    private boolean active = true;

    public static WarehouseBuilder builder() { return new WarehouseBuilder(); }

    public static class WarehouseBuilder {
        private Long id;
        private String code;
        private String name;
        private boolean active = true;

        public WarehouseBuilder id(Long id) { this.id = id; return this; }
        public WarehouseBuilder code(String code) { this.code = code; return this; }
        public WarehouseBuilder name(String name) { this.name = name; return this; }
        public WarehouseBuilder active(boolean active) { this.active = active; return this; }

        public Warehouse build() {
            return new Warehouse(id, code, name, active);
        }
    }
}