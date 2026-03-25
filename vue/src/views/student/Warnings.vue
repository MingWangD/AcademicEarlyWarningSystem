<template>
  <el-space direction="vertical" fill :size="12">
    <el-card>
      <template #header>学业预警信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="当前风险等级">
          <el-tag :type="tagType(info.riskLevel)">{{ info.riskLevel || 'LOW' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前学分">
          {{ Number(info.credit || 0).toFixed(2) }} / {{ Number(credits.totalCredits || 4).toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="已修学分 / 总学分">
          {{ Number(credits.earnedCredits || 0).toFixed(2) }} / {{ Number(credits.totalCredits || 0).toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="本学期学分">
          {{ Number(credits.currentSemesterCredits || 0).toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="挂科学分">
          {{ Number(credits.failedCredits || 0).toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="GPA">
          {{ Number(credits.gpa || 0).toFixed(2) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card>
      <template #header>学分风险分析</template>
      <div class="metric">
        <span>学分完成进度</span>
        <el-progress
          :percentage="Math.round(Number(creditRisk.creditCompletionRate || 0) * 100)"
          :status="progressStatus"
          :stroke-width="18"
        />
      </div>
      <div class="metric">
        <span>挂科学分占比</span>
        <el-progress
          :percentage="Math.round(Number(creditRisk.failedCreditRatio || 0) * 100)"
          :stroke-width="14"
          color="#e6a23c"
        />
      </div>
      <el-alert
        v-if="creditRisk.isAtRisk"
        type="warning"
        show-icon
        :closable="false"
        :title="`⚠ ${creditRisk.riskReason || '学分风险偏高，建议尽快干预'}`"
      />
      <el-alert
        v-else
        type="success"
        show-icon
        :closable="false"
        title="✅ 当前学分风险可控，建议保持学习节奏。"
      />
      <div class="reason">
        <strong>风险原因（可解释性）</strong>
        <ul>
          <li>学分完成率：{{ Number(creditRisk.creditCompletionRate || 0).toFixed(2) }}（建议 ≥ 0.80）</li>
          <li>挂科学分占比：{{ Number(creditRisk.failedCreditRatio || 0).toFixed(2) }}（建议 ≤ 0.30）</li>
          <li>系统将学分与行为特征联合用于风险评估。</li>
        </ul>
      </div>
    </el-card>

    <el-card>
      <template #header>学分趋势（近8次）</template>
      <svg viewBox="0 0 600 200" class="chart" v-if="trend.length > 1">
        <polyline
          fill="none"
          stroke="#409EFF"
          stroke-width="3"
          :points="linePoints"
        />
        <circle
          v-for="(p, idx) in pointList"
          :key="idx"
          :cx="p.x"
          :cy="p.y"
          r="4"
          fill="#409EFF"
        />
      </svg>
      <el-table :data="trend" size="small">
        <el-table-column prop="week" label="周次" width="100"/>
        <el-table-column prop="date" label="日期"/>
        <el-table-column prop="credits" label="累计学分"/>
      </el-table>
    </el-card>
  </el-space>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import request from '@/utils/request'
const info = reactive({})
const credits = reactive({})
const creditRisk = reactive({})
const trend = ref([])
const tagType = (risk) => ({HIGH:'danger',MEDIUM:'warning',LOW:'success'}[risk] || 'info')
const progressStatus = computed(() => Number(creditRisk.creditCompletionRate || 0) < 0.8 ? 'exception' : 'success')

const pointList = computed(() => {
  if (!trend.value.length) return []
  const maxCredit = Math.max(...trend.value.map(i => Number(i.credits || 0)), 1)
  return trend.value.map((item, idx) => {
    const x = 40 + (idx * (520 / Math.max(trend.value.length - 1, 1)))
    const ratio = Number(item.credits || 0) / maxCredit
    const y = 170 - ratio * 140
    return {x, y}
  })
})
const linePoints = computed(() => pointList.value.map(p => `${p.x},${p.y}`).join(' '))

onMounted(async () => {
  const [infoRes, creditRes, riskRes, trendRes] = await Promise.all([
    request.get('/api/v1/student/info'),
    request.get('/api/v1/student/credits'),
    request.get('/api/v1/student/credit-risk'),
    request.get('/api/v1/student/credit-trend')
  ])
  Object.assign(info, infoRes.data || {})
  Object.assign(credits, creditRes.data || {})
  Object.assign(creditRisk, riskRes.data || {})
  trend.value = trendRes.data || []
})
</script>

<style scoped>
.metric { margin-bottom: 12px; }
.reason ul { margin: 8px 0 0 18px; padding: 0; }
.chart { width: 100%; height: 180px; background: #f8fbff; border-radius: 8px; margin-bottom: 12px; }
</style>
