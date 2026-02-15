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

    <el-dialog
      v-model="showHighRiskAlert"
      class="high-risk-dialog"
      :modal="false"
      width="400px"
      :show-close="true"
      append-to-body
      align-center
      title="高风险连续预警提醒"
    >
      <div class="high-risk-icon">⚠️</div>
      <div class="high-risk-title">你已经连续一周预警等级为 HIGH</div>
      <div class="high-risk-text">请及时完成任务，并尽快联系教师制定学习改进计划。</div>
      <template #footer>
        <el-button @click="showHighRiskAlert = false">我知道了</el-button>
        <el-button type="danger" @click="goTasks">立即处理任务</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showReminder"
      class="reminder-dialog"
      :modal="false"
      width="360px"
      :show-close="true"
      append-to-body
      align-center
      title="学习任务提醒"
    >
      <div class="reminder-title">你还有待完成任务，请尽快处理：</div>
      <ul class="reminder-list">
        <li v-for="(item, idx) in reminderItems" :key="idx">{{ item }}</li>
      </ul>
      <div class="reminder-tip">建议立即前往「任务列表」处理，以免影响风险评估。</div>
      <template #footer>
        <el-button @click="showReminder = false">稍后再看</el-button>
        <el-button type="primary" @click="goTasks">立即前往</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import router from '@/router'
import request from '@/utils/request'

const user = JSON.parse(localStorage.getItem('system-user') || '{}')
const showReminder = ref(false)
const showHighRiskAlert = ref(false)
const reminderItems = ref([])

const formatTaskType = (type) => ({ homework: '作业', video: '视频', exam: '考试' }[type] || '任务')

const buildReminder = (tasks = []) => {
  const now = new Date()
  const pending = tasks.filter(t => String(t.status || '').startsWith('待'))
  const overdue = []
  const notOverdue = []

  pending.forEach(t => {
    const due = t.dueDate ? new Date(t.dueDate) : null
    const day = t.dueDate ? String(t.dueDate).slice(0, 10) : '今日'
    const line = `${day} 的《${t.title}》(${formatTaskType(t.type)})`
    if (due && due < now) {
      overdue.push(`⚠️ 已逾期：${line} 尚未完成`)
    } else {
      notOverdue.push(`⏳ 未完成：${line}，请立即去完成`)
    }
  })

  return [...overdue, ...notOverdue].slice(0, 4)
}

const loadStudentReminder = () => {
  if (user.role !== 'STUDENT') return
  request.get('/api/v1/student/tasks').then(res => {
    const tasks = res.data || []
    const lines = buildReminder(tasks)
    reminderItems.value = lines
    showReminder.value = lines.length > 0
  })
}

const loadHighRiskAlert = () => {
  if (user.role !== 'STUDENT') return
  request.get('/api/v1/student/risk/high-streak').then(res => {
    const high7days = !!res?.data?.high7days
    showHighRiskAlert.value = high7days
  })
}

const goTasks = () => {
  showReminder.value = false
  showHighRiskAlert.value = false
  router.push('/portal/student/tasks')
}

const logout = () => {
  localStorage.removeItem('system-user')
  router.push('/login')
}

onMounted(() => {
  loadHighRiskAlert()
  loadStudentReminder()
})
</script>

<style scoped>
.topbar{height:56px;background:#1f2937;color:#fff;display:flex;align-items:center;justify-content:space-between;padding:0 20px}
.brand{font-size:18px;font-weight:700}
.main{display:flex;min-height:calc(100vh - 56px)}
.sidebar{width:220px;border-right:1px solid #eee;background: #fff}
.content{flex:1;padding:16px;background:#f5f7fb;overflow:auto}
.actions{display:flex;align-items:center}

.high-risk-icon {
  font-size: 30px;
  text-align: center;
  margin-top: -6px;
}
.high-risk-title {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 8px;
}
.high-risk-text {
  color: #606266;
  text-align: center;
  line-height: 1.7;
}

.reminder-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}
.reminder-list {
  margin: 0;
  padding-left: 18px;
  color: #606266;
  line-height: 1.8;
}
.reminder-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

:deep(.high-risk-dialog .el-dialog) {
  position: fixed;
  right: 18px;
  bottom: 206px;
  margin: 0 !important;
  border-radius: 14px;
  border: 1px solid #fde2e2;
  box-shadow: 0 12px 30px rgba(245, 108, 108, 0.2);
}

:deep(.reminder-dialog .el-dialog) {
  position: fixed;
  right: 18px;
  bottom: 18px;
  margin: 0 !important;
  border-radius: 12px;
  box-shadow: 0 10px 26px rgba(31, 41, 55, 0.18);
}
:deep(.reminder-dialog .el-dialog__header) {
  padding-bottom: 6px;
}

@media (max-width: 900px) {
  .main {flex-direction: column}
  .sidebar {width: 100%; border-right: none; border-bottom: 1px solid #eee}
  .content {padding: 10px}
  :deep(.reminder-dialog .el-dialog), :deep(.high-risk-dialog .el-dialog) {
    right: 8px;
    left: 8px;
    width: auto !important;
  }
  :deep(.high-risk-dialog .el-dialog) {
    bottom: 220px;
  }
}
</style>
