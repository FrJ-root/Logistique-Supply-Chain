package org.logistics.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logistics.dto.LoginRequest;
import org.logistics.entity.User;
import org.logistics.enums.Role;
import org.logistics.repository.RefreshTokenRepository;
import org.logistics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String clientToken;
    private String adminToken;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setup() throws Exception {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        User client = User.builder()
                .email("client@test.com")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.CLIENT)
                .active(true)
                .build();
        userRepository.save(client);

        User admin = User.builder()
                .email("admin@test.com")
                .passwordHash(passwordEncoder.encode("adminpass"))
                .role(Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);
    }

    @Test
    void shouldAuthenticateAndReturnTokens() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("client@test.com");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldRejectInvalidLogin() throws Exception {
        // Scénario : Login invalide
        LoginRequest request = new LoginRequest();
        request.setEmail("client@test.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDenyAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyAdminRouteForClient() throws Exception {
        String token = getAccessToken("client@test.com", "password");

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldHandleRefreshTokenRenewal() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("client@test.com", "password"))))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(responseBody).get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    private String getAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        User testUser = User.builder()
                .email("expired@test.com")
                .role(Role.CLIENT)
                .active(true)
                .build();

        String expiredToken = jwtService.generateToken(
                new org.logistics.security.CustomUserDetails(testUser),
                -3600000
        );

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRefuseAccessToOtherClientResource() throws Exception {
        User userB = User.builder().email("clientB@test.com").passwordHash(passwordEncoder.encode("pass")).role(Role.CLIENT).build();
        userRepository.save(userB);

        String tokenA = getAccessToken("client@test.com", "password");

        mockMvc.perform(get("/api/orders/99")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());
    }

}