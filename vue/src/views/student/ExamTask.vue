<template>
  <el-card class="paper-card exam-paper">
    <template #header>
      <div class="title-wrap">
        <span class="title">考试答题</span>
        <el-tag type="warning" effect="light">共 {{ questions.length }} 题</el-tag>
      </div>
    </template>

    <div v-for="(q, idx) in questions" :key="q.id" class="question-item">
      <div class="stem">{{ idx + 1 }}. {{ q.stem }}</div>
      <el-radio-group v-model="answers[q.id]" class="options">
        <el-radio label="A">A. {{ q.optionA }}</el-radio>
        <el-radio label="B">B. {{ q.optionB }}</el-radio>
        <el-radio label="C">C. {{ q.optionC }}</el-radio>
        <el-radio label="D">D. {{ q.optionD }}</el-radio>
      </el-radio-group>
    </div>

    <div class="actions">
      <el-button type="warning" size="large" @click="submit">提交考试</el-button>
    </div>
  </el-card>
</template>
<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import request from '@/utils/request'
import {ElMessage} from 'element-plus'
const route = useRoute(); const router = useRouter()
const taskId = Number(route.params.taskId)
const questions = ref([])
const answers = reactive({})
onMounted(()=>request.get(`/api/v1/student/exam/${taskId}/questions`).then(res=>questions.value=res.data||[]))
const submit = () => request.post('/api/v1/student/exam/submit', {taskId, answers}).then(res => {ElMessage.success(`考试提交成功，得分 ${res.data.score}，最新风险等级 ${res.data.riskLevel || 'LOW'}`); router.push('/portal/student/tasks')})
</script>

<style scoped>
.paper-card {
  border-radius: 12px;
}
.exam-paper {
  box-shadow: 0 8px 24px rgba(230, 162, 60, 0.08);
}
.title-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 18px;
  font-weight: 700;
}
.question-item {
  margin-bottom: 16px;
  padding: 14px;
  border: 1px solid #f3e4c3;
  border-radius: 10px;
  background: #fffaf2;
}
.stem {
  font-weight: 600;
  margin-bottom: 10px;
}
.options {
  display: grid;
  gap: 8px;
}
.actions {
  margin-top: 8px;
  text-align: right;
}
</style>
