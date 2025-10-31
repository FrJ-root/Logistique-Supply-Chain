package org.logistics.mapper;

import org.logistics.dto.CarrierDTO;
import org.logistics.entity.Carrier;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CarrierMapper {
    CarrierMapper INSTANCE = Mappers.getMapper(CarrierMapper.class);

    CarrierDTO toDTO(Carrier carrier);
    Carrier toEntity(CarrierDTO dto);
}
