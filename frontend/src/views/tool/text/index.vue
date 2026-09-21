<script setup>
/**
 * 文本工具（REQ-004）：文本对比 / 大小写转换 / 去重排序 / 统计
 * - 文本对比：逐行 diff（LCS），相同 / 新增 / 删除 三色高亮
 * - 大小写转换：大写 / 小写 / 单词首字母大写
 * - 去重排序：按行去重 + 升序 / 降序
 * - 统计：字符数 / 行数 / 单词数
 */
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('diff')

/* ---------------- 文本对比 ---------------- */
const diffA = ref('')
const diffB = ref('')

/** 逐行 diff（LCS 动态规划，O(n*m)） */
function diffLines(a, b) {
  const aLines = a.split('\n')
  const bLines = b.split('\n')
  const n = aLines.length
  const m = bLines.length
  const dp = Array.from({ length: n + 1 }, () => new Array(m + 1).fill(0))
  for (let i = n - 1; i >= 0; i--) {
    for (let j = m - 1; j >= 0; j--) {
      dp[i][j] = aLines[i] === bLines[j] ? dp[i + 1][j + 1] + 1 : Math.max(dp[i + 1][j], dp[i][j + 1])
    }
  }
  const result = []
  let i = 0
  let j = 0
  while (i < n && j < m) {
    if (aLines[i] === bLines[j]) {
      result.push({ type: 'same', text: aLines[i] })
      i++
      j++
    } else if (dp[i + 1][j] >= dp[i][j + 1]) {
      result.push({ type: 'del', text: aLines[i] })
      i++
    } else {
      result.push({ type: 'add', text: bLines[j] })
      j++
    }
  }
  while (i < n) result.push({ type: 'del', text: aLines[i++] })
  while (j < m) result.push({ type: 'add', text: bLines[j++] })
  return result
}

const diffResult = computed(() => {
  if (!diffA.value && !diffB.value) return []
  return diffLines(diffA.value, diffB.value)
})

const diffStat = computed(() => {
  const r = diffResult.value
  return {
    same: r.filter((x) => x.type === 'same').length,
    add: r.filter((x) => x.type === 'add').length,
    del: r.filter((x) => x.type === 'del').length,
  }
})

/* ---------------- 大小写转换 ---------------- */
const caseInput = ref('')
const caseOutput = ref('')

function toUpper() {
  caseOutput.value = caseInput.value.toUpperCase()
}

function toLower() {
  caseOutput.value = caseInput.value.toLowerCase()
}

function toTitleCase() {
  caseOutput.value = caseInput.value.replace(/\b\w/g, (c) => c.toUpperCase())
}

/* ---------------- 去重排序 ---------------- */
const dedupInput = ref('')
const dedupOrder = ref('asc')
const dedupOutput = ref('')

function dedupSort() {
  if (!dedupInput.value) {
    ElMessage.warning('请输入文本')
    return
  }
  const lines = [...new Set(dedupInput.value.split('\n').filter((l) => l !== ''))]
  lines.sort((a, b) => (dedupOrder.value === 'asc' ? a.localeCompare(b) : b.localeCompare(a)))
  dedupOutput.value = lines.join('\n')
}

/* ---------------- 统计 ---------------- */
const statInput = ref('')

const stats = computed(() => {
  const t = statInput.value
  const lines = t.split('\n')
  return [
    { label: '总字符数（含空格）', value: t.length },
    { label: '字符数（不含空白）', value: t.replace(/\s/g, '').length },
    { label: '行数', value: lines.length },
    { label: '非空行数', value: lines.filter((l) => l.trim() !== '').length },
    { label: '单词数', value: t.trim() ? t.trim().split(/\s+/).length : 0 },
  ]
})

async function copyText(text) {
  if (!text) {
    ElMessage.warning('暂无输出内容')
    return
  }
  await navigator.clipboard.writeText(text)
  ElMessage.success('已复制')
}
</script>

