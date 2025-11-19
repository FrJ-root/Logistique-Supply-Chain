package org.logistics.service;


import org.logistics.entity.Carrier;
import org.logistics.entity.SalesOrder;
import org.logistics.entity.Shipment;
import org.logistics.enums.OrderStatus;
import org.logistics.repository.SupplierRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.logistics.dto.SupplierDTO;
import org.logistics.entity.Supplier;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.List;

public class SupplierServiceTest {

    SupplierRepository repository;
    SupplierService service;

    @BeforeEach
    void setup() {
        repository = mock(SupplierRepository.class);
        service = new SupplierService(repository);
    }

    @Test
    void shouldCreateSupplier() {
        SupplierDTO dto = new SupplierDTO();
        dto.setName("Supplier1");
        dto.setContactInfo("contact@supplier.com");

        Supplier saved = Supplier.builder()
                .id(1L)
                .name(dto.getName())
                .contact(dto.getContactInfo())
                .build();

        when(repository.save(any(Supplier.class))).thenReturn(saved);

        Supplier result = service.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Supplier1", result.getName());
        assertEquals("contact@supplier.com", result.getContact());

        verify(repository, times(1)).save(any(Supplier.class));
    }

    @Test
    void shouldReturnAllSuppliers() {
        List<Supplier> suppliers = List.of(
                Supplier.builder().id(1L).name("S1").build(),
                Supplier.builder().id(2L).name("S2").build()
        );

        when(repository.findAll()).thenReturn(suppliers);

        List<Supplier> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("S1", result.get(0).getName());
        assertEquals("S2", result.get(1).getName());

        verify(repository, times(1)).findAll();
    }

    @Test
    void shouldReturnSupplierById() {
        Supplier s = Supplier.builder().id(1L).name("S1").build();
        when(repository.findById(1L)).thenReturn(Optional.of(s));

        Supplier result = service.findById(1L);

        assertNotNull(result);
        assertEquals("S1", result.getName());

        verify(repository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowWhenSupplierNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.findById(99L));

        assertEquals("Supplier not found", ex.getMessage());

        verify(repository, times(1)).findById(99L);
    }

}