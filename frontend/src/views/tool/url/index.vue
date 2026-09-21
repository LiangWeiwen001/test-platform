<script setup>
/**
 * URL 工具（REQ-004）：URL 编码 / 解码 + 参数解析
 * - 编码：encodeURIComponent（组件级，可安全用于 query 值）
 * - 解码：decodeURIComponent
 * - 参数解析：URL 对象解析 query，表格展示 key-value，可复制
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

/* 编解码 */
const codeInput = ref('')
const codeOutput = ref('')

function encode() {
  if (!codeInput.value) {
    ElMessage.warning('请输入内容')
    return
  }
  try {
    codeOutput.value = encodeURIComponent(codeInput.value)
  } catch (e) {
    ElMessage.error(`编码失败：${e.message}`)
  }
}

function decode() {
  if (!codeInput.value) {
    ElMessage.warning('请输入内容')
    return
  }
  try {
    codeOutput.value = decodeURIComponent(codeInput.value)
  } catch (e) {
    ElMessage.error(`解码失败：${e.message}`)
  }
}

/* 参数解析 */
const urlInput = ref('')
const params = ref([])
const urlInfo = ref(null)

function parseUrl() {
  if (!urlInput.value) {
    ElMessage.warning('请输入 URL')
    return
  }
  try {
    const u = new URL(urlInput.value)
    urlInfo.value = {
      protocol: u.protocol,
      host: u.host,
      pathname: u.pathname,
      hash: u.hash || '-',
    }
    params.value = [...u.searchParams.entries()].map(([k, v], i) => ({
      id: i + 1,
      key: k,
      value: v,
    }))
    if (!params.value.length) {
      ElMessage.info('该 URL 无 query 参数')
    }
  } catch (e) {
    ElMessage.error(`URL 无效：${e.message}`)
    urlInfo.value = null
    params.value = []
  }
}

/** 复制全部参数为 query 字符串 */
async function copyQuery() {
  if (!params.value.length) {
    ElMessage.warning('暂无参数')
    return
  }
  const qs = params.value.map((p) => `${p.key}=${p.value}`).join('&')
  await navigator.clipboard.writeText(qs)
  ElMessage.success('已复制 query 字符串')
}

async function copyText(text) {
  if (text === undefined || text === null || text === '') {
    ElMessage.warning('暂无内容')
    return
  }
  await navigator.clipboard.writeText(String(text))
  ElMessage.success('已复制')
}

/** 示例填充 */
function fillSample() {
  urlInput.value = 'https://example.com/api/v1/users?page=1&size=20&keyword=%E6%B5%8B%E8%AF%95&status=1'
  parseUrl()
}
</script>

<template>
  <div class="url-tool">
    <!-- 编解码 -->
    <el-card shadow="never" class="section">
      <template #header>
        <div class="section-title">URL 编码 / 解码</div>
      </template>
      <el-input
        v-model="codeInput"
        type="textarea"
        :rows="3"
        placeholder="输入待编码 / 解码的内容，如 https://example.com/?name=张三&age=18"
        clearable
        class="mono"
      />
      <div class="actions">
        <el-button type="primary" @click="encode">编码（encodeURIComponent）</el-button>
        <el-button @click="decode">解码（decodeURIComponent）</el-button>
      </div>
      <div class="output-row">
        <el-input v-model="codeOutput" readonly type="textarea" :rows="3" placeholder="结果" class="mono" />
        <el-button @click="copyText(codeOutput)">复制</el-button>
      </div>
    </el-card>

    <!-- 参数解析 -->
    <el-card shadow="never" class="section">
      <template #header>
        <div class="section-title">URL 参数解析</div>
      </template>
      <div class="form-row">
        <el-input
          v-model="urlInput"
          placeholder="输入完整 URL，如 https://example.com/api?page=1&size=20"
          clearable
          class="mono"
          @keyup.enter="parseUrl"
        />
        <el-button type="primary" @click="parseUrl">解析</el-button>
        <el-button @click="fillSample">示例</el-button>
      </div>

      <el-descriptions v-if="urlInfo" :column="4" border size="small" class="url-info">
        <el-descriptions-item label="协议">{{ urlInfo.protocol }}</el-descriptions-item>
        <el-descriptions-item label="域名">{{ urlInfo.host }}</el-descriptions-item>
        <el-descriptions-item label="路径">{{ urlInfo.pathname }}</el-descriptions-item>
        <el-descriptions-item label="Hash">{{ urlInfo.hash }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="params.length" class="params-area">
        <div class="params-toolbar">
          <span class="params-count">共 {{ params.length }} 个参数</span>
          <el-button size="small" @click="copyQuery">复制 query 字符串</el-button>
        </div>
        <el-table :data="params" border size="small">
          <el-table-column prop="id" label="#" width="50" align="center" />
          <el-table-column prop="key" label="参数名" min-width="140" />
          <el-table-column prop="value" label="参数值" min-width="180" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="copyText(row.value)">
                复制值
              </el-button>
            </template>
          </el-table-column>
        </el-table>
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

.actions {
  margin: 10px 0;
}

.output-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.form-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.url-info {
  margin-bottom: 10px;
}

.params-area {
  margin-top: 4px;
}

.params-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.params-count {
  font-size: 13px;
  color: #606266;
}

.mono :deep(textarea),
.mono :deep(input) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
</style>