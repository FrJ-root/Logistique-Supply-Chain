package org.logistics.controller;

import org.logistics.entity.TestEntity;
import org.logistics.service.TestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService service;

    public TestController(TestService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public List<TestEntity> getAll() {
        return service.findAll();
    }

    @PostMapping("/save")
    public TestEntity save(@RequestParam String message) {
        return service.save(message);
    }
}