<template>
  <el-row :gutter="12" class="card-row">
    <el-col :xs="24" :sm="8"><el-card><template #header>高风险学生</template><div class="num high">{{ summary.highCount || 0 }}</div></el-card></el-col>
    <el-col :xs="24" :sm="8"><el-card><template #header>中风险学生</template><div class="num med">{{ summary.mediumCount || 0 }}</div></el-card></el-col>
    <el-col :xs="24" :sm="8"><el-card><template #header>低风险学生</template><div class="num low">{{ summary.lowCount || 0 }}</div></el-card></el-col>
  </el-row>

  <el-card style="margin-top:12px">
    <template #header>风险分布</template>
    <el-table :data="normalizedDistribution">
      <el-table-column prop="label" label="等级"/>
      <el-table-column prop="count" label="人数"/>
    </el-table>
  </el-card>

  <el-card style="margin-top:12px">
    <template #header>风险学生明细（按学号升序）</template>
    <el-collapse accordion>
      <el-collapse-item :title="`高风险学生（${highRiskStudentsSorted.length}）`" name="high">
        <el-table :data="highRiskStudentsSorted" size="small">
          <el-table-column prop="id" label="学号" width="120"/>
          <el-table-column prop="name" label="姓名"/>
          <el-table-column label="等级" width="120"><template #default>HIGH</template></el-table-column>
        </el-table>
      </el-collapse-item>
      <el-collapse-item :title="`中风险学生（${mediumRiskStudentsSorted.length}）`" name="medium">
        <el-table :data="mediumRiskStudentsSorted" size="small">
          <el-table-column prop="id" label="学号" width="120"/>
          <el-table-column prop="name" label="姓名"/>
          <el-table-column label="等级" width="120"><template #default>MEDIUM</template></el-table-column>
        </el-table>
      </el-collapse-item>
      <el-collapse-item :title="`低风险学生（${lowRiskStudentsSorted.length}）`" name="low">
        <el-table :data="lowRiskStudentsSorted" size="small">
          <el-table-column prop="id" label="学号" width="120"/>
          <el-table-column prop="name" label="姓名"/>
          <el-table-column label="等级" width="120"><template #default>LOW</template></el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </el-card>

  <el-card style="margin-top:12px">
    <template #header>最近预警（近7天，最近风险计算日期：{{ summary.refreshedAt || '-' }}）</template>
    <el-table :data="pagedRecentWarnings">
      <el-table-column prop="studentId" label="学号"/>
      <el-table-column prop="riskLevel" label="等级"/>
      <el-table-column prop="riskScore" label="风险分"/>
      <el-table-column prop="calcDate" label="日期"/>
    </el-table>
    <div class="pager-wrap">
      <el-pagination
        layout="total, prev, pager, next"
        :total="recentWarningsSorted.length"
        :page-size="pageSize"
        v-model:current-page="currentPage"
      />
    </div>
  </el-card>
</template>
<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import request from '@/utils/request'

const data = reactive({})
const summary = computed(() => data.summary || {})

const pageSize = 10
const currentPage = ref(1)

const sortById = (arr = []) => [...arr].sort((a, b) => Number(a.id || a.studentId || 0) - Number(b.id || b.studentId || 0))

const highRiskStudentsSorted = computed(() => sortById(data.highRiskStudents || []))
const mediumRiskStudentsSorted = computed(() => sortById(data.mediumRiskStudents || []))
const lowRiskStudentsSorted = computed(() => sortById(data.lowRiskStudents || []))
const recentWarningsSorted = computed(() => {
  return [...(data.recentWarnings || [])].sort((a, b) => {
    const da = String(a.calcDate || '')
    const db = String(b.calcDate || '')
    if (da !== db) return db.localeCompare(da)
    return Number(a.studentId || 0) - Number(b.studentId || 0)
  })
})

const pagedRecentWarnings = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return recentWarningsSorted.value.slice(start, start + pageSize)
})

const normalizedDistribution = computed(() => {
  const src = data.riskDistribution || []
  const map = Object.fromEntries(src.map(item => [item.riskLevel, Number(item.count || 0)]))
  return [
    { riskLevel: 'HIGH', label: '高风险', count: map.HIGH || 0 },
    { riskLevel: 'MEDIUM', label: '中风险', count: map.MEDIUM || 0 },
    { riskLevel: 'LOW', label: '低风险', count: map.LOW || 0 }
  ]
})

onMounted(() => request.get('/api/v1/teacher/dashboard').then(res => Object.assign(data, res.data || {})))
</script>
<style scoped>
.num{font-size: 30px;font-weight:700}
.high{color:#d03050}.med{color:#e6a23c}.low{color:#67c23a}
.card-row :deep(.el-card){margin-bottom:12px}
.pager-wrap { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
