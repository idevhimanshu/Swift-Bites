package com.fooddelivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.dto.AuthDto;
import com.fooddelivery.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("Signup → Login → Get token flow")
    void signupAndLoginFlow() throws Exception {
        // 1. Signup
        AuthDto.SignupRequest signup = AuthDto.SignupRequest.builder()
                .name("Test User").email("testuser@example.com")
                .password("test1234").phone("9999999999")
                .role(User.Role.CUSTOMER).build();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // 2. Login
        AuthDto.LoginRequest login = AuthDto.LoginRequest.builder()
                .email("testuser@example.com").password("test1234").build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("accessToken");
    }

    @Test
    @DisplayName("Duplicate email signup returns 409 Conflict")
    void duplicateSignupReturnsConflict() throws Exception {
        AuthDto.SignupRequest signup = AuthDto.SignupRequest.builder()
                .name("Dup User").email("dup@example.com")
                .password("pass1234").role(User.Role.CUSTOMER).build();

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Login with wrong password returns 401")
    void wrongPasswordReturns401() throws Exception {
        AuthDto.LoginRequest login = AuthDto.LoginRequest.builder()
                .email("rahul@example.com").password("wrongpassword").build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
