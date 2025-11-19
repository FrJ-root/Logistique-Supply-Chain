package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.logistics.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.logistics.entity.TestEntity;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class TestServiceTest {

    TestRepository repository;
    TestService service;

    @BeforeEach
    void setup() {
        repository = mock(TestRepository.class);
        service = new TestService(repository);
    }

    @Test
    void shouldSaveMessage() {
        TestEntity saved = new TestEntity(1L, "Hello");
        when(repository.save(any(TestEntity.class))).thenReturn(saved);

        TestEntity result = service.save("Hello");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Hello", result.getMessage());

        verify(repository, times(1)).save(any(TestEntity.class));
    }

    @Test
    void shouldReturnAllEntities() {
        List<TestEntity> list = List.of(
                new TestEntity(1L, "Msg1"),
                new TestEntity(2L, "Msg2")
        );

        when(repository.findAll()).thenReturn(list);

        List<TestEntity> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("Msg1", result.get(0).getMessage());
        assertEquals("Msg2", result.get(1).getMessage());

        verify(repository, times(1)).findAll();
    }

}