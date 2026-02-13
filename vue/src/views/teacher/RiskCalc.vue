<template>
  <el-card>
    <template #header>触发学业风险计算</template>
    <div style="display:flex;gap:12px;align-items:center;flex-wrap:wrap">
      <el-date-picker v-model="date" value-format="YYYY-MM-DD"/>
      <el-button type="primary" @click="calc">立即计算</el-button>
    </div>
    <el-result v-if="result" icon="success" title="计算完成" :sub-title="`更新人数: ${result.updatedCount}，${result.summary}`"/>
    <el-descriptions v-if="result?.modelParams" :column="2" border title="本次模型参数（CV 选择）">
      <el-descriptions-item label="bias">{{ result.modelParams.bias }}</el-descriptions-item>
      <el-descriptions-item label="scoreWeight">{{ result.modelParams.scoreWeight }}</el-descriptions-item>
      <el-descriptions-item label="examFailWeight">{{ result.modelParams.examFailWeight }}</el-descriptions-item>
      <el-descriptions-item label="homeworkWeight">{{ result.modelParams.homeworkWeight }}</el-descriptions-item>
      <el-descriptions-item label="videoWeight">{{ result.modelParams.videoWeight }}</el-descriptions-item>
      <el-descriptions-item label="loginWeight">{{ result.modelParams.loginWeight }}</el-descriptions-item>
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
