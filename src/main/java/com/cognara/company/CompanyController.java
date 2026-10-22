package com.cognara.company;

import com.cognara.company.dto.CompanyResponse;
import com.cognara.company.dto.CreateCompanyRequest;
import com.cognara.company.service.CompanyService;
import com.cognara.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller exposing endpoints for Company management.
 *
 * <p>Spring Annotations Explained:
 * <ul>
 *   <li>{@code @RestController}: Combines {@code @Controller} and {@code @ResponseBody}. Tells Spring
 *       that every method return value should be directly written into the HTTP response body as JSON.</li>
 *   <li>{@code @RequestMapping("/api/companies")}: Sets the base URL path for all endpoints in this controller.</li>
 *   <li>{@code @PostMapping}: Maps HTTP POST requests to {@code /api/companies} for resource creation.</li>
 *   <li>{@code @GetMapping("/{id}")}: Maps HTTP GET requests to {@code /api/companies/{id}} for resource lookup.</li>
 *   <li>{@code @Valid}: Triggers Jakarta Bean Validation on the incoming request body before the method executes.</li>
 *   <li>{@code @RequestBody}: Tells Spring to deserialize the incoming JSON HTTP request body into the DTO record.</li>
 *   <li>{@code @PathVariable}: Extracts URI template variables (like {@code {id}}) into method arguments.</li>
 * </ul>
 *
 * <p>Constructor Injection:
 * Dependencies (like {@link CompanyService}) are declared as final and supplied via the public constructor.
 * Spring automatically injects the corresponding bean without requiring {@code @Autowired}.
 */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    // Constructor injection: Spring provides the CompanyService bean here
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * POST /api/companies
     * Creates a new Company organization (tenant).
     *
     * @param request Validated create request payload
     * @return 201 Created with Location header and CompanyResponse body
     */
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        URI location = URI.create("/api/companies/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * GET /api/companies/{id}
     * Retrieves company details by its UUID.
     *
     * @param id Company UUID
     * @return 200 OK with CompanyResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        if (principal == null || !principal.companyId().equals(id)) {
            throw new AccessDeniedException("You do not have access to this company");
        }
        CompanyResponse response = companyService.getCompanyById(id);
        return ResponseEntity.ok(response);
    }
}
