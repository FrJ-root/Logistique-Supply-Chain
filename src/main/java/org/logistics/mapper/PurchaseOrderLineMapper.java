package org.logistics.mapper;

import org.logistics.dto.PurchaseOrderLineDTO;
import org.logistics.entity.PurchaseOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PurchaseOrderLineMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "purchaseOrder.id", target = "purchaseOrderId")
    PurchaseOrderLineDTO toDTO(PurchaseOrderLine line);

    @Mapping(source = "productId", target = "product.id")
    @Mapping(source = "purchaseOrderId", target = "purchaseOrder.id")
    PurchaseOrderLine toEntity(PurchaseOrderLineDTO dto);
}
