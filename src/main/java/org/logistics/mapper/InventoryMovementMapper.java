package org.logistics.mapper;

import org.logistics.dto.InventoryMovementDTO;
import org.logistics.entity.InventoryMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMovementMapper {

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "product.id", target = "productId")
    InventoryMovementDTO toDTO(InventoryMovement movement);

    @Mapping(source = "warehouseId", target = "warehouse.id")
    @Mapping(source = "productId", target = "product.id")
    InventoryMovement toEntity(InventoryMovementDTO dto);
}
