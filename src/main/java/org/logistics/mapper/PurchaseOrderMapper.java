package org.logistics.mapper;

import org.logistics.dto.PurchaseOrderDTO;
import org.logistics.dto.PurchaseOrderLineDTO;
import org.logistics.entity.PurchaseOrder;
import org.logistics.entity.PurchaseOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {

    PurchaseOrderMapper INSTANCE = Mappers.getMapper(PurchaseOrderMapper.class);

    @Mapping(source = "lines", target = "lines")
    PurchaseOrderDTO toDTO(PurchaseOrder po);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "purchaseOrder.id", target = "purchaseOrderId")
    PurchaseOrderLineDTO toLineDTO(PurchaseOrderLine line);

    List<PurchaseOrderLineDTO> toLineDTOs(List<PurchaseOrderLine> lines);

    @Mapping(source = "lines", target = "lines")
    PurchaseOrder toEntity(PurchaseOrderDTO dto);

    @Mapping(source = "productId", target = "product.id")
    @Mapping(source = "purchaseOrderId", target = "purchaseOrder.id")
    PurchaseOrderLine toLineEntity(PurchaseOrderLineDTO dto);
}
