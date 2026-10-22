package com.cognara.user;

import com.cognara.user.dto.CreateUserRequest;
import com.cognara.user.dto.UserResponse;
import com.cognara.user.service.UserService;
import com.cognara.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller exposing endpoints for User management.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/users
     * Creates a new User associated with a company.
     *
     * @param request Validated create user request payload
     * @return 201 Created with Location header and UserResponse body
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        assertSameCompany(principal, request.companyId());
        UserResponse response = userService.createUser(request);
        URI location = URI.create("/api/users/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * GET /api/users/{id}
     * Retrieves user details by UUID.
     *
     * @param id User UUID
     * @return 200 OK with UserResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        UserResponse response = userService.getUserByIdForCompany(id, principal.companyId());
        return ResponseEntity.ok(response);
    }

    private void assertSameCompany(UserPrincipal principal, UUID companyId) {
        if (principal == null || !principal.companyId().equals(companyId)) {
            throw new AccessDeniedException("You do not have access to this company");
        }
    }
}
