package org.logistics.mapper;

import org.logistics.dto.WarehouseDTO;
import org.logistics.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseMapper INSTANCE = Mappers.getMapper(WarehouseMapper.class);

    WarehouseDTO toDTO(Warehouse warehouse);
    Warehouse toEntity(WarehouseDTO dto);
}
