package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import java.time.LocalDateTime;
import org.logistics.entity.*;
import org.logistics.enums.*;
import java.math.BigDecimal;
import org.logistics.dto.*;
import java.util.Optional;
import java.util.List;

public class SalesOrderServiceTest {

    ClientRepository clientRepo;
    ProductRepository productRepo;
    SalesOrderRepository orderRepo;
    ShipmentRepository shipmentRepo;
    InventoryRepository inventoryRepo;
    InventoryMovementRepository movementRepo;

    SalesOrderService service;

    @BeforeEach
    void setup() {
        clientRepo = mock(ClientRepository.class);
        productRepo = mock(ProductRepository.class);
        orderRepo = mock(SalesOrderRepository.class);
        shipmentRepo = mock(ShipmentRepository.class);
        inventoryRepo = mock(InventoryRepository.class);
        movementRepo = mock(InventoryMovementRepository.class);

        service = new SalesOrderService(
                clientRepo,
                productRepo,
                shipmentRepo,
                inventoryRepo,
                orderRepo,
                movementRepo
        );
    }

    @Test
    void CreateOrder() {
        Client client = Client.builder().id(1L).build();
        Product product = Product.builder().id(1L).active(true).build();
        SalesOrderLineDTO lineDTO = new SalesOrderLineDTO();
        lineDTO.setProductId(1L);
        lineDTO.setQuantity(5);
        lineDTO.setUnitPrice(BigDecimal.valueOf(100.0));

        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setLines(List.of(lineDTO));

        when(clientRepo.findById(1L)).thenReturn(Optional.of(client));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        SalesOrder order = service.createOrder(dto, 1L);

        assertNotNull(order);
        assertEquals(OrderStatus.CREATED, order.getStatus());
        assertEquals(1, order.getLines().size());
    }

    @Test
    void ReserveOrder() {
        Product p = Product.builder().id(1L).active(true).build();
        SalesOrderLine line = SalesOrderLine.builder().product(p).quantity(5).build();
        SalesOrder order = SalesOrder.builder()
                .id(10L)
                .status(OrderStatus.CREATED)
                .lines(List.of(line))
                .build();

        when(orderRepo.findById(10L)).thenReturn(Optional.of(order));
        when(inventoryRepo.findByProduct(p)).thenReturn(
                List.of(Inventory.builder().qtyOnHand(10).qtyReserved(0).build())
        );
        when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        SalesOrder reserved = service.reserveOrder(10L, false);

        assertEquals(OrderStatus.RESERVED, reserved.getStatus());
        assertEquals(5, inventoryRepo.findByProduct(p).get(0).getQtyOnHand() - inventoryRepo.findByProduct(p).get(0).getQtyReserved());
    }

    @Test
    void shouldShipOrder() {
        Product p = Product.builder().id(1L).build();
        Inventory inv = Inventory.builder().qtyReserved(5).qtyOnHand(5).product(p).build();
        SalesOrderLine line = SalesOrderLine.builder().product(p).quantity(5).build();
        SalesOrder order = SalesOrder.builder()
                .id(60L)
                .status(OrderStatus.RESERVED)
                .lines(List.of(line))
                .build();

        Shipment shipment = Shipment.builder().id(1L).build();

        when(orderRepo.findById(60L)).thenReturn(Optional.of(order));
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(shipment));
        when(inventoryRepo.findByProduct(p)).thenReturn(List.of(inv));
        when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(inventoryRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(movementRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        SalesOrder shipped = service.shipOrder(60L, 1L);

        assertEquals(OrderStatus.SHIPPED, shipped.getStatus());
        assertEquals(0, inv.getQtyReserved());
        assertEquals(0, inv.getQtyOnHand());
    }

    @Test
    void shouldDeliverOrder() {
        SalesOrder order = SalesOrder.builder().id(70L).status(OrderStatus.SHIPPED).build();
        Shipment shipment = Shipment.builder().id(2L).build();

        when(orderRepo.findById(70L)).thenReturn(Optional.of(order));
        when(shipmentRepo.findById(2L)).thenReturn(Optional.of(shipment));
        when(shipmentRepo.save(shipment)).thenAnswer(i -> i.getArguments()[0]);
        when(orderRepo.save(order)).thenAnswer(i -> i.getArguments()[0]);

        SalesOrder delivered = service.deliverOrder(70L, 2L);

        assertEquals(OrderStatus.DELIVERED, delivered.getStatus());
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
    }

    @Test
    void shouldCancelOrderFromCreated() {
        SalesOrder order = SalesOrder.builder()
                .id(50L)
                .status(OrderStatus.CREATED)
                .lines(List.of())
                .build();

        when(orderRepo.findById(50L)).thenReturn(Optional.of(order));
        when(orderRepo.save(order)).thenReturn(order);

        SalesOrder canceled = service.cancelOrder(50L, true);

        assertEquals(OrderStatus.CANCELED, canceled.getStatus());
    }

    @Test
    void shouldCancelOrderFromReserved() {
        Product p = Product.builder().id(1L).build();
        Inventory inv = Inventory.builder().qtyReserved(5).qtyOnHand(10).product(p).build();
        SalesOrderLine line = SalesOrderLine.builder().product(p).quantity(5).build();

        SalesOrder order = SalesOrder.builder()
                .id(51L)
                .status(OrderStatus.RESERVED)
                .lines(List.of(line))
                .build();

        when(orderRepo.findById(51L)).thenReturn(Optional.of(order));
        when(inventoryRepo.findByProduct(p)).thenReturn(List.of(inv));
        when(orderRepo.save(order)).thenReturn(order);
        when(inventoryRepo.save(inv)).thenReturn(inv);

        SalesOrder canceled = service.cancelOrder(51L, true);

        assertEquals(OrderStatus.CANCELED, canceled.getStatus());
        assertEquals(0, inv.getQtyReserved());
    }

    @Test
    void shouldReleaseExpiredReservations() {
        Product p = Product.builder().id(1L).build();
        SalesOrderLine line = SalesOrderLine.builder().product(p).quantity(5).build();

        SalesOrder oldOrder = SalesOrder.builder()
                .status(OrderStatus.RESERVED)
                .reservedAt(LocalDateTime.now().minusHours(30))
                .lines(List.of(line))
                .build();

        Inventory inv = Inventory.builder().product(p).qtyReserved(5).qtyOnHand(10).build();

        when(orderRepo.findAll()).thenReturn(List.of(oldOrder));
        when(inventoryRepo.findByProduct(p)).thenReturn(List.of(inv));
        when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(inventoryRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        service.releaseExpiredReservations();

        assertEquals(OrderStatus.CREATED, oldOrder.getStatus());
        assertNull(oldOrder.getReservedAt());
        assertEquals(0, inv.getQtyReserved());
    }

    @Test
    void shouldThrowWhenStockInsufficient() {
        Product p = Product.builder().id(1L).active(true).build();
        SalesOrderLine line = SalesOrderLine.builder().product(p).quantity(10).build();
        SalesOrder order = SalesOrder.builder()
                .id(11L)
                .status(OrderStatus.CREATED)
                .lines(List.of(line))
                .build();

        when(orderRepo.findById(11L)).thenReturn(Optional.of(order));
        when(inventoryRepo.findByProduct(p)).thenReturn(
                List.of(Inventory.builder().qtyOnHand(5).qtyReserved(0).build())
        );

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.reserveOrder(11L, false));

        assertTrue(ex.getMessage().contains("Stock insuffisant"));
    }

}