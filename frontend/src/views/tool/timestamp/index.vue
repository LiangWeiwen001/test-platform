<script setup>
/**
 * 时间戳工具（REQ-004）：时间戳 ↔ 日期互转（秒 / 毫秒）
 * - 当前时间实时刷新（每秒）
 * - 时间戳 → 日期：支持秒 / 毫秒，多格式预览
 * - 日期 → 时间戳：输出秒 / 毫秒
 */
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

/* 当前时间实时刷新 */
const now = ref(new Date())
let timer = null

onMounted(() => {
  timer = setInterval(() => {
    now.value = new Date()
  }, 1000)
})

onBeforeUnmount(() => {
  clearInterval(timer)
})

/* 时间戳 → 日期 */
const tsInput = ref('')
const tsUnit = ref('s') // s | ms
const tsResult = ref([])

function pad(n) {
  return String(n).padStart(2, '0')
}

/** 格式化日期为多种常见格式 */
function formatDate(d) {
  const y = d.getFullYear()
  const m = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const h = pad(d.getHours())
  const min = pad(d.getMinutes())
  const s = pad(d.getSeconds())
  return [
    { label: 'yyyy-MM-dd HH:mm:ss', value: `${y}-${m}-${day} ${h}:${min}:${s}` },
    { label: 'yyyy/MM/dd HH:mm:ss', value: `${y}/${m}/${day} ${h}:${min}:${s}` },
    { label: 'yyyy-MM-dd', value: `${y}-${m}-${day}` },
    { label: 'ISO 8601', value: d.toISOString() },
    { label: 'UTC 字符串', value: d.toUTCString() },
    { label: '本地字符串', value: d.toString() },
  ]
}

function tsToDate() {
  if (!tsInput.value) {
    ElMessage.warning('请输入时间戳')
    return
  }
  const raw = Number(tsInput.value.trim())
  if (!Number.isFinite(raw)) {
    ElMessage.error('时间戳必须是数字')
    return
  }
  const ms = tsUnit.value === 's' ? raw * 1000 : raw
  const d = new Date(ms)
  if (Number.isNaN(d.getTime())) {
    ElMessage.error('时间戳超出有效范围')
    return
  }
  tsResult.value = formatDate(d)
}

/* 日期 → 时间戳 */
const dateInput = ref('')
const dateResult = ref({ s: '', ms: '' })

function dateToTs() {
  if (!dateInput.value) {
    ElMessage.warning('请输入日期时间')
    return
  }
  const d = new Date(dateInput.value)
  if (Number.isNaN(d.getTime())) {
    ElMessage.error('日期格式无效，示例：2026-09-21 10:00:00')
    return
  }
  dateResult.value = {
    s: Math.floor(d.getTime() / 1000),
    ms: d.getTime(),
  }
}

/** 填入当前时间戳 */
function fillNow() {
  tsInput.value = String(Math.floor(Date.now() / 1000))
  tsUnit.value = 's'
  tsToDate()
}

async function copyText(text) {
  if (text === '' || text === null || text === undefined) {
    ElMessage.warning('暂无内容')
    return
  }
  await navigator.clipboard.writeText(String(text))
  ElMessage.success('已复制')
}
</script>

<template>
  <div class="ts-tool">
    <!-- 当前时间 -->
    <el-card shadow="never" class="section">
      <template #header>
        <div class="section-title">当前时间（实时刷新）</div>
      </template>
      <div class="now-box">
        <span class="now-time">{{ now.toLocaleString('zh-CN', { hour12: false }) }}</span>
        <span class="now-ts">
          秒：{{ Math.floor(now.getTime() / 1000) }} ｜ 毫秒：{{ now.getTime() }}
        </span>
        <el-button size="small" @click="fillNow">填入当前时间戳</el-button>
      </div>
    </el-card>

    <!-- 时间戳 → 日期 -->
    <el-card shadow="never" class="section">
      <template #header>
        <div class="section-title">时间戳 → 日期</div>
      </template>
      <div class="form-row">
        <el-input
          v-model="tsInput"
          placeholder="输入时间戳，如 1726905600"
          clearable
          class="mono"
          @keyup.enter="tsToDate"
        />
        <el-select v-model="tsUnit" style="width: 110px">
          <el-option label="秒" value="s" />
          <el-option label="毫秒" value="ms" />
        </el-select>
        <el-button type="primary" @click="tsToDate">转换</el-button>
      </div>
      <el-table v-if="tsResult.length" :data="tsResult" border size="small" class="result-table">
        <el-table-column prop="label" label="格式" width="200" />
        <el-table-column prop="value" label="结果">
          <template #default="{ row }">
            <span class="mono">{{ row.value }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 日期 → 时间戳 -->
    <el-card shadow="never" class="section">
      <template #header>
        <div class="section-title">日期 → 时间戳</div>
      </template>
      <div class="form-row">
        <el-input
          v-model="dateInput"
          placeholder="输入日期时间，如 2026-09-21 10:00:00"
          clearable
          @keyup.enter="dateToTs"
        />
        <el-button type="primary" @click="dateToTs">转换</el-button>
      </div>
      <div v-if="dateResult.s !== ''" class="result-row">
        <div class="result-item">
          <span class="result-label">秒</span>
          <span class="mono">{{ dateResult.s }}</span>
          <el-button size="small" @click="copyText(dateResult.s)">复制</el-button>
        </div>
        <div class="result-item">
          <span class="result-label">毫秒</span>
          <span class="mono">{{ dateResult.ms }}</span>
          <el-button size="small" @click="copyText(dateResult.ms)">复制</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.section {
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
}

.now-box {
  display: flex;
  align-items: center;
  gap: 16px;
}

.now-time {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.now-ts {
  font-size: 13px;
  color: #909399;
}

.form-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.result-table {
  margin-top: 4px;
}

.result-row {
  display: flex;
  gap: 24px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-label {
  font-size: 13px;
  color: #606266;
}

.mono {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
</style>