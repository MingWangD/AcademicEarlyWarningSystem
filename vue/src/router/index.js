import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('@/views/Login.vue') },
    {
      path: '/portal',
      component: () => import('@/views/layout/PortalLayout.vue'),
      redirect: '/portal/student/profile',
      children: [
        { path: 'student/profile', component: () => import('@/views/student/Profile.vue') },
        { path: 'student/tasks', component: () => import('@/views/student/Tasks.vue') },
        { path: 'student/warnings', component: () => import('@/views/student/Warnings.vue') },
        { path: 'student/homework/:taskId', component: () => import('@/views/student/HomeworkTask.vue') },
        { path: 'student/exam/:taskId', component: () => import('@/views/student/ExamTask.vue') },

        { path: 'teacher/publish', component: () => import('@/views/teacher/PublishTask.vue') },
        { path: 'teacher/activity', component: () => import('@/views/teacher/ActivitySummary.vue') },
        { path: 'teacher/risk', component: () => import('@/views/teacher/RiskCalc.vue') },
        { path: 'teacher/dashboard', component: () => import('@/views/teacher/Dashboard.vue') },

        { path: 'admin/users', component: () => import('@/views/admin/UserManage.vue') },
        { path: 'admin/dashboard', component: () => import('@/views/admin/GlobalDashboard.vue') },
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  if (to.path === '/login') return next()
  const user = JSON.parse(localStorage.getItem('system-user') || '{}')
  if (!user?.token) return next('/login')

  if (to.path.includes('/student/') && user.role !== 'STUDENT') return next('/login')
  if (to.path.includes('/teacher/') && user.role !== 'TEACHER') return next('/login')
  if (to.path.includes('/admin/') && user.role !== 'ADMIN') return next('/login')

  next()
})

export default router
