<template>
  <el-row :gutter="12">
    <el-col :xs="24" :sm="8"><el-card><template #header>全校用户数</template><div class="big">{{ data.totalUsers || 0 }}</div></el-card></el-col>
    <el-col :xs="24" :sm="16"><el-card><template #header>风险分布</template><el-table :data="data.riskDistribution || []"><el-table-column prop="riskLevel" label="等级"/><el-table-column prop="count" label="人数"/></el-table></el-card></el-col>
  </el-row>
  <el-card style="margin-top:12px">
    <template #header>风险趋势</template>
    <el-table :data="data.riskTrend || []"><el-table-column prop="date" label="日期"/><el-table-column prop="avgRiskScore" label="平均风险分"/></el-table>
  </el-card>
</template>
<script setup>
import {reactive,onMounted} from 'vue'
import request from '@/utils/request'
const data = reactive({})
onMounted(()=>request.get('/api/v1/admin/dashboard').then(res => Object.assign(data,res.data || {})))
</script>
<style scoped>
.big{font-size:32px;font-weight:700;color:#409eff}
</style>
