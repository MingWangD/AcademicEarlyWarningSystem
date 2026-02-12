<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>学生行为汇总</span>
        <el-date-picker v-model="date" value-format="YYYY-MM-DD" @change="load"/>
      </div>
    </template>
    <el-table :data="rows" stripe>
      <el-table-column prop="studentId" label="学号"/>
      <el-table-column prop="studentName" label="姓名"/>
      <el-table-column prop="loginCount" label="登录次数"/>
      <el-table-column prop="videoMinutes" label="视频时长(分钟)"/>
      <el-table-column prop="homeworkSubmitted" label="作业提交数"/>
      <el-table-column prop="avgScore" label="平均分"/>
    </el-table>
  </el-card>
</template>
<script setup>
import {ref,onMounted} from 'vue'
import request from '@/utils/request'
const date = ref('')
const rows = ref([])
const load = () => request.get('/api/v1/teacher/student/activity', {params: {date: date.value}}).then(res => rows.value = res.data || [])
onMounted(load)
</script>
