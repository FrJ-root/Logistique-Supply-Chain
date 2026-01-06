package org.logistics.security;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.logistics.dto.AuthenticationResponse;
import org.logistics.repository.UserRepository;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.logistics.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.logistics.entity.User;
import org.logistics.enums.Role;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User admin = User.builder()
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);

        User client = User.builder()
                .email("client@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.CLIENT)
                .active(true)
                .build();
        userRepository.save(client);
    }

    @Test
    void testLoginSuccessfully() throws Exception {
        LoginRequest loginRequest = new LoginRequest("client@test.com", "password");

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testFailLoginWithWrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("client@test.com", "wrongpass");

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDenyAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAllowAccessWithValidToken() throws Exception {
        String token = obtainAccessToken("client@test.com", "password");

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testDenyClientAccessToAdminEndpoint() throws Exception {
        String clientToken = obtainAccessToken("client@test.com", "password");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAllowAdminAccessToAdminEndpoint() throws Exception {
        String adminToken = obtainAccessToken("admin@test.com", "password");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testRotateRefreshToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("client@test.com", "password"))))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        AuthenticationResponse authResponse = objectMapper.readValue(response, AuthenticationResponse.class);
        String refreshTokenInitial = authResponse.getRefreshToken();

        String jsonBody = "{\"refreshToken\": \"" + refreshTokenInitial + "\"}";

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").value(org.hamcrest.Matchers.not(refreshTokenInitial)));
    }

    private String obtainAccessToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readValue(response, AuthenticationResponse.class).getAccessToken();
    }

}