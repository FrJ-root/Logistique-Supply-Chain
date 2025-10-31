package org.logistics.mapper;

import org.logistics.dto.InventoryDTO;
import org.logistics.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "product.id", target = "productId")
    InventoryDTO toDTO(Inventory inventory);

    @Mapping(source = "warehouseId", target = "warehouse.id")
    @Mapping(source = "productId", target = "product.id")
    Inventory toEntity(InventoryDTO dto);
}
