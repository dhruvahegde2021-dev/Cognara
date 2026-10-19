package com.cognara.company.repository;

import com.cognara.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Company entities.
 *
 * <p>Spring & JPA Concepts Explained:
 * <ul>
 *   <li>{@code @Repository}: Stereo-type annotation marking this interface as a Data Access Object (DAO).
 *       Spring Data detects interfaces extending {@link JpaRepository} and automatically generates
 *       proxy implementations at runtime with standard CRUD methods (save, findById, delete, etc.).</li>
 *   <li>Derived Query Methods: Methods like {@code existsBySlug(String slug)} and
 *       {@code findBySlug(String slug)} are parsed by Spring Data based on method name keywords
 *       (e.g., "existsBy", "findBy"), creating parameterized SQL queries automatically.</li>
 * </ul>
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    /**
     * Check if a company with the given slug already exists.
     * Useful for pre-insert validation before hitting database unique constraints.
     */
    boolean existsBySlug(String slug);

    /**
     * Retrieve a company by its URL-friendly slug.
     */
    Optional<Company> findBySlug(String slug);
}
