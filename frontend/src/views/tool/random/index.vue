<script setup>
/**
 * 随机数据工具（REQ-004）：随机数 / 随机字符串 / UUID / 手机号 / 邮箱
 * - UUID 使用 crypto.randomUUID（浏览器原生）
 * - 支持批量生成
 */
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const count = ref(1)
const output = ref('')

/* 随机数 */
const numForm = reactive({ min: 1, max: 100 })

/* 随机字符串 */
const strForm = reactive({
  length: 16,
  digits: true,
  lower: true,
  upper: true,
  special: false,
})

function randInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)]
}

function genNumbers() {
  const min = Number(numForm.min)
  const max = Number(numForm.max)
  if (!Number.isFinite(min) || !Number.isFinite(max) || min > max) {
    ElMessage.error('请填写有效的范围（min ≤ max）')
    return
  }
  output.value = Array.from({ length: count.value }, () => randInt(min, max)).join('\n')
}

function genStrings() {
  const sets = []
  if (strForm.digits) sets.push('0123456789')
  if (strForm.lower) sets.push('abcdefghijklmnopqrstuvwxyz')
  if (strForm.upper) sets.push('ABCDEFGHIJKLMNOPQRSTUVWXYZ')
  if (strForm.special) sets.push('!@#$%^&*()-_=+[]{};:,.<>?')
  if (!sets.length) {
    ElMessage.error('请至少选择一个字符集')
    return
  }
  const pool = sets.join('')
  const len = Number(strForm.length)
  if (!Number.isFinite(len) || len < 1 || len > 1000) {
    ElMessage.error('长度需在 1-1000 之间')
    return
  }
  output.value = Array.from({ length: count.value }, () =>
    Array.from({ length: len }, () => pick(pool)).join(''),
  ).join('\n')
}

function genUuids() {
  if (!crypto.randomUUID) {
    ElMessage.error('当前浏览器不支持 crypto.randomUUID')
    return
  }
  output.value = Array.from({ length: count.value }, () => crypto.randomUUID()).join('\n')
}

function genPhones() {
  const prefixes = ['13', '14', '15', '16', '17', '18', '19']
  output.value = Array.from({ length: count.value }, () => {
    const third = randInt(0, 9)
    const rest = Array.from({ length: 8 }, () => randInt(0, 9)).join('')
    return `${pick(prefixes)}${third}${rest}`
  }).join('\n')
}

function genEmails() {
  const domains = ['example.com', 'test.cn', 'mail.com', 'demo.org', 'qq.com']
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789'
  output.value = Array.from({ length: count.value }, () => {
    const name = Array.from({ length: randInt(6, 12) }, () => pick(chars)).join('')
    return `${name}@${pick(domains)}`
  }).join('\n')
}

async function copyOutput() {
  if (!output.value) {
    ElMessage.warning('暂无生成结果')
    return
  }
  await navigator.clipboard.writeText(output.value)
  ElMessage.success('已复制')
}
</script>

<template>
  <div class="random-tool">
    <div class="row">
      <!-- 左侧：生成选项 -->
      <div class="col">
        <el-card shadow="never" class="section">
          <template #header>
            <div class="section-title">生成选项</div>
          </template>

          <div class="opt-group">
            <div class="opt-label">随机整数</div>
            <div class="opt-row">
              <el-input-number v-model="numForm.min" :min="-999999999" :max="999999999" />
              <span class="sep">~</span>
              <el-input-number v-model="numForm.max" :min="-999999999" :max="999999999" />
              <el-button type="primary" @click="genNumbers">生成</el-button>
            </div>
          </div>

          <el-divider />

          <div class="opt-group">
            <div class="opt-label">随机字符串</div>
            <div class="opt-row">
              <el-input-number v-model="strForm.length" :min="1" :max="1000" />
              <el-button type="primary" @click="genStrings">生成</el-button>
            </div>
            <div class="opt-row">
              <el-checkbox v-model="strForm.digits">数字</el-checkbox>
              <el-checkbox v-model="strForm.lower">小写</el-checkbox>
              <el-checkbox v-model="strForm.upper">大写</el-checkbox>
              <el-checkbox v-model="strForm.special">特殊字符</el-checkbox>
            </div>
          </div>

          <el-divider />

          <div class="opt-group">
            <div class="opt-label">其他类型</div>
            <div class="opt-row">
              <el-button @click="genUuids">UUID</el-button>
              <el-button @click="genPhones">手机号</el-button>
              <el-button @click="genEmails">邮箱</el-button>
            </div>
          </div>

          <el-divider />

          <div class="opt-group">
            <div class="opt-label">批量数量</div>
            <div class="opt-row">
              <el-input-number v-model="count" :min="1" :max="100" />
              <span class="hint">每次生成 N 条（换行分隔）</span>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：结果 -->
      <div class="col">
        <el-card shadow="never" class="section">
          <template #header>
            <div class="section-title">
              生成结果
              <el-button size="small" class="copy-btn" @click="copyOutput">复制</el-button>
            </div>
          </template>
          <el-input
            v-model="output"
            type="textarea"
            :rows="16"
            readonly
            placeholder="点击左侧按钮生成随机数据"
            class="mono"
          />
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.row {
  display: flex;
  gap: 12px;
}

.col {
  flex: 1;
  min-width: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.copy-btn {
  margin-left: auto;
}

.opt-group {
  margin-bottom: 4px;
}

.opt-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.opt-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.sep {
  color: #909399;
}

.hint {
  font-size: 12px;
  color: #909399;
}

.mono :deep(textarea) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
</style>