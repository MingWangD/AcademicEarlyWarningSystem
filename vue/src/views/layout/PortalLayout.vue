<template>
  <div>
    <div class="topbar">
      <div class="brand">学业预警系统</div>
      <div class="actions">
        <el-tag type="primary" style="margin-right: 12px">{{ user.role }}</el-tag>
        <el-button link @click="logout">退出登录</el-button>
      </div>
    </div>
    <div class="main">
      <div class="sidebar">
        <el-menu router :default-active="router.currentRoute.value.path">
          <template v-if="user.role === 'STUDENT'">
            <el-menu-item index="/portal/student/profile">个人信息</el-menu-item>
            <el-menu-item index="/portal/student/tasks">任务列表</el-menu-item>
            <el-menu-item index="/portal/student/warnings">学业预警</el-menu-item>
          </template>
          <template v-if="user.role === 'TEACHER'">
            <el-menu-item index="/portal/teacher/publish">发布任务</el-menu-item>
            <el-menu-item index="/portal/teacher/activity">行为汇总</el-menu-item>
            <el-menu-item index="/portal/teacher/risk">触发风险计算</el-menu-item>
            <el-menu-item index="/portal/teacher/dashboard">预警看板</el-menu-item>
          </template>
          <template v-if="user.role === 'ADMIN'">
            <el-menu-item index="/portal/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/portal/admin/dashboard">全局看板</el-menu-item>
          </template>
        </el-menu>
      </div>
      <div class="content"><router-view/></div>
    </div>
  </div>
</template>

<script setup>
import router from '@/router'
const user = JSON.parse(localStorage.getItem('system-user') || '{}')
const logout = () => {
  localStorage.removeItem('system-user')
  router.push('/login')
}
</script>

<style scoped>
.topbar{height:56px;background:#1f2937;color:#fff;display:flex;align-items:center;justify-content:space-between;padding:0 20px}
.brand{font-size:18px;font-weight:700}
.main{display:flex;min-height:calc(100vh - 56px)}
.sidebar{width:220px;border-right:1px solid #eee;background: #fff}
.content{flex:1;padding:16px;background:#f5f7fb;overflow:auto}
.actions{display:flex;align-items:center}

@media (max-width: 900px) {
  .main {flex-direction: column}
  .sidebar {width: 100%; border-right: none; border-bottom: 1px solid #eee}
  .content {padding: 10px}
}
</style>
