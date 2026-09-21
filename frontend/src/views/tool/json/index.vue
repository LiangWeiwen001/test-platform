<script setup>
/**
 * JSON 工具（REQ-004）：格式化 / 压缩 / 校验 / 排序键
 * 全部浏览器端实现，JSON.parse 失败时给出具体错误提示
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const input = ref('')
const output = ref('')

/** 解析输入 JSON，失败时提示并返回 null */
function parseInput() {
  if (!input.value.trim()) {
    ElMessage.warning('请输入 JSON 内容')
    return null
  }
  try {
    return JSON.parse(input.value)
  } catch (e) {
    ElMessage.error(`JSON 解析失败：${e.message}`)
    return null
  }
}

/** 格式化：2 空格缩进 */
function format() {
  const obj = parseInput()
  if (obj === null) return
  output.value = JSON.stringify(obj, null, 2)
}

/** 压缩：去除空白 */
function compress() {
  const obj = parseInput()
  if (obj === null) return
  output.value = JSON.stringify(obj)
}

/** 校验：仅解析并提示结果 */
function validate() {
  const obj = parseInput()
  if (obj === null) return
  ElMessage.success('JSON 合法')
  output.value = input.value
}

/** 递归排序对象键（数组元素逐个处理） */
function sortKeys(value) {
  if (Array.isArray(value)) return value.map(sortKeys)
  if (value && typeof value === 'object') {
    return Object.keys(value)
      .sort()
      .reduce((acc, k) => {
        acc[k] = sortKeys(value[k])
        return acc
      }, {})
  }
  return value
}

/** 排序键：按键名升序重排后格式化 */
function sortKey() {
  const obj = parseInput()
  if (obj === null) return
  output.value = JSON.stringify(sortKeys(obj), null, 2)
}

/** 复制输出 */
async function copyOutput() {
  if (!output.value) {
    ElMessage.warning('暂无输出内容')
    return
  }
  await navigator.clipboard.writeText(output.value)
  ElMessage.success('已复制')
}

/** 清空 */
function clearAll() {
  input.value = ''
  output.value = ''
}
</script>

<template>
  <div class="json-tool">
    <div class="row">
      <div class="col">
        <div class="col-label">输入 JSON</div>
        <el-input
          v-model="input"
          type="textarea"
          :rows="12"
          placeholder='{"name":"测试平台","version":"1.0","items":[1,2,3]}'
          class="mono"
        />
      </div>
      <div class="col">
        <div class="col-label">输出结果</div>
        <el-input v-model="output" type="textarea" :rows="12" readonly class="mono" />
      </div>
    </div>

    <div class="actions">
      <el-button type="primary" @click="format">格式化</el-button>
      <el-button @click="compress">压缩</el-button>
      <el-button @click="validate">校验</el-button>
      <el-button @click="sortKey">排序键</el-button>
      <el-button @click="copyOutput">复制结果</el-button>
      <el-button @click="clearAll">清空</el-button>
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

.col-label {
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}

.actions {
  margin-top: 12px;
}

.mono :deep(textarea) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
</style>