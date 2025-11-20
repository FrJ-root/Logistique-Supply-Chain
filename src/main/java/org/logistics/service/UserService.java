package org.logistics.service;

import org.logistics.repository.ClientRepository;
import org.logistics.repository.UserRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.logistics.entity.Client;
import org.logistics.dto.UserDTO;
import org.logistics.entity.User;
import org.logistics.enums.Role;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public UserService(UserRepository userRepository, ClientRepository clientRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPasswordHash().equals(password) && user.isActive());
    }

    public List<User> findById(Long id) {
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

    @Transactional
    public User registerClient(String email, String password, String name, String contactInfo) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(password)
                .role(Role.CLIENT)
                .active(true)
                .build();
        user = userRepository.save(user);

        Client client = Client.builder()
                .name(name)
                .contactInfo(contactInfo)
                .build();
        client.setUser(user);
        clientRepository.save(client);

        return user;
    }

}