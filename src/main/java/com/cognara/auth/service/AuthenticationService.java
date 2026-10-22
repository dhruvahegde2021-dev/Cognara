package com.cognara.auth.service;

import com.cognara.auth.dto.AuthResponse;
import com.cognara.auth.dto.LoginRequest;
import com.cognara.security.JwtService;
import com.cognara.user.User;
import com.cognara.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Validates tenant-scoped credentials and issues a signed, short-lived JWT. */
@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByCompanyIdAndEmail(request.companyId(), email)
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        String token = jwtService.generateToken(
                user.getId(), user.getEmail(), user.getCompany().getId());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(),
                user.getCompany().getId(), user.getCompany().getName());
    }

    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException("Invalid company ID, email, or password");
    }
}
