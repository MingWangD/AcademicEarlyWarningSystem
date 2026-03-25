package com.example.api;

import com.example.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Test
    void shouldRejectWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/student/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldReturnTasksForStudentRole() throws Exception {
        when(studentService.getTasks(any(), anyInt(), anyInt())).thenReturn(Map.of("list", List.of()));
        mockMvc.perform(get("/api/v1/student/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldReturnCreditRiskForStudentRole() throws Exception {
        when(studentService.creditRisk(any())).thenReturn(Map.of(
                "creditCompletionRate", 0.75,
                "failedCreditRatio", 0.2,
                "isAtRisk", true
        ));
        mockMvc.perform(get("/api/v1/student/credit-risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.isAtRisk").value(true));
    }
}
