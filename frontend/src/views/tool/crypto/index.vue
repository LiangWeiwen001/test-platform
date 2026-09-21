<script setup>
/**
 * 加解密工具（REQ-004）：MD5 / SHA1 / SHA256 / Base64
 * - MD5：纯 JS 实现（RFC 1321，无第三方依赖）
 * - SHA1 / SHA256：Web Crypto API（crypto.subtle，异步）
 * - Base64：btoa / atob（Unicode 安全）
 * - AES 未实现：避免引入 CryptoJS 依赖，如需可后续注册
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const hashInput = ref('')
const hashAlgo = ref('md5')
const hashOutput = ref('')
const hashLoading = ref(false)

const b64Input = ref('')
const b64Output = ref('')

/* ---------------- MD5（RFC 1321 纯 JS 实现） ---------------- */

function utf8Encode(str) {
  return unescape(encodeURIComponent(str))
}

function md5(string) {
  function RotateLeft(lValue, iShiftBits) {
    return (lValue << iShiftBits) | (lValue >>> (32 - iShiftBits))
  }
  function AddUnsigned(lX, lY) {
    const lX8 = lX & 0x80000000
    const lY8 = lY & 0x80000000
    const lX4 = lX & 0x40000000
    const lY4 = lY & 0x40000000
    let lResult = (lX & 0x3fffffff) + (lY & 0x3fffffff)
    if (lX4 & lY4) return lResult ^ 0x80000000 ^ lX8 ^ lY8
    if (lX4 | lY4) {
      if (lResult & 0x40000000) return lResult ^ 0xc0000000 ^ lX8 ^ lY8
      return lResult ^ 0x40000000 ^ lX8 ^ lY8
    }
    return lResult ^ lX8 ^ lY8
  }
  const F = (x, y, z) => (x & y) | (~x & z)
  const G = (x, y, z) => (x & z) | (y & ~z)
  const H = (x, y, z) => x ^ y ^ z
  const I = (x, y, z) => y ^ (x | ~z)
  const FF = (a, b, c, d, x, s, ac) => AddUnsigned(RotateLeft(AddUnsigned(a, AddUnsigned(AddUnsigned(F(b, c, d), x), ac)), s), b)
  const GG = (a, b, c, d, x, s, ac) => AddUnsigned(RotateLeft(AddUnsigned(a, AddUnsigned(AddUnsigned(G(b, c, d), x), ac)), s), b)
  const HH = (a, b, c, d, x, s, ac) => AddUnsigned(RotateLeft(AddUnsigned(a, AddUnsigned(AddUnsigned(H(b, c, d), x), ac)), s), b)
  const II = (a, b, c, d, x, s, ac) => AddUnsigned(RotateLeft(AddUnsigned(a, AddUnsigned(AddUnsigned(I(b, c, d), x), ac)), s), b)

  function ConvertToWordArray(str) {
    const lMessageLength = str.length
    const lNumberOfWords_temp1 = lMessageLength + 8
    const lNumberOfWords_temp2 = (lNumberOfWords_temp1 - (lNumberOfWords_temp1 % 64)) / 64
    const lNumberOfWords = (lNumberOfWords_temp2 + 1) * 16
    const lWordArray = new Array(lNumberOfWords - 1)
    let lBytePosition = 0
    let lByteCount = 0
    while (lByteCount < lMessageLength) {
      const lWordCount = (lByteCount - (lByteCount % 4)) / 4
      lBytePosition = (lByteCount % 4) * 8
      lWordArray[lWordCount] = lWordArray[lWordCount] | (str.charCodeAt(lByteCount) << lBytePosition)
      lByteCount++
    }
    const lWordCount = (lByteCount - (lByteCount % 4)) / 4
    lBytePosition = (lByteCount % 4) * 8
    lWordArray[lWordCount] = lWordArray[lWordCount] | (0x80 << lBytePosition)
    lWordArray[lNumberOfWords - 2] = lMessageLength << 3
    lWordArray[lNumberOfWords - 1] = lMessageLength >>> 29
    return lWordArray
  }

  function WordToHex(lValue) {
    let WordToHexValue = ''
    for (let lCount = 0; lCount <= 3; lCount++) {
      const lByte = (lValue >>> (lCount * 8)) & 255
      const temp = '0' + lByte.toString(16)
      WordToHexValue += temp.substr(temp.length - 2, 2)
    }
    return WordToHexValue
  }

  const S11 = 7, S12 = 12, S13 = 17, S14 = 22
  const S21 = 5, S22 = 9, S23 = 14, S24 = 20
  const S31 = 4, S32 = 11, S33 = 16, S34 = 23
  const S41 = 6, S42 = 10, S43 = 15, S44 = 21

  const x = ConvertToWordArray(utf8Encode(string))
  let a = 0x67452301
  let b = 0xefcdab89
  let c = 0x98badcfe
  let d = 0x10325476

  for (let k = 0; k < x.length; k += 16) {
    const AA = a, BB = b, CC = c, DD = d
    a = FF(a, b, c, d, x[k + 0], S11, 0xd76aa478)
    d = FF(d, a, b, c, x[k + 1], S12, 0xe8c7b756)
    c = FF(c, d, a, b, x[k + 2], S13, 0x242070db)
    b = FF(b, c, d, a, x[k + 3], S14, 0xc1bdceee)
    a = FF(a, b, c, d, x[k + 4], S11, 0xf57c0faf)
    d = FF(d, a, b, c, x[k + 5], S12, 0x4787c62a)
    c = FF(c, d, a, b, x[k + 6], S13, 0xa8304613)
    b = FF(b, c, d, a, x[k + 7], S14, 0xfd469501)
    a = FF(a, b, c, d, x[k + 8], S11, 0x698098d8)
    d = FF(d, a, b, c, x[k + 9], S12, 0x8b44f7af)
    c = FF(c, d, a, b, x[k + 10], S13, 0xffff5bb1)
    b = FF(b, c, d, a, x[k + 11], S14, 0x895cd7be)
    a = FF(a, b, c, d, x[k + 12], S11, 0x6b901122)
    d = FF(d, a, b, c, x[k + 13], S12, 0xfd987193)
    c = FF(c, d, a, b, x[k + 14], S13, 0xa679438e)
    b = FF(b, c, d, a, x[k + 15], S14, 0x49b40821)
    a = GG(a, b, c, d, x[k + 1], S21, 0xf61e2562)
    d = GG(d, a, b, c, x[k + 6], S22, 0xc040b340)
    c = GG(c, d, a, b, x[k + 11], S23, 0x265e5a51)
    b = GG(b, c, d, a, x[k + 0], S24, 0xe9b6c7aa)
    a = GG(a, b, c, d, x[k + 5], S21, 0xd62f105d)
    d = GG(d, a, b, c, x[k + 10], S22, 0x2441453)
    c = GG(c, d, a, b, x[k + 15], S23, 0xd8a1e681)
    b = GG(b, c, d, a, x[k + 4], S24, 0xe7d3fbc8)
    a = GG(a, b, c, d, x[k + 9], S21, 0x21e1cde6)
    d = GG(d, a, b, c, x[k + 14], S22, 0xc33707d6)
    c = GG(c, d, a, b, x[k + 3], S23, 0xf4d50d87)
    b = GG(b, c, d, a, x[k + 8], S24, 0x455a14ed)
    a = GG(a, b, c, d, x[k + 13], S21, 0xa9e3e905)
    d = GG(d, a, b, c, x[k + 2], S22, 0xfcefa3f8)
    c = GG(c, d, a, b, x[k + 7], S23, 0x676f02d9)
    b = GG(b, c, d, a, x[k + 12], S24, 0x8d2a4c8a)
    a = HH(a, b, c, d, x[k + 5], S31, 0xfffa3942)
    d = HH(d, a, b, c, x[k + 8], S32, 0x8771f681)
    c = HH(c, d, a, b, x[k + 11], S33, 0x6d9d6122)
    b = HH(b, c, d, a, x[k + 14], S34, 0xfde5380c)
    a = HH(a, b, c, d, x[k + 1], S31, 0xa4beea44)
    d = HH(d, a, b, c, x[k + 4], S32, 0x4bdecfa9)
    c = HH(c, d, a, b, x[k + 7], S33, 0xf6bb4b60)
    b = HH(b, c, d, a, x[k + 10], S34, 0xbebfbc70)
    a = HH(a, b, c, d, x[k + 13], S31, 0x289b7ec6)
    d = HH(d, a, b, c, x[k + 0], S32, 0xeaa127fa)
    c = HH(c, d, a, b, x[k + 3], S33, 0xd4ef3085)
    b = HH(b, c, d, a, x[k + 6], S34, 0x4881d05)
    a = HH(a, b, c, d, x[k + 9], S31, 0xd9d4d039)
    d = HH(d, a, b, c, x[k + 12], S32, 0xe6db99e5)
    c = HH(c, d, a, b, x[k + 15], S33, 0x1fa27cf8)
    b = HH(b, c, d, a, x[k + 2], S34, 0xc4ac5665)
    a = II(a, b, c, d, x[k + 0], S41, 0xf4292244)
    d = II(d, a, b, c, x[k + 7], S42, 0x432aff97)
    c = II(c, d, a, b, x[k + 14], S43, 0xab9423a7)
    b = II(b, c, d, a, x[k + 5], S44, 0xfc93a039)
    a = II(a, b, c, d, x[k + 12], S41, 0x655b59c3)
    d = II(d, a, b, c, x[k + 3], S42, 0x8f0ccc92)
    c = II(c, d, a, b, x[k + 10], S43, 0xffeff47d)
    b = II(b, c, d, a, x[k + 1], S44, 0x85845dd1)
    a = II(a, b, c, d, x[k + 8], S41, 0x6fa87e4f)
    d = II(d, a, b, c, x[k + 15], S42, 0xfe2ce6e0)
    c = II(c, d, a, b, x[k + 6], S43, 0xa3014314)
    b = II(b, c, d, a, x[k + 13], S44, 0x4e0811a1)
    a = II(a, b, c, d, x[k + 4], S41, 0xf7537e82)
    d = II(d, a, b, c, x[k + 11], S42, 0xbd3af235)
    c = II(c, d, a, b, x[k + 2], S43, 0x2ad7d2bb)
    b = II(b, c, d, a, x[k + 9], S44, 0xeb86d391)
    a = AddUnsigned(a, AA)
    b = AddUnsigned(b, BB)
    c = AddUnsigned(c, CC)
    d = AddUnsigned(d, DD)
  }
  return (WordToHex(a) + WordToHex(b) + WordToHex(c) + WordToHex(d)).toLowerCase()
}

