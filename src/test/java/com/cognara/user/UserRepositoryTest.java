package com.cognara.user;

import com.cognara.company.Company;
import com.cognara.company.repository.CompanyRepository;
import com.cognara.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataJpaTest testing User entity mapping, relationships, and company-scoped email uniqueness.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    @DisplayName("Users with identical email can exist in different companies (tenant-scoped uniqueness)")
    void canCreateSameEmailInDifferentCompanies() {
        Company companyA = companyRepository.save(new Company("Company A", "company-a"));
        Company companyB = companyRepository.save(new Company("Company B", "company-b"));

        User userA = new User("Alice A", "alice@example.com", "hash123", companyA);
        User userB = new User("Alice B", "alice@example.com", "hash456", companyB);

        userRepository.save(userA);
        userRepository.save(userB);

        Optional<User> foundA = userRepository.findByCompanyIdAndEmail(companyA.getId(), "alice@example.com");
        Optional<User> foundB = userRepository.findByCompanyIdAndEmail(companyB.getId(), "alice@example.com");

        assertThat(foundA).isPresent();
        assertThat(foundA.get().getCompany().getId()).isEqualTo(companyA.getId());
        assertThat(foundB).isPresent();
        assertThat(foundB.get().getCompany().getId()).isEqualTo(companyB.getId());
    }

    @Test
    @DisplayName("Users with duplicate email within the same company violate uniqueness constraint")
    void duplicateEmailInSameCompanyThrowsDataIntegrityViolation() {
        Company company = companyRepository.save(new Company("Acme", "acme"));

        User user1 = new User("Alice", "alice@example.com", "hash1", company);
        userRepository.saveAndFlush(user1);

        User user2 = new User("Alice Duplicate", "alice@example.com", "hash2", company);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
