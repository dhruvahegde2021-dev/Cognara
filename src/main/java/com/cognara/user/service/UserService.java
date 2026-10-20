package com.cognara.user.service;

import com.cognara.common.exception.DuplicateResourceException;
import com.cognara.common.exception.ResourceNotFoundException;
import com.cognara.company.Company;
import com.cognara.company.repository.CompanyRepository;
import com.cognara.user.User;
import com.cognara.user.dto.CreateUserRequest;
import com.cognara.user.dto.UserResponse;
import com.cognara.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service managing User domain logic, tenant verification, and password hashing.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user associated with an existing company.
     *
     * <p>Enforces:
     * <ul>
     *   <li>Company existence verification</li>
     *   <li>Email normalization (trimmed, lowercased)</li>
     *   <li>Company-scoped email uniqueness check</li>
     *   <li>BCrypt password hashing prior to persistence</li>
     * </ul>
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UUID companyId = request.companyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company with ID '" + companyId + "' not found"));

        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByCompanyIdAndEmail(companyId, normalizedEmail)) {
            throw new DuplicateResourceException(
                    "User with email '" + normalizedEmail + "' already exists in company '" + companyId + "'"
            );
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.name().trim(), normalizedEmail, encodedPassword, company);
        User savedUser = userRepository.save(user);

        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Retrieves a user by their UUID identifier.
     */
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID '" + id + "' not found"));
        return UserResponse.fromEntity(user);
    }
}
