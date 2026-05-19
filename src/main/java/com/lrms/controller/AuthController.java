package com.lrms.controller;

import com.lrms.SecurityConfig;
import com.lrms.entity.Staff;
import com.lrms.repository.StaffRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and Security tokens")
public class AuthController {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityConfig.JwtTokenProvider jwtTokenProvider;

    public AuthController(StaffRepository staffRepository, PasswordEncoder passwordEncoder, SecurityConfig.JwtTokenProvider jwtTokenProvider) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Staff Login",
        description = "Authenticates staff and returns a JWT token. The token is automatically applied to Swagger UI upon success.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful", 
                content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
        }
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest credentials) {
        Staff staff = staffRepository.findByEmail(credentials.email()).orElse(null);

        if (staff != null && staff.getIsActive() && passwordEncoder.matches(credentials.password(), staff.getPasswordHash())) {
            String token = jwtTokenProvider.generateToken(staff.getEmail());
            return ResponseEntity.ok(new LoginResponse(token, staff.getRole(), staff.getFullName()));
        }

        return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
    }

    // Small, flat DTOs for documentation clarity
    public record LoginRequest(
        @Schema(example = "admin@lrms.com") String email, 
        @Schema(example = "admin123") String password
    ) {}

    public record LoginResponse(String token, String role, String name) {}
}
