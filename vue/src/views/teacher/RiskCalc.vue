<template>
  <el-space direction="vertical" fill :size="12">
    <el-card class="hero">
      <template #header>触发学业风险计算</template>
      <div class="toolbar">
        <el-date-picker v-model="date" value-format="YYYY-MM-DD" />
        <el-button type="primary" @click="calc">立即计算</el-button>
      </div>
      <p class="hint">支持按日期重算风险，结果将写入风险历史并刷新教师看板。</p>
    </el-card>

    <el-row v-if="result" :gutter="12">
      <el-col :xs="24" :sm="8">
        <el-card>
          <template #header>更新人数</template>
          <div class="kpi">{{ result.updatedCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card>
          <template #header>高风险阈值（完成率）</template>
          <div class="kpi warn">{{ Number(result?.rule?.creditCompletionHighRiskThreshold || 0).toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card>
          <template #header>挂科学分阈值</template>
          <div class="kpi">{{ Number(result?.rule?.failedCreditHighRiskThreshold || 0).toFixed(2) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-result
      v-if="result"
      icon="success"
      title="计算完成"
      :sub-title="`更新人数: ${result.updatedCount}，${result.summary}`"
    />

    <el-card v-if="result?.rule">
      <template #header>学分风险规则</template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="作业权重">{{ result.rule.homeworkWeight }}</el-descriptions-item>
        <el-descriptions-item label="考试权重">{{ result.rule.examWeight }}</el-descriptions-item>
        <el-descriptions-item label="总学分">{{ result.rule.totalCredit }}</el-descriptions-item>
        <el-descriptions-item label="LOW 区间">{{ result.rule.lowRange }}</el-descriptions-item>
        <el-descriptions-item label="MEDIUM 区间">{{ result.rule.mediumRange }}</el-descriptions-item>
        <el-descriptions-item label="HIGH 区间">{{ result.rule.highRange }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="result?.modelParams">
      <template #header>模型指标（可解释）</template>
      <el-row :gutter="12">
        <el-col :xs="24" :sm="6"><el-progress type="dashboard" :percentage="toPercent(result.modelParams.metrics?.precision)"><template #default>Precision</template></el-progress></el-col>
        <el-col :xs="24" :sm="6"><el-progress type="dashboard" :percentage="toPercent(result.modelParams.metrics?.recall)"><template #default>Recall</template></el-progress></el-col>
        <el-col :xs="24" :sm="6"><el-progress type="dashboard" :percentage="toPercent(result.modelParams.metrics?.f1)"><template #default>F1</template></el-progress></el-col>
        <el-col :xs="24" :sm="6"><el-progress type="dashboard" :percentage="toPercent(result.modelParams.metrics?.accuracy)"><template #default>Accuracy</template></el-progress></el-col>
      </el-row>
    </el-card>

    <el-card v-if="result?.sample?.length">
      <template #header>样本预览（前5条）</template>
      <el-table :data="result.sample" size="small">
        <el-table-column prop="studentId" label="学号" width="100" />
        <el-table-column prop="credit" label="学分" />
        <el-table-column prop="creditCompletionRate" label="完成率" />
        <el-table-column prop="failedCreditRatio" label="挂科占比" />
        <el-table-column prop="riskScore" label="风险分" />
        <el-table-column prop="riskLevel" label="风险等级" />
      </el-table>
    </el-card>
  </el-space>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'
const date = ref('')
const result = ref(null)
const toPercent = (num) => Math.round(Number(num || 0) * 100)
const calc = () => request.post('/api/v1/teacher/risk/calc', { date: date.value }).then(res => result.value = res.data)
</script>

<style scoped>
.toolbar{display:flex;gap:12px;align-items:center;flex-wrap:wrap}
.hint{margin:8px 0 0;color:#64748b}
.hero{background:linear-gradient(135deg,#eff6ff,#f8fafc)}
.kpi{font-size:30px;font-weight:700;color:#334155}
.warn{color:#d97706}
</style>
