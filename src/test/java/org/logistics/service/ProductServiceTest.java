package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import org.logistics.entity.*;
import org.logistics.dto.*;

import java.math.BigDecimal;
import java.util.*;

public class ProductServiceTest {

    ProductRepository productRepo;
    SalesOrderLineRepository orderLineRepo;
    ProductService service;

    @BeforeEach
    void setup() {
        productRepo = mock(ProductRepository.class);
        orderLineRepo = mock(SalesOrderLineRepository.class);
        service = new ProductService(productRepo, orderLineRepo);
    }

    @Test
    void shouldCreateProduct() {
        ProductDTO dto = ProductDTO.builder()
                .name("Test Product")
                .category("Electronics")
                .originalPrice(BigDecimal.valueOf(100.0))
                .finalPrice(BigDecimal.valueOf(90.0))
                .build();

        when(productRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Product p = service.create(dto);

        assertNotNull(p.getSku());
        assertEquals("Test Product", p.getName());
        assertTrue(p.isActive());
    }

    @Test
    void shouldReturnAllProducts() {
        Product p1 = new Product();
        Product p2 = new Product();
        when(productRepo.findAll()).thenReturn(List.of(p1, p2));

        List<Product> products = service.findAll();
        assertEquals(2, products.size());
    }

    @Test
    void shouldFindProductById() {
        Product p = new Product();
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        Optional<Product> result = service.findById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void shouldUpdateProduct() {
        Product existing = Product.builder()
                .id(1L)
                .name("Old Name")
                .category("Old Cat")
                .originalPrice(BigDecimal.valueOf(50.0))
                .finalPrice(BigDecimal.valueOf(45.0))
                .build();

        ProductDTO dto = ProductDTO.builder()
                .name("New Name")
                .category("New Cat")
                .originalPrice(BigDecimal.valueOf(60.0))
                .finalPrice(BigDecimal.valueOf(55.0))
                .build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Optional<Product> updated = service.update(1L, dto);
        assertTrue(updated.isPresent());
        assertEquals("New Name", updated.get().getName());
        assertEquals("New Cat", updated.get().getCategory());
        assertEquals(BigDecimal.valueOf(60.0), updated.get().getOriginalPrice());
        assertEquals(BigDecimal.valueOf(55.0), updated.get().getFinalPrice());

    }

    @Test
    void shouldDeactivateProduct() {
        Product p = Product.builder().id(1L).active(true).build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(orderLineRepo.existsByProductId(1L)).thenReturn(true); // has orders
        when(productRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Optional<Product> result = service.deactivate(1L);
        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    void shouldActivateProduct() {
        Product p = Product.builder().id(1L).active(false).build();

        when(productRepo.findById(1L)).thenReturn(Optional.of(p));
        when(productRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Optional<Product> result = service.activate(1L);
        assertTrue(result.isPresent());
        assertTrue(result.get().isActive());
    }

    @Test
    void shouldThrowWhenActivatingAlreadyActive() {
        Product p = Product.builder().id(1L).active(true).build();
        when(productRepo.findById(1L)).thenReturn(Optional.of(p));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.activate(1L));
        assertTrue(ex.getMessage().contains("already active"));
    }

    @Test
    void shouldFindBySku() {
        Product p = Product.builder().id(1L).sku("SKU-123").name("Product").active(true).build();
        when(productRepo.findBySku("SKU-123")).thenReturn(Optional.of(p));

        Optional<ProductDTO> result = service.findBySku("SKU-123");
        assertTrue(result.isPresent());
        assertEquals("Product", result.get().getName());
    }

}