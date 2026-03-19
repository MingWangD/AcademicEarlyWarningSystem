<template>
  <el-card>
    <template #header>学业预警信息</template>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="当前风险等级">
        <el-tag :type="tagType(info.riskLevel)">{{ info.riskLevel || 'LOW' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="当前学分">{{ Number(info.credit || 0).toFixed(2) }} / 4.00</el-descriptions-item>
      <el-descriptions-item label="学分规则">学分 = 作业均分×40% + 考试均分×60%，再按满分 4 分折算。</el-descriptions-item>
      <el-descriptions-item label="风险规则">学分 3~4 为 LOW，2~3 为 MEDIUM，低于 2 为 HIGH。</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>

<script setup>
import {onMounted, reactive} from 'vue'
import request from '@/utils/request'
const info = reactive({})
const tagType = (risk) => ({HIGH:'danger',MEDIUM:'warning',LOW:'success'}[risk] || 'info')
onMounted(()=>request.get('/api/v1/student/info').then(res => Object.assign(info,res.data||{})))
</script>
