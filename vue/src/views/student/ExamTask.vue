<template>
  <el-card>
    <template #header>考试答题（10道选择题）</template>
    <div v-for="(q, idx) in questions" :key="q.id" style="margin-bottom:16px">
      <div style="font-weight:600">{{ idx + 1 }}. {{ q.stem }}</div>
      <el-radio-group v-model="answers[q.id]">
        <el-radio label="A">A. {{ q.optionA }}</el-radio>
        <el-radio label="B">B. {{ q.optionB }}</el-radio>
        <el-radio label="C">C. {{ q.optionC }}</el-radio>
        <el-radio label="D">D. {{ q.optionD }}</el-radio>
      </el-radio-group>
    </div>
    <el-button type="warning" @click="submit">提交考试</el-button>
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