<template>
  <div class="text-tool">
    <el-tabs v-model="activeTab">
      <!-- 文本对比 -->
      <el-tab-pane label="文本对比" name="diff">
        <div class="row">
          <div class="col">
            <div class="col-label">原文（A）</div>
            <el-input v-model="diffA" type="textarea" :rows="10" placeholder="粘贴原文" class="mono" />
          </div>
          <div class="col">
            <div class="col-label">对比文本（B）</div>
            <el-input v-model="diffB" type="textarea" :rows="10" placeholder="粘贴对比文本" class="mono" />
          </div>
        </div>
        <div v-if="diffResult.length" class="diff-area">
          <div class="diff-stat">
            <el-tag size="small" type="success">相同 {{ diffStat.same }}</el-tag>
            <el-tag size="small" type="danger">删除 {{ diffStat.del }}</el-tag>
            <el-tag size="small" type="warning">新增 {{ diffStat.add }}</el-tag>
          </div>
          <div class="diff-box mono">
            <div
              v-for="(line, i) in diffResult"
              :key="i"
              class="diff-line"
              :class="`diff-${line.type}`"
            >
              <span class="diff-mark">{{ line.type === 'same' ? ' ' : line.type === 'del' ? '-' : '+' }}</span>
              <span class="diff-text">{{ line.text || ' ' }}</span>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 大小写转换 -->
      <el-tab-pane label="大小写转换" name="case">
        <el-input
          v-model="caseInput"
          type="textarea"
          :rows="6"
          placeholder="输入文本"
          class="mono"
        />
        <div class="actions">
          <el-button type="primary" @click="toUpper">转大写</el-button>
          <el-button @click="toLower">转小写</el-button>
          <el-button @click="toTitleCase">单词首字母大写</el-button>
        </div>
        <div class="output-row">
          <el-input v-model="caseOutput" readonly type="textarea" :rows="6" placeholder="结果" class="mono" />
          <el-button @click="copyText(caseOutput)">复制</el-button>
        </div>
      </el-tab-pane>

      <!-- 去重排序 -->
      <el-tab-pane label="去重排序" name="dedup">
        <el-input
          v-model="dedupInput"
          type="textarea"
          :rows="6"
          placeholder="每行一条数据，如：&#10;banana&#10;apple&#10;banana"
          class="mono"
        />
        <div class="actions">
          <el-select v-model="dedupOrder" style="width: 120px">
            <el-option label="升序" value="asc" />
            <el-option label="降序" value="desc" />
          </el-select>
          <el-button type="primary" @click="dedupSort">去重并排序</el-button>
        </div>
        <div class="output-row">
          <el-input v-model="dedupOutput" readonly type="textarea" :rows="6" placeholder="结果" class="mono" />
          <el-button @click="copyText(dedupOutput)">复制</el-button>
        </div>
      </el-tab-pane>

      <!-- 统计 -->
      <el-tab-pane label="统计" name="stat">
        <el-input
          v-model="statInput"
          type="textarea"
          :rows="8"
          placeholder="输入文本，实时统计"
          class="mono"
        />
        <el-descriptions :column="3" border class="stat-box">
          <el-descriptions-item v-for="s in stats" :key="s.label" :label="s.label">
            <span class="stat-value">{{ s.value }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
    </el-tabs>
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
  margin: 10px 0;
  display: flex;
  gap: 8px;
}

.output-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.diff-area {
  margin-top: 12px;
}

.diff-stat {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.diff-box {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  max-height: 320px;
  overflow: auto;
  font-size: 13px;
}

.diff-line {
  display: flex;
  padding: 2px 8px;
  white-space: pre-wrap;
  word-break: break-all;
}

.diff-mark {
  width: 16px;
  flex-shrink: 0;
  color: #909399;
  user-select: none;
}

.diff-same {
  background: #fff;
}

.diff-add {
  background: #f0f9eb;
}

.diff-del {
  background: #fef0f0;
}

.stat-box {
  margin-top: 12px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.mono :deep(textarea),
.mono :deep(input) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
</style>