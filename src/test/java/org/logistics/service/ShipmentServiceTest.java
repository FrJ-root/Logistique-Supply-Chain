package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import org.logistics.entity.*;
import org.logistics.enums.*;
import java.util.Optional;
import java.util.List;

public class ShipmentServiceTest {

    ShipmentRepository shipmentRepo;
    SalesOrderRepository orderRepo;
    CarrierRepository carrierRepo;

    ShipmentService service;

    @BeforeEach
    void setup() {
        orderRepo = mock(SalesOrderRepository.class);
        shipmentRepo = mock(ShipmentRepository.class);
        carrierRepo = mock(CarrierRepository.class);

        service = new ShipmentService(orderRepo, shipmentRepo, carrierRepo);
    }

    @Test
    void shouldCreateShipment() {
        SalesOrder order = SalesOrder.builder().id(1L).status(OrderStatus.RESERVED).build();
        Carrier carrier = Carrier.builder().id(1L).cutoffTime(15).build();

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));
        when(carrierRepo.findById(1L)).thenReturn(Optional.of(carrier));
        when(shipmentRepo.findBySalesOrder(order)).thenReturn(Optional.empty());
        when(shipmentRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Shipment shipment = service.createShipment(1L, 1L);

        assertNotNull(shipment);
        assertEquals(order, shipment.getSalesOrder());
        assertEquals(ShipmentStatus.PLANNED, shipment.getStatus());
        assertNotNull(shipment.getPlannedDate());
        assertTrue(shipment.getTrackingNumber().startsWith("TRK-"));
    }

    @Test
    void shouldThrowIfOrderNotReserved() {
        SalesOrder order = SalesOrder.builder().status(OrderStatus.CREATED).build();
        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.createShipment(1L, 1L));
        assertTrue(ex.getMessage().contains("RESERVED"));
    }

    @Test
    void shouldUpdateShipmentStatus() {
        Shipment s = Shipment.builder().id(1L).status(ShipmentStatus.PLANNED).build();

        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(s));
        when(shipmentRepo.save(s)).thenAnswer(i -> i.getArguments()[0]);

        Shipment updated = service.updateStatus(1L, ShipmentStatus.IN_TRANSIT);

        assertEquals(ShipmentStatus.IN_TRANSIT, updated.getStatus());
        assertNotNull(updated.getShippedDate());
    }

    @Test
    void shouldUpdateShipmentStatusToDelivered() {
        Shipment s = Shipment.builder().id(1L).status(ShipmentStatus.IN_TRANSIT).build();

        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(s));
        when(shipmentRepo.save(s)).thenAnswer(i -> i.getArguments()[0]);

        Shipment updated = service.updateStatus(1L, ShipmentStatus.DELIVERED);

        assertEquals(ShipmentStatus.DELIVERED, updated.getStatus());
        assertNotNull(updated.getDeliveredDate());
    }

    @Test
    void shouldGetShipmentForClient() {
        Client client = Client.builder().id(1L).build();
        SalesOrder order = SalesOrder.builder().id(10L).client(client).lines(List.of(SalesOrderLine.builder().build())).build();
        Shipment shipment = Shipment.builder().id(100L).build();

        when(orderRepo.findById(10L)).thenReturn(Optional.of(order));
        when(shipmentRepo.findById(10L)).thenReturn(Optional.of(shipment));

        Shipment result = service.getShipmentForOrder(10L, 1L);
        assertEquals(shipment, result);
    }

    @Test
    void shouldThrowIfClientMismatch() {
        Client client = Client.builder().id(2L).build();
        SalesOrder order = SalesOrder.builder().id(10L).client(client).lines(List.of(SalesOrderLine.builder().build())).build();

        when(orderRepo.findById(10L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getShipmentForOrder(10L, 1L));
        assertTrue(ex.getMessage().contains("Accès interdit"));
    }

    @Test
    void shouldThrowIfShipmentNotFound() {
        Client client = Client.builder().id(1L).build();
        SalesOrder order = SalesOrder.builder().id(10L).client(client).lines(List.of(SalesOrderLine.builder().build())).build();

        when(orderRepo.findById(10L)).thenReturn(Optional.of(order));
        when(shipmentRepo.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.getShipmentForOrder(10L, 1L));
        assertTrue(ex.getMessage().contains("Aucune expédition"));
    }

}