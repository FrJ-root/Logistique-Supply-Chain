package org.logistics.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.logistics.repository.*;
import org.logistics.dto.UserDTO;
import org.logistics.enums.Role;
import org.logistics.entity.*;
import java.util.Optional;

public class UserServiceTest {

    UserRepository userRepo;
    ClientRepository clientRepo;
    UserService service;

    @BeforeEach
    void setup() {
        userRepo = mock(UserRepository.class);
        clientRepo = mock(ClientRepository.class);
        service = new UserService(userRepo, clientRepo);
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = User.builder().email("a@b.com").passwordHash("pass").active(true).build();
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        Optional<User> result = service.login("a@b.com", "pass");
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void shouldFailLoginWithWrongPassword() {
        User user = User.builder().email("a@b.com").passwordHash("pass").active(true).build();
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        Optional<User> result = service.login("a@b.com", "wrong");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFailLoginIfUserInactive() {
        User user = User.builder().email("a@b.com").passwordHash("pass").active(false).build();
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        Optional<User> result = service.login("a@b.com", "pass");
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindUserById() {
        User user = new User();
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = service.findById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder().email("old@a.com").passwordHash("old").role(Role.CLIENT).active(true).build();
        UserDTO dto = UserDTO.builder().email("new@a.com").passwordHash("new").role(Role.ADMIN).active(false).build();

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(userRepo.save(user)).thenAnswer(i -> i.getArguments()[0]);

        Optional<User> result = service.updateUser(1L, dto);

        assertTrue(result.isPresent());
        assertEquals("new@a.com", result.get().getEmail());
        assertEquals("new", result.get().getPasswordHash());
        assertEquals(Role.ADMIN, result.get().getRole());
        assertFalse(result.get().isActive());
    }

    @Test
    void shouldRegisterClientSuccessfully() {
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.empty());
        when(userRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(clientRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        User user = service.registerClient("a@b.com", "pass", "ClientName", "Contact");

        assertEquals("a@b.com", user.getEmail());
        assertEquals("pass", user.getPasswordHash());
        assertEquals(Role.CLIENT, user.getRole());
        assertTrue(user.isActive());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        User existing = new User();
        when(userRepo.findByEmail("a@b.com")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> service.registerClient("a@b.com", "pass", "ClientName", "Contact"));
    }

}