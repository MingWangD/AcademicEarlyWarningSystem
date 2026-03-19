<template>
  <div class="student-tasks-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero-content">
        <div>
          <div class="hero-title">学习任务中心</div>
          <div class="hero-subtitle">风险等级由作业与考试成绩折算的学分决定（视频仅记录学习行为）</div>
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

      <div class="pagination-wrap">
        <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="onSizeChange"
            @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
import router from '@/router'

const tasks = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const load = () => request.get('/api/v1/student/tasks', {
  params: {
    pageNum: pageNum.value,
    pageSize: pageSize.value
  }
}).then(res => {
  const payload = res?.data || {}
  tasks.value = payload.list || []
  total.value = payload.total || 0
})

const onPageChange = (val) => {
  pageNum.value = val
  load()
}

const onSizeChange = (val) => {
  pageSize.value = val
  pageNum.value = 1
  load()
}

const typeLabel = (type) => ({homework: '作业', video: '视频', exam: '考试'}[type] || type)
const taskTypeTag = (type) => ({homework: 'primary', video: 'success', exam: 'warning'}[type] || 'info')
const statusType = (status) => ({'已提交': 'success', '已完成': 'success', '待完成': 'warning', '待观看': 'info', '待考试': 'danger'}[status] || 'info')

const goHomework = (taskId) => router.push(`/portal/student/homework/${taskId}`)
const goExam = (taskId) => router.push(`/portal/student/exam/${taskId}`)
const watchVideo = (row) => request.post('/api/v1/student/video/watch', {videoId: row.taskId, watchTime: 600}).then((res)=>{
  const riskLevel = res?.data?.riskLevel || 'LOW'
  const credit = Number(res?.data?.credit || 0).toFixed(2)
  ElMessage.success(`视频学习进度已记录，当前学分 ${credit}，风险等级 ${riskLevel}`)
  load()
})

onMounted(load)
</script>

<style scoped>
.student-tasks-page {
  display: grid;
  gap: 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #f6f7ff 100%);
  padding: 4px;
  border-radius: 14px;
}
.hero-card {
  border: none;
  background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 45%, #fae8ff 100%);
  box-shadow: 0 10px 28px rgba(99, 102, 241, 0.12);
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
  color: #374151;
}
.task-table-card {
  border-radius: 14px;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.08);
}
.task-table :deep(.el-table__header-wrapper th) {
  background: #eef2ff;
  color: #374151;
}
.task-table :deep(.el-tag) {
  font-weight: 600;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
