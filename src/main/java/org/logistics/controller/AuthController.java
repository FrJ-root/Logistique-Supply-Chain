package org.logistics.controller;

import jakarta.servlet.http.HttpSession;
import org.logistics.dto.LoginRequest;
import org.logistics.dto.RegisterRequest;
import org.logistics.dto.UserDTO;
import org.logistics.entity.User;
import org.logistics.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        var user = userService.login(request.getEmail(), request.getPassword());
        if (user.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        session.setAttribute("userId", user.get().getId());
        session.setAttribute("role", user.get().getRole());

        UserDTO dto = UserDTO.builder()
                .id(user.get().getId())
                .email(user.get().getEmail())
                .role(user.get().getRole())
                .active(user.get().isActive())
                .build();

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Successfully logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpSession session) {
        Object id = session.getAttribute("userId");
        if (id == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        var user = userService.findById((Long) id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerClient(
                    request.getEmail(),
                    request.getPassword(),
                    request.getName(),
                    request.getContactInfo()
            );

            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "active", user.isActive()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

}
