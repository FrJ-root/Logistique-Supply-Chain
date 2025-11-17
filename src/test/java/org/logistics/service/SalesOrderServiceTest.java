package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.logistics.enums.OrderStatus;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import java.time.LocalDateTime;
import org.logistics.entity.*;
import java.util.*;

public class SalesOrderServiceTest {

    ClientRepository clientRepo = mock(ClientRepository.class);
    ProductRepository productRepo = mock(ProductRepository.class);
    ShipmentRepository shipmentRepo = mock(ShipmentRepository.class);
    SalesOrderRepository orderRepo = mock(SalesOrderRepository.class);
    InventoryRepository inventoryRepo = mock(InventoryRepository.class);
    InventoryMovementRepository movementRepo = mock(InventoryMovementRepository.class);

    SalesOrderService service;

    @Test
    void shouldPreventReservationWhenStockIsNegative() {
        Product p = Product.builder().id(1L).active(true).build();
        Client c = Client.builder().id(1L).build();

        SalesOrder order = SalesOrder.builder()
                .id(10L)
                .status(OrderStatus.CREATED)
                .client(c)
                .lines(List.of(SalesOrderLine.builder().product(p).quantity(10).build()))
                .build();

        when(orderRepo.findById(10L)).thenReturn(Optional.of(order));
        when(inventoryRepo.findByProduct(p)).thenReturn(
                List.of(Inventory.builder().qtyOnHand(5).qtyReserved(5).build())
        );

        assertThrows(RuntimeException.class,
                () -> service.reserveOrder(10L, false)
        );
    }

    @Test
    void shouldTransitionOrderToCanceledFromCreated() {
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
    void shouldCreateBackorderWhenPartialAllowed() {
        Product p = Product.builder().id(1L).active(true).build();
        SalesOrderLine line = SalesOrderLine.builder().product(p).quantity(10).build();
        SalesOrder order = SalesOrder.builder()
                .id(100L)
                .status(OrderStatus.CREATED)
                .lines(List.of(line))
                .build();

        when(orderRepo.findById(100L)).thenReturn(Optional.of(order));
        when(inventoryRepo.findByProduct(p)).thenReturn(
                List.of(Inventory.builder().qtyOnHand(5).qtyReserved(0).build())
        );

        SalesOrder reserved = service.reserveOrderWarehouse(100L, true);

        assertEquals(5, line.getBackorderedQty());
    }

    @Test
    void shouldReleaseExpiredReservations() {
        Product p = Product.builder().id(1L).active(true).build();

        SalesOrderLine line = SalesOrderLine.builder()
                .product(p).quantity(5).build();

        SalesOrder oldOrder = SalesOrder.builder()
                .status(OrderStatus.RESERVED)
                .reservedAt(LocalDateTime.now().minusHours(30))
                .lines(List.of(line))
                .build();

        when(orderRepo.findAll()).thenReturn(List.of(oldOrder));
        when(inventoryRepo.findByProduct(p)).thenReturn(
                List.of(Inventory.builder().qtyReserved(5).qtyOnHand(10).build())
        );

        service.releaseExpiredReservations();

        assertEquals(OrderStatus.CREATED, oldOrder.getStatus());
        assertNull(oldOrder.getReservedAt());
    }

    @Test
    void shouldReserveStockCorrectly() {
        Product p = Product.builder().id(1L).active(true).build();
        Client c = Client.builder().id(1L).build();

        SalesOrder order = SalesOrder.builder()
                .id(10L)
                .status(OrderStatus.CREATED)
                .client(c)
                .lines(List.of(SalesOrderLine.builder().product(p).quantity(5).build()))
                .build();

        when(orderRepo.findById(10L)).thenReturn(Optional.of(order));
        when(inventoryRepo.findByProduct(p)).thenReturn(
                List.of(Inventory.builder().qtyOnHand(10).qtyReserved(0).build())
        );
        when(orderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        SalesOrder result = service.reserveOrder(10L, false);

        assertEquals(OrderStatus.RESERVED, result.getStatus());
    }

    @BeforeEach
    void setup() {
        service = new SalesOrderService(
                clientRepo,
                productRepo,
                shipmentRepo,
                inventoryRepo,
                orderRepo,
                movementRepo
        );
    }

}