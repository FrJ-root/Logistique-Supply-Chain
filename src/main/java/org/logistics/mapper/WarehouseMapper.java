package org.logistics.mapper;

import org.logistics.dto.WarehouseDTO;
import org.logistics.entity.Warehouse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseDTO toDTO(Warehouse warehouse);
    Warehouse toEntity(WarehouseDTO dto);
}
