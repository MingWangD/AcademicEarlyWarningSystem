<template>
  <el-card>
    <template #header>触发学业风险计算</template>
    <div style="display:flex;gap:12px;align-items:center">
      <el-date-picker v-model="date" value-format="YYYY-MM-DD"/>
      <el-button type="primary" @click="calc">立即计算</el-button>
    </div>
    <el-result v-if="result" icon="success" title="计算完成" :sub-title="`更新人数: ${result.updatedCount}，${result.summary}`"/>
  </el-card>
</template>
<script setup>
import {ref} from 'vue'
import request from '@/utils/request'
const date = ref('')
const result = ref(null)
const calc = () => request.post('/api/v1/teacher/risk/calc', {date: date.value}).then(res => result.value = res.data)
</script>
