package org.logistics.service;

import org.logistics.entity.Warehouse;
import org.logistics.enums.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {

    private final List<Warehouse> warehouses = List.of(
            Warehouse.builder().id(1L).code("WHS-01").name("Main").active(true).build(),
            Warehouse.builder().id(2L).code("WHS-02").name("Secondary").active(true).build()
    );

    public List<Warehouse> getWarehouses(Role role) {
        if (role == Role.ADMIN || role == Role.WAREHOUSE_MANAGER) {
            return warehouses;
        } else {
            throw new RuntimeException("Access denied for role: " + role);
        }
    }
}
