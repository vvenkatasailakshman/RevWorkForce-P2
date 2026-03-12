package com.rev.app.rest;

import com.rev.app.dto.ApiRegistrationRequest;
import com.rev.app.dto.AuthRequest;
import com.rev.app.dto.AuthResponse;
import com.rev.app.entity.User;
import com.rev.app.security.JwtUtil;
import com.rev.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller providing JWT-based authentication endpoints.
 * All endpoints are under /api/auth and are publicly accessible.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // -----------------------------------------------------------------------
    // POST /api/auth/login
    // -----------------------------------------------------------------------

    /**
     * Authenticate with email + password and receive a signed JWT token.
     *
     * Example request:
     *   POST /api/auth/login
     *   { "email": "admin@acme.com", "password": "secret" }
     *
     * Example response:
     *   { "token": "eyJ...", "email": "admin@acme.com", "role": "ROLE_ADMIN", "expiresIn": 3600 }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_EMPLOYEE");

            AuthResponse response = new AuthResponse(
                    token,
                    userDetails.getUsername(),
                    role,
                    expirationMs / 1000L  // convert ms → seconds
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/register
    // -----------------------------------------------------------------------

    /**
     * Register a new user via the API.
     * Delegates to the existing UserService.registerUser() for consistency.
     *
     * Example request:
     *   POST /api/auth/register
     *   { "email": "john@acme.com", "password": "secret123",
     *     "firstName": "John", "lastName": "Doe", "role": "ROLE_EMPLOYEE" }
     *
     * Example response:
     *   201 Created
     *   { "message": "User registered successfully", "email": "john@acme.com", "role": "ROLE_EMPLOYEE" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ApiRegistrationRequest request) {
        User created = userService.registerUser(request.toRegistrationDto());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "User registered successfully",
                        "email", created.getEmail(),
                        "role", created.getRole().name()
                )
        );
    }
}
