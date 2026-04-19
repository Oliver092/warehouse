package com.example.warehouse.service;

import com.example.warehouse.controller.AuthController;
import com.example.warehouse.dto.LoginRequest;
import com.example.warehouse.dto.RegisterRequest;
import com.example.warehouse.entity.Role;
import com.example.warehouse.entity.User;
import com.example.warehouse.exception.DuplicateResourceException;
import com.example.warehouse.exception.InvalidOperationException;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.repository.UserRepository;
import com.example.warehouse.security.JwtService;
import com.example.warehouse.security.TokenBlacklist;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;

    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_WORKER);
        userRepository.save(user);
        return "User registered successfully";
    }

    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidOperationException("Invalid credentials"));
        return jwtService.generateToken(user);
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expiration = jwtService.extractExpiration(token).getTime()
                    - System.currentTimeMillis();
            tokenBlacklist.blacklist(token, expiration);
        }
    }
}