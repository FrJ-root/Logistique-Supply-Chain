package org.logistics.service;

import lombok.RequiredArgsConstructor;
import org.logistics.dto.AuthenticationResponse;
import org.logistics.dto.LoginRequest;
import org.logistics.entity.RefreshToken;
import org.logistics.entity.User;
import org.logistics.repository.RefreshTokenRepository;
import org.logistics.security.CustomUserDetails;
import org.logistics.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        var refreshToken = createRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthenticationResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(Instant.now()))
                .filter(t -> !t.isRevoked())
                .orElseThrow(() -> new RuntimeException("Refresh token invalide ou expiré"));

        User user = refreshToken.getUser();

        refreshTokenRepository.delete(refreshToken);

        String newAccessToken = jwtService.generateToken(new CustomUserDetails(user));
        RefreshToken newRefreshToken = createRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(604800000))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

}