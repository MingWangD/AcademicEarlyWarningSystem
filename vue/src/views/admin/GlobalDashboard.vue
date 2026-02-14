<template>
  <el-row :gutter="12">
    <el-col :xs="24" :sm="8">
      <el-card>
        <template #header>学生总人数</template>
        <div class="big">{{ data.totalStudents || 0 }}</div>
      </el-card>
    </el-col>
    <el-col :xs="24" :sm="16">
      <el-card>
        <template #header>风险分布（饼图）</template>
        <div class="pie-wrap">
          <div class="pie" :style="pieStyle"></div>
          <div class="legend">
            <div class="legend-item" v-for="item in normalizedRiskDistribution" :key="item.riskLevel">
              <span class="dot" :style="{ backgroundColor: colorMap[item.riskLevel] || '#909399' }"></span>
              <span>{{ riskLabel(item.riskLevel) }}：{{ item.count }}人（{{ item.percent.toFixed(1) }}%）</span>
            </div>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>

  <el-card style="margin-top:12px">
    <template #header>风险趋势（柱状图）｜最近风险计算日期：{{ data.refreshedAt || '-' }}</template>
    <div class="bar-chart">
      <div class="bar-item" v-for="row in data.riskTrend || []" :key="row.date">
        <div class="bar" :style="{ height: `${Math.max(6, Number(row.avgRiskScore || 0) * 100)}%` }"></div>
        <div class="bar-label">{{ row.date }}</div>
        <div class="bar-value">{{ Number(row.avgRiskScore || 0).toFixed(2) }}</div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import {computed, reactive, onMounted} from 'vue'
import request from '@/utils/request'

const data = reactive({
  riskDistribution: [],
  riskTrend: []
})

const colorMap = {
  HIGH: '#f56c6c',
  MEDIUM: '#e6a23c',
  LOW: '#67c23a'
}

const normalizedRiskDistribution = computed(() => {
  const sourceMap = Object.fromEntries((data.riskDistribution || []).map(item => [item.riskLevel, Number(item.count || 0)]))
  const items = [
    { riskLevel: 'HIGH', count: sourceMap.HIGH || 0 },
    { riskLevel: 'MEDIUM', count: sourceMap.MEDIUM || 0 },
    { riskLevel: 'LOW', count: sourceMap.LOW || 0 }
  ]
  const total = items.reduce((sum, item) => sum + item.count, 0)
  return items.map(item => ({
    ...item,
    percent: total === 0 ? 0 : (item.count * 100) / total
  }))
})

const totalRiskCount = computed(() => normalizedRiskDistribution.value.reduce((sum, item) => sum + Number(item.count || 0), 0))

const pieStyle = computed(() => {
  if (!normalizedRiskDistribution.value.length || totalRiskCount.value === 0) {
    return { background: '#e5e7eb' }
  }
  let start = 0
  const sections = normalizedRiskDistribution.value.map(item => {
    const end = start + item.percent
    const color = colorMap[item.riskLevel] || '#909399'
    const section = `${color} ${start}% ${end}%`
    start = end
    return section
  })
  return { background: `conic-gradient(${sections.join(',')})` }
})

const riskLabel = (level) => {
  if (level === 'HIGH') return '高风险'
  if (level === 'MEDIUM') return '中风险'
  if (level === 'LOW') return '低风险'
  return level
}

onMounted(() => request.get('/api/v1/admin/dashboard').then(res => Object.assign(data, res.data || {})))
</script>

<style scoped>
.big { font-size: 32px; font-weight: 700; color: #409eff; }
.pie-wrap { display: flex; gap: 20px; align-items: center; flex-wrap: wrap; }
.pie {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  box-shadow: inset 0 0 0 1px #e5e7eb;
}
.legend { display: flex; flex-direction: column; gap: 8px; }
.legend-item { display: flex; align-items: center; gap: 8px; color: #606266; }
.dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 220px;
  padding: 12px 4px 0;
  border-bottom: 1px solid #ebeef5;
}
.bar-item { width: 64px; text-align: center; }
.bar {
  width: 100%;
  background: linear-gradient(180deg, #79bbff 0%, #409eff 100%);
  border-radius: 8px 8px 0 0;
  min-height: 6px;
}
.bar-label { margin-top: 8px; font-size: 12px; color: #606266; }
.bar-value { font-size: 12px; color: #909399; }
</style>
