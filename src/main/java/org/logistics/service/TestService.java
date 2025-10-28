package org.logistics.service;

import org.logistics.entity.TestEntity;
import org.logistics.repository.TestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    private final TestRepository repository;

    public TestService(TestRepository repository) {
        this.repository = repository;
    }

    public TestEntity save(String message) {
        return repository.save(new TestEntity(null, message));
    }

    public List<TestEntity> findAll() {
        return repository.findAll();
    }
}