<template>
  <div class="login-container">
    <div class="login-box">
      <div class="title">学业预警系统登录</div>
      <el-form :model="form" ref="formRef" :rules="rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item prop="role">
          <el-select size="large" style="width: 100%" v-model="form.role">
            <el-option value="STUDENT" label="学生端"></el-option>
            <el-option value="TEACHER" label="教师端"></el-option>
            <el-option value="ADMIN" label="管理员端"></el-option>
          </el-select>
        </el-form-item>
        <el-button size="large" type="primary" style="width: 100%" @click="login">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import router from "@/router";

const form = reactive({ username: '', password: '', role: 'STUDENT' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择端口角色', trigger: 'change' }],
}
const formRef = ref()

const roleHomeMap = {
  STUDENT: '/portal/student/profile',
  TEACHER: '/portal/teacher/publish',
  ADMIN: '/portal/admin/users'
}

const login = () => {
  formRef.value.validate(valid => {
    if (!valid) return
    request.post('/api/v1/auth/login', form).then(res => {
      if (res.code === 0) {
        localStorage.setItem('system-user', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        router.push(roleHomeMap[res.data.role])
      } else {
        ElMessage.error(res.message || '登录失败')
      }
    })
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(120deg, #1f3b87, #355cce);
}
.login-box {
  width: 380px;
  padding: 36px;
  border-radius: 12px;
  background-color: #fff;
}
.title {
  font-weight: 700;
  font-size: 24px;
  text-align: center;
  margin-bottom: 28px;
  color: #213547;
}
</style>
