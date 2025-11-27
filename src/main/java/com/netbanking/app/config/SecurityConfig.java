package com.netbanking.app.config;

import com.netbanking.app.security.JwtAuthenticationEntryPoint;
import com.netbanking.app.security.JwtAuthenticationFilter;
import com.netbanking.app.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for NetBanking application
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                         JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                
                // Admin only endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/users/admin/**").hasRole("ADMIN")
                .requestMatchers("/loans/admin/**").hasRole("ADMIN")
                .requestMatchers("/insurance/admin/**").hasRole("ADMIN")
                
                // User endpoints
                .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/accounts/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/transactions/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/cards/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/loans/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/insurance/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/branches/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/policies/**").hasAnyRole("USER", "ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions().disable()); // For H2 console

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
