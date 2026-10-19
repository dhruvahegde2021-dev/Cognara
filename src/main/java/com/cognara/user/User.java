package com.cognara.user;

import com.cognara.company.Company;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

/**
 * User entity representing an actor belonging to a specific Company.
 *
 * <p>Key Concepts:
 * <ul>
 *   <li>@ManyToOne(fetch = FetchType.LAZY) – Many users can belong to one company.
 *       LAZY loading avoids pulling the entire Company record unless explicitly accessed.</li>
 *   <li>Tenant-Scoped Uniqueness: The composite unique constraint (company_id, email)
 *       guarantees that an email is unique per company, but the same email could exist
 *       under another distinct company (multi-tenancy principle).</li>
 *   <li>Security Design: The passwordHash stores cryptographic hash values (e.g. bcrypt/argon2),
 *       never plain text, and must never be serialized or leaked across DTOs.</li>
 * </ul>
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_company_email", columnNames = {"company_id", "email"})
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_users_company"))
    private Company company;

    public User() {
        // Default constructor required by JPA
    }

    public User(String name, String email, String passwordHash, Company company) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.company = company;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
