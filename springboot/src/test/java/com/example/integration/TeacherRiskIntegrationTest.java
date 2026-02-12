package com.example.integration;

import com.example.mapper.ActivityMapper;
import com.example.mapper.TaskMapper;
import com.example.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeacherRiskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityMapper activityMapper;
    @MockBean
    private UserMapper userMapper;
    @MockBean
    private TaskMapper taskMapper;

    @Test
    @WithMockUser(roles = "TEACHER")
    void calcRiskShouldReturnSummaryAndModelParams() throws Exception {
        when(activityMapper.historicalFeatures(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(
                Map.of("avgScore", 58, "homeworkSubmitted", 0, "videoMinutes", 5),
                Map.of("avgScore", 80, "homeworkSubmitted", 2, "videoMinutes", 45),
                Map.of("avgScore", 66, "homeworkSubmitted", 1, "videoMinutes", 20)
        ));
        when(activityMapper.featureRowsByDate(any(LocalDate.class))).thenReturn(List.of(
                Map.of("studentId", 3, "loginCount", 2, "videoMinutes", 10, "homeworkSubmitted", 0, "avgScore", 50, "examCount", 1, "examPassCount", 0)
        ));

        mockMvc.perform(post("/api/v1/teacher/risk/calc")
                        .contentType("application/json")
                        .content("{" + "\"date\":\"2026-02-12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.updatedCount").value(1))
                .andExpect(jsonPath("$.data.modelParams.bias").exists())
                .andExpect(jsonPath("$.data.summary").value("已完成每日风险更新"));
    }
}
