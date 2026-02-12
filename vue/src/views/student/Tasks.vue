<template>
  <el-card>
    <template #header>任务列表（教师发布）</template>

    <el-table :data="tasks" stripe>
      <el-table-column prop="taskId" label="任务ID" width="90" />

      <el-table-column prop="type" label="类型" width="110">
        <template #default="scope">
          <el-tag>{{ typeLabel(scope.row.type) }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="details" label="任务详情" min-width="220" show-overflow-tooltip />
      <el-table-column prop="dueDate" label="截止时间" width="180" />

      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button
              v-if="scope.row.type === 'homework'"
              size="small"
              type="primary"
              :disabled="scope.row.status === '已提交'"
              @click="submitHomework(scope.row)"
          >
            提交作业
          </el-button>

          <el-button
              v-else-if="scope.row.type === 'video'"
              size="small"
              type="success"
              :disabled="scope.row.status === '已完成'"
              @click="watchVideo(scope.row)"
          >
            记录观看
          </el-button>

          <el-button
              v-else-if="scope.row.type === 'exam'"
              size="small"
              type="warning"
              :disabled="scope.row.status === '已完成'"
              @click="submitExam(scope.row)"
          >
            提交考试
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue"
import request from "@/utils/request"
import { ElMessage } from "element-plus"

const tasks = ref([])

const load = async () => {
  const res = await request.get("/api/v1/student/tasks")
  tasks.value = res.data || []
}

const typeLabel = (type) => ({ homework: "作业", video: "视频", exam: "考试" }[type] || type)

const statusType = (status) =>
    ({ 已提交: "success", 已完成: "success", 待完成: "warning", 待观看: "info", 待考试: "danger" }[status] || "info")

const submitHomework = async (row) => {
  await request.post("/api/v1/student/homework/submit", {
    homeworkId: row.taskId,
    content: "已按要求完成"
  })
  ElMessage.success("作业已提交")
  await load()
}

const watchVideo = async (row) => {
  await request.post("/api/v1/student/video/watch", {
    videoId: row.taskId,
    watchTime: 600
  })
  ElMessage.success("视频学习进度已记录")
  await load()
}

const submitExam = async (row) => {
  const res = await request.post("/api/v1/student/exam/submit", {
    examId: row.taskId,
    answers: { q1: "A", q2: "C" }
  })
  // 注意：你的后端统一契约是 code/message/data，所以 score 在 res.data.score 还是 res.data.data.score 取决于 request 封装
  // 如果你 request 已经做了 data 解包，那么下面这句 OK；否则改成 res.data.data.score
  ElMessage.success(`考试提交成功，得分 ${res.data.data.score ?? res.data?.data?.score ?? "-"}`)
  await load()
}

onMounted(load)
</script>
