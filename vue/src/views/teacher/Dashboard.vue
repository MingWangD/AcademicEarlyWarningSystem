<template>
  <el-row :gutter="12" class="card-row">
    <el-col :xs="24" :sm="8"><el-card><template #header>高风险学生</template><div class="num high">{{ summary.highCount || 0 }}</div></el-card></el-col>
    <el-col :xs="24" :sm="8"><el-card><template #header>中风险学生</template><div class="num med">{{ summary.mediumCount || 0 }}</div></el-card></el-col>
    <el-col :xs="24" :sm="8"><el-card><template #header>低风险学生</template><div class="num low">{{ summary.lowCount || 0 }}</div></el-card></el-col>
  <el-row :gutter="12">
    <el-col :span="12"><el-card><template #header>高风险学生</template><div>{{ (data.highRiskStudents||[]).length }}</div></el-card></el-col>
    <el-col :span="12"><el-card><template #header>中风险学生</template><div>{{ (data.mediumRiskStudents||[]).length }}</div></el-card></el-col>
  </el-row>
  <el-card style="margin-top:12px">
    <template #header>风险分布</template>
    <el-table :data="data.riskDistribution || []">
      <el-table-column prop="riskLevel" label="等级"/>
      <el-table-column prop="count" label="人数"/>
    </el-table>
  </el-card>
  <el-card style="margin-top:12px">
    <template #header>最近预警（刷新时间：{{ summary.refreshedAt || '-' }}）</template>
    <template #header>最近预警</template>
    <el-table :data="data.recentWarnings || []">
      <el-table-column prop="studentId" label="学号"/>
      <el-table-column prop="riskLevel" label="等级"/>
      <el-table-column prop="riskScore" label="风险分"/>
      <el-table-column prop="calcDate" label="日期"/>
    </el-table>
  </el-card>
</template>
<script setup>
import {computed, onMounted, reactive} from 'vue'
import request from '@/utils/request'
const data = reactive({})
const summary = computed(() => data.summary || {})
onMounted(()=>request.get('/api/v1/teacher/dashboard').then(res => Object.assign(data,res.data || {})))
</script>
<style scoped>
.num{font-size: 30px;font-weight:700}
.high{color:#d03050}.med{color:#e6a23c}.low{color:#67c23a}
.card-row :deep(.el-card){margin-bottom:12px}
</style>
import {onMounted, reactive} from 'vue'
import request from '@/utils/request'
const data = reactive({})
onMounted(()=>request.get('/api/v1/teacher/dashboard').then(res => Object.assign(data,res.data || {})))
</script>
