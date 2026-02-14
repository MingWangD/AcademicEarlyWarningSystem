<template>
  <div class="student-tasks-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-title">学习任务中心</div>
          <div class="hero-subtitle">完成作业、视频、考试任务后将自动更新你的最新风险评估</div>
        </div>
        <el-button type="primary" plain @click="load">刷新任务</el-button>
      </div>
    </el-card>

    <el-card class="task-table-card">
      <template #header>
        <div class="card-header">任务列表（教师发布）</div>
      </template>

      <el-table :data="tasks" stripe class="task-table" empty-text="暂无任务">
        <el-table-column prop="taskId" label="任务ID" width="90"/>
        <el-table-column prop="type" label="类型" width="110">
          <template #default="scope">
            <el-tag :type="taskTypeTag(scope.row.type)">{{ typeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180"/>
        <el-table-column prop="details" label="任务详情" min-width="220" show-overflow-tooltip/>
        <el-table-column prop="dueDate" label="截止时间" width="180"/>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope"><el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.type === 'homework'"
              size="small"
              type="primary"
              :disabled="scope.row.status==='已提交'"
              @click="goHomework(scope.row.taskId)"
            >提交作业</el-button>
            <el-button
              v-else-if="scope.row.type === 'video'"
              size="small"
              type="success"
              :disabled="scope.row.status==='已完成'"
              @click="watchVideo(scope.row)"
            >记录观看</el-button>
            <el-button
              v-else
              size="small"
              type="warning"
              :disabled="scope.row.status==='已完成'"
              @click="goExam(scope.row.taskId)"
            >参加考试</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
import router from '@/router'

const tasks = ref([])
const load = () => request.get('/api/v1/student/tasks').then(res => tasks.value = res.data || [])

const typeLabel = (type) => ({homework: '作业', video: '视频', exam: '考试'}[type] || type)
const taskTypeTag = (type) => ({homework: 'primary', video: 'success', exam: 'warning'}[type] || 'info')
const statusType = (status) => ({'已提交': 'success', '已完成': 'success', '待完成': 'warning', '待观看': 'info', '待考试': 'danger'}[status] || 'info')

const goHomework = (taskId) => router.push(`/portal/student/homework/${taskId}`)
const goExam = (taskId) => router.push(`/portal/student/exam/${taskId}`)
const watchVideo = (row) => request.post('/api/v1/student/video/watch', {videoId: row.taskId, watchTime: 600}).then((res)=>{
  const riskLevel = res?.data?.riskLevel || 'LOW'
  ElMessage.success(`视频学习进度已记录，最新风险等级 ${riskLevel}`)
  load()
})

onMounted(load)
</script>

<style scoped>
.student-tasks-page {
  display: grid;
  gap: 16px;
}
.hero-card {
  border: none;
  background: linear-gradient(135deg, #ecf5ff, #f4f9ff);
}
.hero-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.hero-title {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.hero-subtitle {
  margin-top: 6px;
  color: #606266;
  font-size: 13px;
}
.card-header {
  font-size: 16px;
  font-weight: 600;
}
.task-table :deep(.el-tag) {
  font-weight: 600;
}
</style>
