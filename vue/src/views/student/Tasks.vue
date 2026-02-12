<template>
  <el-card>
    <template #header>任务列表</template>
    <el-table :data="tasks" stripe>
      <el-table-column prop="id" label="任务ID" width="100"/>
      <el-table-column prop="type" label="类型" width="120"/>
      <el-table-column prop="title" label="标题"/>
      <el-table-column prop="dueDate" label="截止时间" width="200"/>
      <el-table-column label="操作" width="280">
        <template #default="scope">
          <el-button size="small" @click="submitHomework(scope.row)">提交作业</el-button>
          <el-button size="small" @click="watchVideo(scope.row)">观看视频</el-button>
          <el-button size="small" type="primary" @click="submitExam(scope.row)">参加考试</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'

const tasks = ref([])
const load = () => request.get('/api/v1/student/tasks').then(res => tasks.value = res.data || [])
const submitHomework = (row) => request.post('/api/v1/student/homework/submit', {homeworkId: row.id, content: '完成'}).then(()=>ElMessage.success('作业已提交'))
const watchVideo = (row) => request.post('/api/v1/student/video/watch', {videoId: row.id, watchTime: 300}).then(()=>ElMessage.success('观看记录已更新'))
const submitExam = (row) => request.post('/api/v1/student/exam/submit', {examId: row.id, answers: {q1:'A'}}).then(res=>ElMessage.success(`考试提交成功，得分 ${res.data.score}`))
onMounted(load)
</script>
