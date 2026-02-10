<template>
  <el-card>
    <template #header>个人信息</template>
    <el-form :model="form" label-width="100px" style="max-width: 520px">
      <el-form-item label="学号">
        <el-input v-model="form.username" disabled/>
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="form.name"/>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email"/>
      </el-form-item>
      <el-form-item label="风险等级">
        <el-tag :type="tagType(form.riskLevel)">{{ form.riskLevel || 'LOW' }}</el-tag>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存信息</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import {reactive, onMounted} from 'vue'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'

const form = reactive({})
const load = () => request.get('/api/v1/student/info').then(res => Object.assign(form, res.data || {}))
const save = () => request.put('/api/v1/admin/user', form).then(() => ElMessage.success('已保存（演示接口）'))
const tagType = (risk) => ({HIGH:'danger',MEDIUM:'warning',LOW:'success'}[risk] || 'info')
onMounted(load)
</script>
