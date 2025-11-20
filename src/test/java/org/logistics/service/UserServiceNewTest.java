package org.logistics.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.logistics.entity.User;
import org.logistics.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class UserServiceNewTest {
    @Mock
    private UserRepository repo;
    @InjectMocks
    private UserService ser;

    @Test
    void findById(){
        //Arrange
        User user=new User();
        user.setId(1L);
        User.builder().build().setEmail("test@test.com");
        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(user));

        //Act
        List<User> result= ser.findById(1L);

        //Assert
        assertNotNull(result);
        assertEquals("test@test.com", result.toString());
        Mockito.verify(repo).findById(1L);
    }
}