<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="error-page">
    <el-result icon="info" title="404" sub-title="抱歉，你访问的页面不存在">
      <template #extra>
        <el-button type="primary" @click="router.push('/login')">返回登录</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>
