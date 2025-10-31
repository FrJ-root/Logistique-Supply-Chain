package org.logistics.service;

import org.logistics.dto.UserDTO;
import org.logistics.entity.User;
import org.logistics.enums.Role;
import org.logistics.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPasswordHash().equals(password) && user.isActive());
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> updateUser(Long id, UserDTO dto) {
        return userRepository.findById(id)
                .map(user -> {
                    if (dto.getEmail() != null) user.setEmail(dto.getEmail());
                    if (dto.getPasswordHash() != null) user.setPasswordHash(dto.getPasswordHash());
                    if (dto.getRole() != null) user.setRole(dto.getRole());
                    user.setActive(dto.isActive());
                    return userRepository.save(user);
                });
    }

    public User registerClient(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(password)
                .role(Role.CLIENT)
                .active(true)
                .build();

        return userRepository.save(user);
    }

}