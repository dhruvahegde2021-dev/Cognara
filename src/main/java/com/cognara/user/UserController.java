package com.cognara.user;

import com.cognara.user.dto.CreateUserRequest;
import com.cognara.user.dto.UserResponse;
import com.cognara.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
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
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
}
