package com.cognara.user.repository;

import com.cognara.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entities.
 *
 * <p>Spring & JPA Concepts Explained:
 * <ul>
 *   <li>{@code @Repository}: Stereo-type annotation marking this interface as a Data Access Object (DAO).
 *       Spring Data generates the runtime implementation backed by Hibernate.</li>
 *   <li>Tenant-Scoped Derived Queries: Methods such as {@code findByCompanyIdAndEmail} and
 *       {@code existsByCompanyIdAndEmail} automatically construct queries traversing the entity
 *       relationship to filter strictly within the requested company.</li>
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user within a specific company by their email.
     * Enforces tenant-isolated user lookups.
     */
    Optional<User> findByCompanyIdAndEmail(UUID companyId, String email);

    /**
     * Check if an email is already taken within a company.
     */
    boolean existsByCompanyIdAndEmail(UUID companyId, String email);

    /**
     * List all users belonging to a company.
     */
    List<User> findByCompanyId(UUID companyId);
}
