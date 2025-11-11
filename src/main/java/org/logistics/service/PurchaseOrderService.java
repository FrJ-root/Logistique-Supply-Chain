package org.logistics.service;

import org.springframework.transaction.annotation.Transactional;
import org.logistics.mapper.PurchaseOrderMapper;
import org.logistics.enums.PurchaseOrderStatus;
import org.springframework.stereotype.Service;
import org.logistics.enums.MovementType;
import lombok.RequiredArgsConstructor;
import org.logistics.repository.*;
import java.time.LocalDateTime;
import org.logistics.entity.*;
import org.logistics.dto.*;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderMapper mapper;
    private final ProductRepository productRepo;
    private final PurchaseOrderRepository poRepo;
    private final SupplierRepository supplierRepo;
    private final WarehouseRepository warehouseRepo;
    private final InventoryRepository inventoryRepo;
    private final PurchaseOrderLineRepository lineRepo;
    private final InventoryMovementRepository movementRepo;

    @Transactional
    public PurchaseOrderDTO create(PurchaseOrderDTO dto) {
        Supplier supplier = supplierRepo.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setStatus(PurchaseOrderStatus.CREATED);
        po.setCreatedAt(LocalDateTime.now());
        po.setLines(new ArrayList<>());

        poRepo.save(po);

        for (PurchaseOrderLineDTO lineDTO : dto.getLines()) {
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setProduct(productRepo.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found")));
            line.setQuantity(lineDTO.getQuantity());
            line.setUnitPrice(lineDTO.getUnitPrice());
            line.setReceivedQty(0);
            line.setPurchaseOrder(po);

            lineRepo.save(line);
            po.getLines().add(line);
        }

        return mapper.toDTO(po);
    }

    @Transactional
    public PurchaseOrderDTO approve(Long id) {
        PurchaseOrder po = poRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("PO not found"));

        if (po.getStatus() == PurchaseOrderStatus.APPROVED) {
            return mapper.toDTO(po);
        }

        if (po.getStatus() != PurchaseOrderStatus.CREATED) {
            throw new RuntimeException("Only CREATED POs can be approved (current status: " + po.getStatus() + ")");
        }

        po.setStatus(PurchaseOrderStatus.APPROVED);
        PurchaseOrder saved = poRepo.save(po);

        return mapper.toDTO(saved);
    }

    @Transactional
    public PurchaseOrderDTO receive(PurchaseReceptionBatchDTO batch) {

        PurchaseOrder po = poRepo.findById(batch.getPurchaseOrderId())
                .orElseThrow(() -> new RuntimeException("PO not found"));

        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new RuntimeException("PO must be APPROVED to receive");
        }

        Warehouse warehouse = warehouseRepo.findById(batch.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        for (PurchaseReceptionItemDTO item : batch.getItems()) {

            if (item.getQuantityReceived() == null || item.getQuantityReceived() <= 0) {
                throw new IllegalArgumentException("Quantity received must be > 0 for line " + item.getLineId());
            }

            PurchaseOrderLine line = lineRepo.findById(item.getLineId())
                    .orElseThrow(() -> new RuntimeException("Line not found: " + item.getLineId()));

            Integer currentReceived = line.getReceivedQty();
            if (currentReceived == null) currentReceived = 0;

            int remaining = line.getQuantity() - currentReceived;
            if (item.getQuantityReceived() > remaining) {
                throw new RuntimeException("Excessive receiving for line " + item.getLineId() + " (remaining: " + remaining + ")");
            }

            int newReceived = currentReceived + item.getQuantityReceived();
            line.setReceivedQty(newReceived);
            lineRepo.save(line);

            InventoryMovement movement = new InventoryMovement();
            movement.setType(MovementType.INBOUND);
            movement.setQuantity(item.getQuantityReceived());
            movement.setOccurredAt(LocalDateTime.now());
            movement.setWarehouse(warehouse);
            movement.setProduct(line.getProduct());
            movement.setDescription("PO Reception #" + po.getId());
            movement.setReferenceDocument("PO-" + po.getId());

            movementRepo.save(movement);

            Inventory inv = inventoryRepo.findByWarehouseIdAndProductId(
                    warehouse.getId(),
                    line.getProduct().getId()
            ).orElseGet(() -> {
                Inventory newInv = new Inventory();
                newInv.setProduct(line.getProduct());
                newInv.setWarehouse(warehouse);
                newInv.setQtyOnHand(0);
                newInv.setQtyReserved(0);
                return newInv;
            });

            inv.setQtyOnHand(inv.getQtyOnHand() + item.getQuantityReceived());
            inventoryRepo.save(inv);
        }

        boolean completed = po.getLines().stream()
                .allMatch(l -> {
                    Integer r = l.getReceivedQty();
                    return r != null && r.equals(l.getQuantity());
                });

        if (completed) {
            po.setStatus(PurchaseOrderStatus.RECEIVED);
        }

        po = poRepo.save(po);
        return mapper.toDTO(po);
    }

}