package com.cognara.document.repository;

import com.cognara.document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository queries that always scope documents to their tenant. */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndCompanyId(UUID id, UUID companyId);

    List<Document> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}
