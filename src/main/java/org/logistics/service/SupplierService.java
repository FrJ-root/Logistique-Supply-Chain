package org.logistics.service;

import lombok.RequiredArgsConstructor;
import org.logistics.dto.SupplierDTO;
import org.logistics.entity.Supplier;
import org.logistics.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepo;

    public Supplier create(SupplierDTO dto) {
        Supplier supplier = Supplier.builder()
                .name(dto.getName())
                .contact(dto.getContactInfo())
                .build();
        return supplierRepo.save(supplier);
    }

    public List<Supplier> findAll() {
        return supplierRepo.findAll();
    }

    public Supplier findById(Long id) {
        return supplierRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
    }

}