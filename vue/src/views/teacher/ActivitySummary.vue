<template>
  <div class="activity-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-wrap">
        <div>
          <div class="hero-title">学生行为与学分总览</div>
          <div class="hero-sub">行为数据 + 学分联动展示，帮助快速识别学习状态</div>
        </div>
        <el-date-picker v-model="date" value-format="YYYY-MM-DD" @change="load"/>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table :data="rows" stripe class="colorful-table">
        <el-table-column prop="studentId" label="学号" width="110"/>
        <el-table-column prop="studentName" label="姓名" min-width="120"/>
        <el-table-column prop="credit" label="学分" width="110">
          <template #default="scope">
            <el-tag :type="creditTag(scope.row.credit)">{{ Number(scope.row.credit || 0).toFixed(2) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="loginCount" label="登录次数" width="120"/>
        <el-table-column prop="videoMinutes" label="视频时长(分钟)" width="140"/>
        <el-table-column prop="homeworkSubmitted" label="作业提交数" width="130"/>
        <el-table-column prop="avgScore" label="平均分" min-width="180">
          <template #default="scope">
            <div class="score-cell">
              <el-progress :percentage="Math.round(Number(scope.row.avgScore || 0))" :stroke-width="10" :show-text="false" />
              <span class="score-text">{{ Number(scope.row.avgScore || 0).toFixed(1) }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<script setup>
import {ref, onMounted} from 'vue'
import request from '@/utils/request'
const date = ref('')
const rows = ref([])
const load = () => request.get('/api/v1/teacher/student/activity', {params: {date: date.value}}).then(res => rows.value = res.data || [])
const creditTag = (credit) => {
  const c = Number(credit || 0)
  if (c >= 3) return 'success'
  if (c >= 2) return 'warning'
  return 'danger'
}
onMounted(load)
</script>

<style scoped>
.activity-page { display: grid; gap: 14px; }
.hero-card {
  border: none;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0e7ff 40%, #f5f3ff 100%);
}
.hero-wrap { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.hero-title { font-size: 20px; font-weight: 700; color: #374151; }
.hero-sub { margin-top: 6px; font-size: 13px; color: #6b7280; }
.table-card { border-radius: 14px; }
.colorful-table :deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, #22c55e 0%, #3b82f6 50%, #a855f7 100%);
}
.score-cell { display: flex; align-items: center; gap: 10px; }
.score-text { color: #4b5563; font-weight: 600; min-width: 38px; }
</style>
