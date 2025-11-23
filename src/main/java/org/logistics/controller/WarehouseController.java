package org.logistics.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.logistics.dto.WarehouseDTO;
import org.logistics.service.WarehouseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @GetMapping("/dashboard")
    public String warehouseDashboard() {
        return "Welcome WAREHOUSE MANAGER!";
    }

    @PostMapping
    public WarehouseDTO create(@RequestBody WarehouseDTO dto) {
        return service.create(dto);
    }

    @GetMapping
    public List<WarehouseDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public WarehouseDTO get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public WarehouseDTO update(@PathVariable Long id, @RequestBody WarehouseDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/deactivate")
    public WarehouseDTO deactivate(@PathVariable Long id) {
        return service.deactivate(id);
    }

}