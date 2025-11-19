package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.logistics.dto.InventoryMovementDTO;
import org.junit.jupiter.api.BeforeEach;
import org.logistics.enums.MovementType;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import org.logistics.entity.*;
import java.util.Optional;

public class InventoryServiceTest {

    InventoryMovementRepository movementRepo;
    ProductRepository productRepo;
    InventoryRepository invRepo;
    WarehouseRepository whRepo;

    InventoryService service;

    @BeforeEach
    void setup() {
        movementRepo = mock(InventoryMovementRepository.class);
        productRepo = mock(ProductRepository.class);
        invRepo = mock(InventoryRepository.class);
        whRepo = mock(WarehouseRepository.class);

        service = new InventoryService(movementRepo, invRepo, whRepo, productRepo);
    }

    @Test
    void Inbound() {
        InventoryMovementDTO dto = new InventoryMovementDTO();
        dto.setProductId(1L);
        dto.setWarehouseId(1L);
        dto.setQuantity(10);
        dto.setReferenceDocument("REF1");
        dto.setDescription("Inbound test");

        Product p = Product.builder().id(1L).build();
        Warehouse w = Warehouse.builder().id(1L).build();
        Inventory inv = Inventory.builder().qtyOnHand(5).qtyReserved(0).product(p).warehouse(w).build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(whRepo.findById(1L)).thenReturn(Optional.of(w));
        when(invRepo.findByWarehouseIdAndProductId(1L,1L)).thenReturn(Optional.of(inv));
        when(invRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(movementRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        InventoryMovement result = service.recordInbound(dto);

        assertEquals(MovementType.INBOUND, result.getType());
        assertEquals(15, inv.getQtyOnHand());
    }

    @Test
    void ThrowWhenInboundQtyNonPositive() {
        InventoryMovementDTO dto = new InventoryMovementDTO();
        dto.setQuantity(0);

        assertThrows(RuntimeException.class, () -> service.recordInbound(dto));
    }

    @Test
    void ThrowWhenOutboundGreaterThanStock() {
        InventoryMovementDTO dto = new InventoryMovementDTO();
        dto.setQuantity(10);
        dto.setProductId(1L);
        dto.setWarehouseId(1L);

        Product p = Product.builder().id(1L).build();
        Warehouse w = Warehouse.builder().id(1L).build();
        Inventory inv = Inventory.builder().qtyOnHand(5).qtyReserved(0).product(p).warehouse(w).build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(whRepo.findById(1L)).thenReturn(Optional.of(w));
        when(invRepo.findByWarehouseIdAndProductId(1L,1L)).thenReturn(Optional.of(inv));

        assertThrows(RuntimeException.class, () -> service.recordOutbound(dto));
    }

    @Test
    void ThrowWhenAdjustmentBelowReserved() {
        InventoryMovementDTO dto = new InventoryMovementDTO();
        dto.setQuantity(-5);
        dto.setProductId(1L);
        dto.setWarehouseId(1L);

        Product p = Product.builder().id(1L).build();
        Warehouse w = Warehouse.builder().id(1L).build();
        Inventory inv = Inventory.builder().qtyOnHand(5).qtyReserved(5).product(p).warehouse(w).build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(whRepo.findById(1L)).thenReturn(Optional.of(w));
        when(invRepo.findByWarehouseIdAndProductId(1L,1L)).thenReturn(Optional.of(inv));

        assertThrows(RuntimeException.class, () -> service.recordAdjustment(dto));
    }

    @Test
    void RecordAdjustmentSuccessfully() {
        InventoryMovementDTO dto = new InventoryMovementDTO();
        dto.setQuantity(3);
        dto.setProductId(1L);
        dto.setWarehouseId(1L);

        Product p = Product.builder().id(1L).build();
        Warehouse w = Warehouse.builder().id(1L).build();
        Inventory inv = Inventory.builder().qtyOnHand(5).qtyReserved(2).product(p).warehouse(w).build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(whRepo.findById(1L)).thenReturn(Optional.of(w));
        when(invRepo.findByWarehouseIdAndProductId(1L,1L)).thenReturn(Optional.of(inv));
        when(invRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(movementRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        InventoryMovement result = service.recordAdjustment(dto);

        assertEquals(MovementType.ADJUSTMENT, result.getType());
        assertEquals(8, inv.getQtyOnHand());
    }

    @Test
    void Outbound() {
        InventoryMovementDTO dto = new InventoryMovementDTO();
        dto.setQuantity(5);
        dto.setProductId(1L);
        dto.setWarehouseId(1L);

        Product p = Product.builder().id(1L).build();
        Warehouse w = Warehouse.builder().id(1L).build();
        Inventory inv = Inventory.builder().qtyOnHand(10).qtyReserved(0).product(p).warehouse(w).build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(whRepo.findById(1L)).thenReturn(Optional.of(w));
        when(invRepo.findByWarehouseIdAndProductId(1L,1L)).thenReturn(Optional.of(inv));
        when(invRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(movementRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        InventoryMovement result = service.recordOutbound(dto);

        assertEquals(MovementType.OUTBOUND, result.getType());
        assertEquals(5, inv.getQtyOnHand());
    }

}