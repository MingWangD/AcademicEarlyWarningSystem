<template>
  <el-card>
    <template #header>发布任务</template>
    <el-form :model="form" label-width="100px" style="max-width: 600px">
      <el-form-item label="课程ID"><el-input-number v-model="form.courseId" :min="1"/></el-form-item>
      <el-form-item label="任务类型">
        <el-select v-model="form.type" style="width:100%">
          <el-option label="作业" value="homework"/>
          <el-option label="视频" value="video"/>
          <el-option label="考试" value="exam"/>
        </el-select>
      </el-form-item>
      <el-form-item label="标题"><el-input v-model="form.title"/></el-form-item>
      <el-form-item label="详情"><el-input type="textarea" v-model="form.details"/></el-form-item>
      <el-form-item label="截止时间"><el-date-picker v-model="form.dueDate" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss"/></el-form-item>
      <el-button type="primary" @click="submit">发布任务</el-button>
    </el-form>
  </el-card>
</template>
<script setup>
import {reactive} from 'vue'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
const form = reactive({courseId:1, type:'homework', title:'', details:'', dueDate:''})
const submit = () => request.post('/api/v1/teacher/task/create', form).then(res => ElMessage.success('发布成功，任务ID: '+res.data.taskId))
</script>
