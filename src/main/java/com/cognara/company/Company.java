package com.cognara.company;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Company entity represents an organization/tenant within Cognara.
 *
 * <p>Key Concepts:
 * <ul>
 *   <li>@Entity – Identifies this class as a persistent JPA entity mapped to a relational table.</li>
 *   <li>@Table – Specifies the table name (`companies`) and database constraints like unique indexes.</li>
 *   <li>UUID – Universally unique identifier used as primary key to prevent enumeration attacks and simplify distributed IDs.</li>
 *   <li>slug – A URL-friendly unique human identifier (e.g. "acme-corp") used for tenant scoping/routing.</li>
 * </ul>
 */
@Entity
@Table(name = "companies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_companies_slug", columnNames = "slug")
})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Company() {
        // Default constructor required by JPA
    }

    public Company(String name, String slug) {
        this.name = name;
        this.slug = slug;
        this.createdAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company company)) return false;
        return Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
