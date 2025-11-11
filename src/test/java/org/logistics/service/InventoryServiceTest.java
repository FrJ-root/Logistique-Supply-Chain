package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.logistics.dto.InventoryMovementDTO;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import org.logistics.entity.*;
import java.util.Optional;

public class InventoryServiceTest {

    InventoryMovementRepository movementRepo = mock(InventoryMovementRepository.class);
    InventoryRepository invRepo = mock(InventoryRepository.class);
    ProductRepository productRepo = mock(ProductRepository.class);
    WarehouseRepository whRepo = mock(WarehouseRepository.class);

    InventoryService service = new InventoryService(movementRepo, invRepo, whRepo, productRepo);

    @Test
    void shouldThrowWhenOutboundGreaterThanStock() {
        InventoryMovementDTO dto = new InventoryMovementDTO();
        dto.setQuantity(10);

        Product p = Product.builder().id(1L).build();
        Warehouse w = Warehouse.builder().id(1L).build();
        Inventory inv = Inventory.builder().qtyOnHand(5).qtyReserved(0).build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(whRepo.findById(1L)).thenReturn(Optional.of(w));
        when(invRepo.findByWarehouseIdAndProductId(1L,1L)).thenReturn(Optional.of(inv));

        assertThrows(RuntimeException.class, () -> service.recordOutbound(dto));
    }

}