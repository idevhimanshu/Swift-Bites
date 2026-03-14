package com.fooddelivery.service;

import com.fooddelivery.dto.AuthDto;
import com.fooddelivery.entity.RefreshToken;
import com.fooddelivery.entity.User;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceAlreadyExistsException;
import com.fooddelivery.exception.UnauthorizedException;
import com.fooddelivery.repository.RefreshTokenRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                       RefreshTokenRepository refreshTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public AuthDto.AuthResponse signup(AuthDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole() != null ? request.getRole() : User.Role.CUSTOMER)
                .build();
        user = userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUserId(user.getId());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthDto.AuthResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (stored.isRevoked()) throw new UnauthorizedException("Refresh token has been revoked");
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new UnauthorizedException("Refresh token expired, please login again");
        }
        refreshTokenRepository.delete(stored);
        return buildAuthResponse(stored.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(t -> { t.setRevoked(true); refreshTokenRepository.save(t); });
    }

    private AuthDto.AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user);
        String rawRefreshToken = jwtUtil.generateRefreshToken(user);

        RefreshToken rt = RefreshToken.builder()
                .token(rawRefreshToken)
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(rt);

        return AuthDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