/* ---------------- SHA（Web Crypto API） ---------------- */

async function shaDigest(algorithm, text) {
  const data = new TextEncoder().encode(text)
  const buf = await crypto.subtle.digest(algorithm, data)
  return Array.from(new Uint8Array(buf))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
}

/* ---------------- 页面逻辑 ---------------- */

async function calcHash() {
  if (!hashInput.value) {
    ElMessage.warning('请输入待计算文本')
    return
  }
  hashLoading.value = true
  try {
    if (hashAlgo.value === 'md5') {
      hashOutput.value = md5(hashInput.value)
    } else {
      const algo = hashAlgo.value === 'sha1' ? 'SHA-1' : 'SHA-256'
      hashOutput.value = await shaDigest(algo, hashInput.value)
    }
  } catch (e) {
    ElMessage.error(`计算失败：${e.message}`)
  } finally {
    hashLoading.value = false
  }
}

/** Base64 编码（Unicode 安全） */
function b64Encode() {
  if (!b64Input.value) {
    ElMessage.warning('请输入待编码文本')
    return
  }
  try {
    b64Output.value = btoa(unescape(encodeURIComponent(b64Input.value)))
  } catch (e) {
    ElMessage.error(`编码失败：${e.message}`)
  }
}

/** Base64 解码（Unicode 安全） */
function b64Decode() {
  if (!b64Input.value) {
    ElMessage.warning('请输入待解码内容')
    return
  }
  try {
    b64Output.value = decodeURIComponent(escape(atob(b64Input.value.trim())))
  } catch (e) {
    ElMessage.error(`解码失败：Base64 内容无效（${e.message}）`)
  }
}

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
  <div class="crypto-tool">
    <!-- 哈希计算 -->
    <el-card shadow="never" class="section">
      <template #header>
        <div class="section-title">哈希计算（MD5 / SHA1 / SHA256）</div>
      </template>
      <div class="form-row">
        <el-select v-model="hashAlgo" style="width: 140px">
          <el-option label="MD5" value="md5" />
          <el-option label="SHA1" value="sha1" />
          <el-option label="SHA256" value="sha256" />
        </el-select>
        <el-input
          v-model="hashInput"
          placeholder="输入待计算文本（支持中文）"
          clearable
          @keyup.enter="calcHash"
        />
        <el-button type="primary" :loading="hashLoading" @click="calcHash">计算</el-button>
      </div>
      <div class="output-row">
        <el-input v-model="hashOutput" readonly placeholder="哈希结果" class="mono" />
        <el-button @click="copyText(hashOutput)">复制</el-button>
      </div>
    </el-card>

    <!-- Base64 编解码 -->
    <el-card shadow="never" class="section">
      <template #header>
        <div class="section-title">Base64 编解码</div>
      </template>
      <el-input
        v-model="b64Input"
        type="textarea"
        :rows="4"
        placeholder="输入文本或 Base64 内容（支持中文）"
        class="mono"
      />
      <div class="actions">
        <el-button type="primary" @click="b64Encode">编码 → Base64</el-button>
        <el-button @click="b64Decode">解码 ← 原文</el-button>
      </div>
      <div class="output-row">
        <el-input v-model="b64Output" readonly type="textarea" :rows="4" placeholder="结果" class="mono" />
        <el-button @click="copyText(b64Output)">复制</el-button>
      </div>
    </el-card>

    <el-alert
      type="info"
      :closable="false"
      title="说明：AES 对称加解密未内置（避免引入 CryptoJS 依赖）；SHA1/SHA256 由浏览器 Web Crypto API 计算，MD5 为纯 JS 实现。"
    />
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

.form-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.output-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.actions {
  margin: 10px 0;
}

.mono :deep(textarea),
.mono :deep(input) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
</style>