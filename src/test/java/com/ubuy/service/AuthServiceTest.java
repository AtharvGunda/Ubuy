package com.ubuy.service;

import com.ubuy.config.JwtUtil;
import com.ubuy.dto.auth.AuthResponse;
import com.ubuy.dto.auth.LoginRequest;
import com.ubuy.dto.auth.RegisterRequest;
import com.ubuy.entity.Role;
import com.ubuy.entity.User;
import com.ubuy.exception.BadRequestException;
import com.ubuy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authManager;
    @Mock JwtUtil jwtUtil;
    @InjectMocks AuthService authService;

    @Test
    void register_savesUserWithHashedPassword() {
        RegisterRequest req = new RegisterRequest("Alice", "alice@ubuy.com", "password123");
        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse resp = authService.register(req);

        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("alice@ubuy.com") &&
                u.getPassword().equals("hashed") &&
                u.getRole() == Role.CUSTOMER));
        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getEmail()).isEqualTo("alice@ubuy.com");
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        when(userRepository.findByEmail("alice@ubuy.com"))
                .thenReturn(Optional.of(new User()));
        RegisterRequest req = new RegisterRequest("Alice", "alice@ubuy.com", "password123");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_returnsToken_forValidCredentials() {
        User user = new User(1L, "Alice", "alice@ubuy.com", "hashed", Role.CUSTOMER);
        when(userRepository.findByEmail("alice@ubuy.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        LoginRequest req = new LoginRequest("alice@ubuy.com", "password123");
        AuthResponse resp = authService.login(req);

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getRole()).isEqualTo("CUSTOMER");
    }
}

