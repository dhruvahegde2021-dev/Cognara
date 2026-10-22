package com.cognara.health;

import com.cognara.security.JwtService;
import com.cognara.config.SecurityConfig;
import com.cognara.security.CustomAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit-level test for HealthController.
 *
 * <p>Concepts:
 * <ul>
 *   <li>@WebMvcTest – loads ONLY the Spring MVC layer (controllers, filters,
 *       converters). It does NOT start the full application context, which means
 *       it does NOT need a database connection. This makes the test fast and
 *       isolated.</li>
 *   <li>MockMvc – a test utility that simulates HTTP requests to your controllers
 *       without actually starting a real HTTP server. You call perform() with a
 *       request builder, then chain result matchers.</li>
 *   <li>jsonPath() – evaluates a JSONPath expression against the response body.
 *       "$.status" means "the 'status' field at the root of the JSON object".</li>
 * </ul>
 */
@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class)
class HealthControllerTest {

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * MockMvc is injected by Spring's test framework when using @WebMvcTest.
     * It wires up the controller under test and intercepts HTTP calls.
     */
    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpoint_returnsOkWithStatusUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                // Verify the HTTP status code is 200 OK
                .andExpect(status().isOk())
                // Verify Content-Type header contains application/json
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                // Verify the JSON body has status = "UP"
                .andExpect(jsonPath("$.status").value("UP"))
                // Verify the application name field is present
                .andExpect(jsonPath("$.application").value("cognara-backend"))
                // Verify a timestamp field exists (we don't check the exact value)
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
