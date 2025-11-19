package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.logistics.mapper.WarehouseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.logistics.dto.WarehouseDTO;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import org.logistics.entity.*;
import java.util.*;

public class WarehouseServiceTest {

    InventoryMovementRepository movementRepo;
    WarehouseRepository warehouseRepo;
    WarehouseService service;
    WarehouseMapper mapper;

    @BeforeEach
    void setup() {
        warehouseRepo = mock(WarehouseRepository.class);
        movementRepo = mock(InventoryMovementRepository.class);
        mapper = mock(WarehouseMapper.class);
        service = new WarehouseService(warehouseRepo, movementRepo, mapper);
    }

    @Test
    void shouldCreateWarehouse() {
        WarehouseDTO dto = WarehouseDTO.builder().name("WH1").build();
        Warehouse entity = new Warehouse();

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(warehouseRepo.count()).thenReturn(0L);
        when(warehouseRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toDTO(any())).thenAnswer(i -> {
            Warehouse w = i.getArgument(0);
            return WarehouseDTO.builder()
                    .name(dto.getName())
                    .code(w.getCode())
                    .active(w.isActive())
                    .build();
        });

        WarehouseDTO result = service.create(dto);

        assertEquals("WH-00001", result.getCode());
        assertTrue(result.isActive());
    }

    @Test
    void shouldGetWarehouseById() {
        Warehouse w = new Warehouse();
        WarehouseDTO dto = WarehouseDTO.builder().name("WH1").build();
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(w));
        when(mapper.toDTO(w)).thenReturn(dto);

        WarehouseDTO result = service.get(1L);
        assertEquals("WH1", result.getName());
    }

    @Test
    void shouldGetAllWarehouses() {
        Warehouse w1 = new Warehouse();
        Warehouse w2 = new Warehouse();
        WarehouseDTO dto1 = WarehouseDTO.builder().name("WH1").build();
        WarehouseDTO dto2 = WarehouseDTO.builder().name("WH2").build();

        when(warehouseRepo.findAll()).thenReturn(List.of(w1, w2));
        when(mapper.toDTO(w1)).thenReturn(dto1);
        when(mapper.toDTO(w2)).thenReturn(dto2);

        List<WarehouseDTO> all = service.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldUpdateWarehouse() {
        Warehouse w = new Warehouse();
        WarehouseDTO dto = WarehouseDTO.builder().name("NewName").active(false).build();
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(w));
        when(warehouseRepo.save(w)).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toDTO(w)).thenReturn(dto);

        WarehouseDTO result = service.update(1L, dto);
        assertEquals("NewName", result.getName());
        assertFalse(result.isActive());
    }

    @Test
    void shouldDeactivateWarehouse() {
        Warehouse w = new Warehouse();
        WarehouseDTO dto = WarehouseDTO.builder().build();
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(w));
        when(warehouseRepo.save(w)).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toDTO(w)).thenReturn(dto);

        WarehouseDTO result = service.deactivate(1L);
        assertFalse(w.isActive());
    }

    @Test
    void shouldDeleteWarehouseWithoutMovements() {
        Warehouse w = new Warehouse();
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(w));
        when(movementRepo.existsByWarehouseId(1L)).thenReturn(false);

        assertDoesNotThrow(() -> service.delete(1L));
        verify(warehouseRepo, times(1)).delete(w);
    }

    @Test
    void shouldThrowWhenDeletingWarehouseWithMovements() {
        Warehouse w = new Warehouse();
        when(warehouseRepo.findById(1L)).thenReturn(Optional.of(w));
        when(movementRepo.existsByWarehouseId(1L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.delete(1L));
        assertTrue(ex.getMessage().contains("contains movements"));
    }

}