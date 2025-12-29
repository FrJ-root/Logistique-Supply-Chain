package org.logistics.service;

import lombok.RequiredArgsConstructor;
import org.logistics.repository.ClientRepository;
import org.logistics.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.logistics.entity.Client;
import org.logistics.dto.UserDTO;
import org.logistics.entity.User;
import org.logistics.enums.Role;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
                .filter(User::isActive);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> updateUser(Long id, UserDTO dto) {
        return userRepository.findById(id)
                .map(user -> {
                    if (dto.getEmail() != null) user.setEmail(dto.getEmail());

                    if (dto.getPasswordHash() != null) {
                        user.setPasswordHash(passwordEncoder.encode(dto.getPasswordHash()));
                    }

                    if (dto.getRole() != null) user.setRole(dto.getRole());
                    user.setActive(dto.isActive());
                    return userRepository.save(user);
                });
    }

    @Transactional
    public User registerClient(String email, String password, String name, String contactInfo) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.CLIENT)
                .active(true)
                .build();

        user = userRepository.save(user);

        Client client = Client.builder()
                .name(name)
                .contactInfo(contactInfo)
                .user(user)
                .build();

        clientRepository.save(client);

        return user;
    }
}