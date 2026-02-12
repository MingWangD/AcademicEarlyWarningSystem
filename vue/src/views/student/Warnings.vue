<template>
  <el-card>
    <template #header>学业预警信息</template>
    <el-descriptions :column="1" border>
      <el-descriptions-item label="当前风险等级">
        <el-tag :type="tagType(info.riskLevel)">{{ info.riskLevel || 'LOW' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="预警说明">请按时完成任务并保持稳定学习行为。</el-descriptions-item>
      <el-descriptions-item label="建议">高风险同学建议联系教师制定辅导计划；中风险建议提高作业完成率。</el-descriptions-item>
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
