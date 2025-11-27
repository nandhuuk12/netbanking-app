package com.netbanking.app.controller;

import com.banking.core.entity.User;
import com.banking.core.service.UserService;
import com.netbanking.app.dto.JwtResponse;
import com.netbanking.app.dto.LoginRequest;
import com.netbanking.app.security.UserDetailsServiceImpl;
import com.netbanking.app.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                         UserService userService,
                         UserDetailsServiceImpl userDetailsService,
                         JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            
            User user = userOpt.get();

            // Generate JWT tokens
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

            JwtResponse jwtResponse = new JwtResponse(
                    accessToken,
                    refreshToken,
                    jwtUtil.getExpiration(),
                    user.getEmail(),
                    user.getRole()
            );

            logger.info("Login successful for user: {}", loginRequest.getEmail());
            return ResponseEntity.ok(jwtResponse);

        } catch (Exception e) {
            logger.error("Login failed for user: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Generate new access token using refresh token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String refreshToken = jwtUtil.extractTokenFromHeader(authorizationHeader);
            
            if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
                String username = jwtUtil.extractUsername(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                String newAccessToken = jwtUtil.generateToken(userDetails);
                
                JwtResponse jwtResponse = new JwtResponse();
                jwtResponse.setAccessToken(newAccessToken);
                jwtResponse.setRefreshToken(refreshToken);
                jwtResponse.setExpiresIn(jwtUtil.getExpiration());
                jwtResponse.setUserEmail(username);
                
                return ResponseEntity.ok(jwtResponse);
            } else {
                return ResponseEntity.badRequest().body("Invalid refresh token");
            }
        } catch (Exception e) {
            logger.error("Token refresh failed", e);
            return ResponseEntity.badRequest().body("Token refresh failed");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user (client should discard tokens)")
    public ResponseEntity<?> logoutUser() {
        // In a stateless JWT setup, logout is handled on the client side
        // by discarding the tokens. Server-side token blacklisting can be implemented
        // for enhanced security but is not implemented in this basic version.
        
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }
}
