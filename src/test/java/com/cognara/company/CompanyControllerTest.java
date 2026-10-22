package com.cognara.company;

import com.cognara.common.exception.DuplicateResourceException;
import com.cognara.common.exception.GlobalExceptionHandler;
import com.cognara.common.exception.ResourceNotFoundException;
import com.cognara.company.dto.CompanyResponse;
import com.cognara.company.dto.CreateCompanyRequest;
import com.cognara.company.service.CompanyService;
import com.cognara.config.SecurityConfig;
import com.cognara.security.CustomAuthenticationEntryPoint;
import com.cognara.security.JwtService;
import com.cognara.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.UUID;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvc unit test testing CompanyController endpoints, validation, and error translation.
 */
@WebMvcTest(CompanyController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Test
    @DisplayName("POST /api/companies succeeds with 201 Created and response body")
    void createCompany_withValidRequest_returnsCreated() throws Exception {
        UUID companyId = UUID.randomUUID();
        CompanyResponse mockResponse = new CompanyResponse(
                companyId,
                "Acme Corporation",
                "acme-corp",
                Instant.now()
        );

        when(companyService.createCompany(any(CreateCompanyRequest.class))).thenReturn(mockResponse);

        CreateCompanyRequest request = new CreateCompanyRequest("Acme Corporation", "acme-corp");

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/companies/" + companyId))
                .andExpect(jsonPath("$.id").value(companyId.toString()))
                .andExpect(jsonPath("$.name").value("Acme Corporation"))
                .andExpect(jsonPath("$.slug").value("acme-corp"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/companies fails with 400 Bad Request when name or slug is blank")
    void createCompany_withInvalidPayload_returnsBadRequest() throws Exception {
        CreateCompanyRequest request = new CreateCompanyRequest("", "Invalid Slug!");

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.slug").exists());
    }

    @Test
    @DisplayName("POST /api/companies fails with 409 Conflict when slug already exists")
    void createCompany_withDuplicateSlug_returnsConflict() throws Exception {
        when(companyService.createCompany(any(CreateCompanyRequest.class)))
                .thenThrow(new DuplicateResourceException("A company with slug 'acme-corp' already exists"));

        CreateCompanyRequest request = new CreateCompanyRequest("Acme Corporation", "acme-corp");

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A company with slug 'acme-corp' already exists"));
    }

    @Test
    @DisplayName("GET /api/companies/{id} succeeds with 200 OK when company exists")
    void getCompanyById_whenFound_returnsCompany() throws Exception {
        UUID companyId = UUID.randomUUID();
        CompanyResponse mockResponse = new CompanyResponse(
                companyId,
                "Acme Corporation",
                "acme-corp",
                Instant.now()
        );

        when(companyService.getCompanyById(companyId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/companies/{id}", companyId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                new UserPrincipal(UUID.randomUUID(), "user@acme.test", companyId), null,
                                Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyId.toString()))
                .andExpect(jsonPath("$.name").value("Acme Corporation"))
                .andExpect(jsonPath("$.slug").value("acme-corp"));
    }

    @Test
    @DisplayName("GET /api/companies/{id} fails with 404 Not Found when company does not exist")
    void getCompanyById_whenNotFound_returnsNotFound() throws Exception {
        UUID companyId = UUID.randomUUID();
        when(companyService.getCompanyById(companyId))
                .thenThrow(new ResourceNotFoundException("Company with ID '" + companyId + "' not found"));

        mockMvc.perform(get("/api/companies/{id}", companyId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                new UserPrincipal(UUID.randomUUID(), "user@acme.test", companyId), null,
                                Collections.emptyList()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Company with ID '" + companyId + "' not found"));
    }
}
