<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between"><span>用户管理</span><el-button type="primary" @click="openAdd">新增用户</el-button></div>
    </template>
    <el-table :data="table.records || []" stripe>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="username" label="用户名"/>
      <el-table-column prop="name" label="姓名"/>
      <el-table-column prop="email" label="邮箱"/>
      <el-table-column prop="role" label="角色"/>
      <el-table-column label="风险等级">
        <template #default="scope">
          <span v-if="scope.row.role === 'STUDENT'">{{ scope.row.riskLevel }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button size="small" @click="edit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="visible" title="用户信息" width="460px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="用户名"><el-input v-model="form.username"/></el-form-item>
      <el-form-item v-if="!form.id" label="密码"><el-input v-model="form.password"/></el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.name"/></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email"/></el-form-item>
      <el-form-item label="角色"><el-select v-model="form.role"><el-option value="STUDENT" label="学生"/><el-option value="TEACHER" label="教师"/></el-select></el-form-item>
    </el-form>
    <template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
  </el-dialog>
</template>
<script setup>
import {onMounted, reactive, ref} from 'vue'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
const table = reactive({records:[], total:0})
const visible = ref(false)
const form = reactive({})
const load = () => request.get('/api/v1/admin/user').then(res => Object.assign(table, res.data || {}))
const openAdd = () => { Object.assign(form,{username:'',password:'123456',name:'',email:'',role:'STUDENT'}); visible.value=true }
const edit = (row) => { Object.assign(form, JSON.parse(JSON.stringify(row))); visible.value=true }
const save = () => {
  const api = form.id ? request.put('/api/v1/admin/user', form) : request.post('/api/v1/admin/user', form)
  api.then(()=>{ElMessage.success('保存成功'); visible.value=false; load()})
}
const del = (id) => request.delete('/api/v1/admin/user/'+id).then(()=>{ElMessage.success('删除成功'); load()})
onMounted(load)
</script>
