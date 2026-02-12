package com.example.integration;

import com.example.entity.AppUser;
import com.example.enums.RiskLevel;
import com.example.enums.UserRole;
import com.example.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserMapper userMapper;

    @Test
    void loginShouldReturnJwtTokenForMatchedRole() throws Exception {
        AppUser user = new AppUser();
        user.setId(2L);
        user.setUsername("teacher01");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRole(UserRole.TEACHER);
        user.setRiskLevel(RiskLevel.LOW);

        when(userMapper.findByUsername("teacher01")).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"username":"teacher01","password":"123456","role":"TEACHER"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("TEACHER"));
    }
}
