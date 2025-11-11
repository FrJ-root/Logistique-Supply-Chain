package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.logistics.enums.ShipmentStatus;
import org.logistics.enums.OrderStatus;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import org.logistics.entity.*;
import java.util.Optional;

public class ShipmentServiceTest {

    SalesOrderRepository orderRepo = mock(SalesOrderRepository.class);
    ShipmentRepository shipmentRepo = mock(ShipmentRepository.class);
    CarrierRepository carrierRepo = mock(CarrierRepository.class);

    ShipmentService service = new ShipmentService(orderRepo, shipmentRepo, carrierRepo);

    @Test
    void shouldNotCreateShipmentWhenOrderNotReserved() {
        SalesOrder o = SalesOrder.builder().status(OrderStatus.CREATED).build();
        when(orderRepo.findById(1L)).thenReturn(Optional.of(o));

        assertThrows(RuntimeException.class,
                () -> service.createShipment(1L, 1L)
        );
    }

    @Test
    void shouldUpdateShipmentStatus() {
        Shipment s = Shipment.builder().id(1L).status(ShipmentStatus.PLANNED).build();
        when(shipmentRepo.findById(1L)).thenReturn(Optional.of(s));
        when(shipmentRepo.save(s)).thenReturn(s);

        Shipment updated = service.updateStatus(1L, ShipmentStatus.IN_TRANSIT);

        assertEquals(ShipmentStatus.IN_TRANSIT, updated.getStatus());
        assertNotNull(updated.getShippedDate());
    }

}