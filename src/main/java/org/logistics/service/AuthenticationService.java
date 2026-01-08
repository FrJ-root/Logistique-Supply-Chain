package org.logistics.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.transaction.annotation.Transactional;
import org.logistics.repository.RefreshTokenRepository;
import org.logistics.dto.AuthenticationResponse;
import org.logistics.security.CustomUserDetails;
import org.logistics.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.logistics.security.JwtService;
import org.logistics.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.logistics.dto.LoginRequest;
import org.logistics.entity.User;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthenticationResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        var refreshToken = createRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthenticationResponse refreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .filter(t -> t.getExpiryDate().isAfter(Instant.now()))
                .filter(t -> !t.isRevoked())
                .orElseThrow(() -> new RuntimeException("Refresh Token invalide ou expiré"));

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
                .revoked(false)
                .expiryDate(Instant.now().plusMillis(604800000))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}