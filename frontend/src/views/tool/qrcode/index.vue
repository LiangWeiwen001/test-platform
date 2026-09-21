<script setup>
/**
 * 二维码工具（REQ-004）：文本 / URL 转二维码
 * - 使用 qrcode npm 包（前端仅依赖，QRCode.toCanvas）
 * - 支持尺寸选择与 PNG 下载
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'

const text = ref('')
const size = ref(256)
const canvasRef = ref(null)
const generated = ref(false)

async function generate() {
  if (!text.value.trim()) {
    ElMessage.warning('请输入文本或 URL')
    return
  }
  try {
    await QRCode.toCanvas(canvasRef.value, text.value, {
      width: size.value,
      margin: 2,
      errorCorrectionLevel: 'M',
    })
    generated.value = true
  } catch (e) {
    ElMessage.error(`生成失败：${e.message}`)
  }
}

function download() {
  if (!generated.value) return
  const url = canvasRef.value.toDataURL('image/png')
  const a = document.createElement('a')
  a.href = url
  a.download = `qrcode-${Date.now()}.png`
  a.click()
  ElMessage.success('已下载')
}

function fillSample() {
  text.value = 'https://example.com'
}
</script>

<template>
  <div class="qrcode-tool">
    <div class="row">
      <div class="col">
        <el-input
          v-model="text"
          type="textarea"
          :rows="4"
          placeholder="输入文本或 URL，如 https://example.com"
          clearable
        />
        <div class="opt-row">
          <span class="opt-label">尺寸</span>
          <el-select v-model="size" style="width: 110px">
            <el-option label="128px" :value="128" />
            <el-option label="256px" :value="256" />
            <el-option label="512px" :value="512" />
          </el-select>
          <el-button type="primary" @click="generate">生成二维码</el-button>
          <el-button @click="fillSample">示例</el-button>
        </div>
      </div>
      <div class="col center">
        <div class="qr-box">
          <canvas ref="canvasRef" />
          <el-empty v-if="!generated" description="输入内容后点击生成" :image-size="60" />
        </div>
        <el-button v-if="generated" class="download-btn" @click="download">
          下载 PNG
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.row {
  display: flex;
  gap: 24px;
}

.col {
  flex: 1;
  min-width: 0;
}

.center {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-box {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 260px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 12px;
}

.qr-box canvas {
  max-width: 100%;
}

.download-btn {
  margin-top: 12px;
}

.opt-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.opt-label {
  font-size: 13px;
  color: #606266;
}
</style>