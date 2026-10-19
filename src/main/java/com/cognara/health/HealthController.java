package com.cognara.health;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health-check endpoint for the Cognara API.
 *
 * <p>Concepts:
 * <ul>
 *   <li>@RestController – combines @Controller and @ResponseBody. Every method
 *       return value is automatically serialised to JSON and written to the HTTP
 *       response body (using Jackson, which Spring Boot includes by default).</li>
 *   <li>@RequestMapping("/api") – all routes in this class are prefixed with /api.</li>
 *   <li>@GetMapping – maps HTTP GET requests to a method.</li>
 *   <li>ResponseEntity – gives you full control over the HTTP response: status
 *       code, headers, and body.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health
     *
     * <p>Returns a small JSON object confirming the application is running.
     * This is a lightweight custom endpoint distinct from Spring Actuator's
     * /actuator/health — it is meant as a simple API-level ping.
     *
     * @return 200 OK with a JSON body
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = Map.of(
                "status", "UP",
                "application", "cognara-backend",
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.ok(body);
    }
}
