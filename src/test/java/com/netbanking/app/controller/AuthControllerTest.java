package com.netbanking.app.controller;

import com.banking.core.entity.Address;
import com.banking.core.entity.User;
import com.banking.core.repository.UserRepository;
import com.banking.core.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netbanking.app.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@bank.com");
        testUser.setMobile("+1-555-0123");
        testUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
        testUser.setPasswordHash(PasswordUtil.encode("Test@123"));
        testUser.setRole("ROLE_USER");
        
        Address address = new Address("123 Test St", "Test City", "TS", "12345", "USA");
        testUser.setAddress(address);
        
        userRepository.save(testUser);
    }

    @Test
    void testSuccessfulLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@bank.com", "Test@123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.userEmail").value("test@bank.com"))
                .andExpect(jsonPath("$.userRole").value("ROLE_USER"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        System.out.println("Login response: " + responseContent);
    }

    @Test
    void testInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@bank.com", "WrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNonExistentUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@bank.com", "Test@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidEmailFormat() throws Exception {
        LoginRequest loginRequest = new LoginRequest("invalid-email", "Test@123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEmptyCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}
