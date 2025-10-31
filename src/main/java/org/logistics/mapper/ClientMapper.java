package org.logistics.mapper;

import org.logistics.dto.ClientDTO;
import org.logistics.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);

    ClientDTO toDTO(Client client);
    Client toEntity(ClientDTO dto);
}
