package org.logistics.service;

import lombok.RequiredArgsConstructor;
import org.logistics.dto.WarehouseDTO;
import org.logistics.entity.InventoryMovement;
import org.logistics.entity.Warehouse;
import org.logistics.mapper.WarehouseMapper;
import org.logistics.repository.InventoryMovementRepository;
import org.logistics.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryMovementRepository movementRepository;
    private final WarehouseMapper mapper;

    private String generateCode() {
        long counter = warehouseRepository.count() + 1;
        return String.format("WH-%05d", counter);
    }

    public WarehouseDTO create(WarehouseDTO dto) {
        Warehouse warehouse = mapper.toEntity(dto);
        warehouse.setCode(generateCode());
        warehouse.setActive(true);
        return mapper.toDTO(warehouseRepository.save(warehouse));
    }

    public List<WarehouseDTO> getAll() {
        return warehouseRepository.findAll().stream().map(mapper::toDTO).toList();
    }

    public WarehouseDTO get(Long id) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        return mapper.toDTO(w);
    }

    public WarehouseDTO update(Long id, WarehouseDTO dto) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        w.setName(dto.getName());
        w.setActive(dto.isActive());

        return mapper.toDTO(warehouseRepository.save(w));
    }

    @Transactional
    public void delete(Long id) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        boolean hasMovements = movementRepository.existsByWarehouseId(id);

        if (hasMovements) {
            throw new RuntimeException(
                    "Warehouse contains movements! Deactivation required instead of deletion"
            );
        }

        warehouseRepository.delete(w);
    }

    public WarehouseDTO deactivate(Long id) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        w.setActive(false);
        return mapper.toDTO(warehouseRepository.save(w));
    }
}
