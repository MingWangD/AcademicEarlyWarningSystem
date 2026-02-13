<template>
  <el-card>
    <template #header>任务列表（教师发布）</template>
    <el-table :data="tasks" stripe>
      <el-table-column prop="taskId" label="任务ID" width="90"/>
      <el-table-column prop="type" label="类型" width="110">
        <template #default="scope"><el-tag>{{ typeLabel(scope.row.type) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180"/>
      <el-table-column prop="details" label="任务详情" min-width="220" show-overflow-tooltip/>
      <el-table-column prop="dueDate" label="截止时间" width="180"/>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope"><el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button v-if="scope.row.type === 'homework'" size="small" type="primary" :disabled="scope.row.status==='已提交'" @click="goHomework(scope.row.taskId)">提交作业</el-button>
          <el-button v-else-if="scope.row.type === 'video'" size="small" type="success" :disabled="scope.row.status==='已完成'" @click="watchVideo(scope.row)">记录观看</el-button>
          <el-button v-else size="small" type="warning" :disabled="scope.row.status==='已完成'" @click="goExam(scope.row.taskId)">参加考试</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
import router from '@/router'

const tasks = ref([])
const load = () => request.get('/api/v1/student/tasks').then(res => tasks.value = res.data || [])

const typeLabel = (type) => ({homework: '作业', video: '视频', exam: '考试'}[type] || type)
const statusType = (status) => ({'已提交': 'success', '已完成': 'success', '待完成': 'warning', '待观看': 'info', '待考试': 'danger'}[status] || 'info')

const goHomework = (taskId) => router.push(`/portal/student/homework/${taskId}`)
const goExam = (taskId) => router.push(`/portal/student/exam/${taskId}`)
const watchVideo = (row) => request.post('/api/v1/student/video/watch', {videoId: row.taskId, watchTime: 600}).then(()=>{ElMessage.success('视频学习进度已记录');load()})

onMounted(load)
</script>
