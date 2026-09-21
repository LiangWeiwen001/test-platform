<script setup>
/**
 * 正则测试工具（REQ-004）：输入正则 + 测试文本，实时高亮匹配
 * - 显示匹配数量、每个匹配的起止位置与捕获分组
 * - 无效正则给出错误提示
 */
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

const pattern = ref('')
const flags = ref('g')
const testText = ref('')

/** 转义 HTML，防止 v-html 注入 */
function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

/** 构建正则（自动补 g 以便 matchAll） */
function buildRegex() {
  if (!pattern.value) return null
  let f = flags.value
  if (!f.includes('g')) f += 'g'
  try {
    return new RegExp(pattern.value, f)
  } catch (e) {
    return { error: e.message }
  }
}

const result = computed(() => {
  if (!pattern.value || !testText.value) return null
  const re = buildRegex()
  if (!re || re.error) return null

  const matches = [...testText.value.matchAll(re)]
  if (!matches.length) return { count: 0, html: escapeHtml(testText.value), details: [] }

  // 构建高亮 HTML
  let html = ''
  let last = 0
  for (const m of matches) {
    html += escapeHtml(testText.value.slice(last, m.index))
    html += `<mark>${escapeHtml(m[0])}</mark>`
    last = m.index + m[0].length
  }
  html += escapeHtml(testText.value.slice(last))

  // 匹配详情（含分组）
  const details = matches.map((m, i) => ({
    index: i + 1,
    start: m.index,
    end: m.index + m[0].length,
    text: m[0],
    groups: m.slice(1).map((g, gi) => (g === undefined ? '' : g)),
  }))

  return { count: matches.length, html, details }
})

const errorMsg = computed(() => {
  if (!pattern.value) return ''
  const re = buildRegex()
  return re && re.error ? re.error : ''
})

/** 示例填充 */
function fillSample() {
  pattern.value = '(\\d{4})-(\\d{2})-(\\d{2})'
  flags.value = 'g'
  testText.value = '日期：2026-09-21 与 2026-10-01，还有 2026-12-31。'
}
</script>

<template>
  <div class="regex-tool">
    <div class="form-row">
      <el-input
        v-model="pattern"
        placeholder="正则表达式，如 (\d{4})-(\d{2})-(\d{2})"
        clearable
        class="mono"
      />
      <el-input v-model="flags" placeholder="flags" style="width: 90px" class="mono" />
      <el-button @click="fillSample">示例</el-button>
    </div>

    <el-alert
      v-if="errorMsg"
      type="error"
      :closable="false"
      :title="`正则无效：${errorMsg}`"
      class="err-alert"
    />

    <el-input
      v-model="testText"
      type="textarea"
      :rows="8"
      placeholder="输入测试文本"
      class="mono"
    />

    <div v-if="result" class="result-area">
      <div class="result-stat">
        匹配数量：<el-tag type="success" size="small">{{ result.count }}</el-tag>
      </div>
      <div class="highlight-box mono" v-html="result.html" />
      <el-table
        v-if="result.details.length"
        :data="result.details"
        border
        size="small"
        class="detail-table"
      >
        <el-table-column prop="index" label="#" width="50" align="center" />
        <el-table-column prop="start" label="起始" width="70" align="center" />
        <el-table-column prop="end" label="结束" width="70" align="center" />
        <el-table-column prop="text" label="匹配内容" min-width="140" />
        <el-table-column label="捕获分组" min-width="160">
          <template #default="{ row }">
            <el-tag
              v-for="(g, i) in row.groups"
              :key="i"
              size="small"
              type="info"
              class="group-tag"
            >
              ${{ i + 1 }}: {{ g }}
            </el-tag>
            <span v-if="!row.groups.length">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.form-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.err-alert {
  margin-bottom: 10px;
}

.result-area {
  margin-top: 12px;
}

.result-stat {
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.highlight-box {
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #fff;
  white-space: pre-wrap;
  word-break: break-all;
  min-height: 60px;
  max-height: 240px;
  overflow: auto;
}

.highlight-box :deep(mark) {
  background: #fde68a;
  color: #b45309;
  border-radius: 2px;
  padding: 0 1px;
}

.detail-table {
  margin-top: 10px;
}

.group-tag {
  margin-right: 4px;
}

.mono :deep(textarea),
.mono :deep(input) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
</style>