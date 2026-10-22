package com.cognara.auth.service;

import com.cognara.auth.dto.AuthResponse;
import com.cognara.auth.dto.LoginRequest;
import com.cognara.company.Company;
import com.cognara.security.JwtService;
import com.cognara.security.UserPrincipal;
import com.cognara.user.User;
import com.cognara.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService(
            "test-secret-that-is-safely-longer-than-thirty-two-bytes", 3_600_000);

    @Test
    void login_withValidTenantScopedCredentials_issuesTokenWithIdentityAndCompanyClaims() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Company company = new Company("Acme", "acme");
        company.setId(companyId);
        User user = new User("Ada", "ada@acme.test", passwordEncoder.encode("correct-password"), company);
        user.setId(userId);
        when(userRepository.findByCompanyIdAndEmail(companyId, "ada@acme.test"))
                .thenReturn(Optional.of(user));

        AuthenticationService service = new AuthenticationService(userRepository, passwordEncoder, jwtService);
        AuthResponse response = service.login(new LoginRequest(companyId, " ADA@ACME.TEST ", "correct-password"));

        Claims claims = jwtService.parseAndValidateClaims(response.token());
        UserPrincipal principal = jwtService.extractUserPrincipal(claims);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(principal.userId()).isEqualTo(userId);
        assertThat(principal.email()).isEqualTo("ada@acme.test");
        assertThat(principal.companyId()).isEqualTo(companyId);
    }

    @Test
    void login_withWrongPassword_returnsGenericBadCredentials() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company("Acme", "acme");
        company.setId(companyId);
        User user = new User("Ada", "ada@acme.test", passwordEncoder.encode("correct-password"), company);
        when(userRepository.findByCompanyIdAndEmail(eq(companyId), anyString())).thenReturn(Optional.of(user));

        AuthenticationService service = new AuthenticationService(userRepository, passwordEncoder, jwtService);

        assertThatThrownBy(() -> service.login(new LoginRequest(companyId, "ada@acme.test", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid company ID, email, or password");
    }
}
