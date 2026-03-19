<template>
  <el-card>
    <template #header>触发学业风险计算</template>
    <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
      <el-date-picker v-model="date" value-format="YYYY-MM-DD"/>
      <el-button type="primary" @click="calc">立即计算</el-button>
    </div>
    <el-result v-if="result" icon="success" title="计算完成" :sub-title="`更新人数: ${result.updatedCount}，${result.summary}`"/>
    <el-descriptions v-if="result?.rule" :column="2" border title="学分风险规则">
      <el-descriptions-item label="作业权重">{{ result.rule.homeworkWeight }}</el-descriptions-item>
      <el-descriptions-item label="考试权重">{{ result.rule.examWeight }}</el-descriptions-item>
      <el-descriptions-item label="总学分">{{ result.rule.totalCredit }}</el-descriptions-item>
      <el-descriptions-item label="LOW 区间">{{ result.rule.lowRange }}</el-descriptions-item>
      <el-descriptions-item label="MEDIUM 区间">{{ result.rule.mediumRange }}</el-descriptions-item>
      <el-descriptions-item label="HIGH 区间">{{ result.rule.highRange }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>
<script setup>
import {ref} from 'vue'
import request from '@/utils/request'
const date = ref('')
const result = ref(null)
const calc = () => request.post('/api/v1/teacher/risk/calc', {date: date.value}).then(res => result.value = res.data)
</script>
