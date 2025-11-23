package org.logistics.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @GetMapping("/dashboard")
    public String clientDashboard() {
        return "Welcome CLIENT!";
    }
}