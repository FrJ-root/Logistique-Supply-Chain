package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logistics.mapper.PurchaseOrderMapper;
import org.logistics.repository.*;
import org.logistics.entity.*;
import org.logistics.dto.*;
import org.logistics.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class PurchaseOrderServiceTest {

    PurchaseOrderMapper mapper;
    ProductRepository productRepo;
    PurchaseOrderRepository poRepo;
    SupplierRepository supplierRepo;
    WarehouseRepository warehouseRepo;
    InventoryRepository inventoryRepo;
    PurchaseOrderLineRepository lineRepo;
    InventoryMovementRepository movementRepo;

    PurchaseOrderService service;

    @BeforeEach
    void setup() {
        mapper = mock(PurchaseOrderMapper.class);
        productRepo = mock(ProductRepository.class);
        poRepo = mock(PurchaseOrderRepository.class);
        supplierRepo = mock(SupplierRepository.class);
        warehouseRepo = mock(WarehouseRepository.class);
        inventoryRepo = mock(InventoryRepository.class);
        lineRepo = mock(PurchaseOrderLineRepository.class);
        movementRepo = mock(InventoryMovementRepository.class);

        service = new PurchaseOrderService(
                mapper,
                productRepo,
                poRepo,
                supplierRepo,
                warehouseRepo,
                inventoryRepo,
                lineRepo,
                movementRepo
        );
    }

    // ====== Happy path: create PO ======
    @Test
    void shouldCreatePurchaseOrder() {
        Supplier supplier = Supplier.builder().id(1L).build();
        PurchaseOrderLineDTO lineDTO = new PurchaseOrderLineDTO();
        lineDTO.setProductId(10L);
        lineDTO.setQuantity(5);
        lineDTO.setUnitPrice(BigDecimal.valueOf(100.0));

        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setSupplierId(1L);
        dto.setLines(List.of(lineDTO));

        Product product = Product.builder().id(10L).build();
        when(supplierRepo.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(poRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(lineRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toDTO(any())).thenAnswer(i -> new PurchaseOrderDTO());

        PurchaseOrderDTO result = service.create(dto);

        assertNotNull(result);
    }

    // ====== Happy path: approve PO ======
    @Test
    void shouldApprovePurchaseOrder() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);
        po.setStatus(PurchaseOrderStatus.CREATED);

        when(poRepo.findById(1L)).thenReturn(Optional.of(po));
        when(poRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toDTO(any())).thenAnswer(i -> new PurchaseOrderDTO());

        PurchaseOrderDTO result = service.approve(1L);

        assertNotNull(result);
        assertEquals(PurchaseOrderStatus.APPROVED, po.getStatus());
    }

    // ====== Exception: approve PO not in CREATED ======
    @Test
    void shouldThrowWhenApprovingNonCreatedPO() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(2L);
        po.setStatus(PurchaseOrderStatus.RECEIVED);

        when(poRepo.findById(2L)).thenReturn(Optional.of(po));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.approve(2L));

        assertTrue(ex.getMessage().contains("Only CREATED POs can be approved"));
    }

    // ====== Happy path: receive PO ======
    @Test
    void shouldReceivePurchaseOrder() {
        // Prepare PO
        Product product = Product.builder().id(10L).build();
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setId(100L);
        line.setProduct(product);
        line.setQuantity(5);
        line.setReceivedQty(0);

        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);
        po.setStatus(PurchaseOrderStatus.APPROVED);
        po.setLines(new ArrayList<>());
        po.getLines().add(line);

        when(poRepo.findById(1L)).thenReturn(Optional.of(po));

        // Warehouse
        Warehouse warehouse = Warehouse.builder().id(1L).build();
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(warehouse));

        // PurchaseReceptionItem
        PurchaseReceptionItemDTO itemDTO = new PurchaseReceptionItemDTO();
        itemDTO.setLineId(100L);
        itemDTO.setQuantityReceived(5);

        PurchaseReceptionBatchDTO batch = new PurchaseReceptionBatchDTO();
        batch.setPurchaseOrderId(1L);
        batch.setWarehouseId(1L);
        batch.setItems(List.of(itemDTO));

        when(lineRepo.findById(100L)).thenReturn(Optional.of(line));
        when(inventoryRepo.findByWarehouseIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(inventoryRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(movementRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(poRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toDTO(any())).thenAnswer(i -> new PurchaseOrderDTO());

        PurchaseOrderDTO result = service.receive(batch);

        assertNotNull(result);
        assertEquals(PurchaseOrderStatus.RECEIVED, po.getStatus());
        assertEquals(5, line.getReceivedQty());
    }

    // ====== Exception: receive PO not approved ======
    @Test
    void shouldThrowWhenReceivingUnapprovedPO() {
        PurchaseOrder po = new PurchaseOrder();
        po.setId(1L);
        po.setStatus(PurchaseOrderStatus.CREATED);

        when(poRepo.findById(1L)).thenReturn(Optional.of(po));

        PurchaseReceptionBatchDTO batch = new PurchaseReceptionBatchDTO();
        batch.setPurchaseOrderId(1L);
        batch.setWarehouseId(1L);
        batch.setItems(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.receive(batch));

        assertTrue(ex.getMessage().contains("PO must be APPROVED to receive"));
    }
}