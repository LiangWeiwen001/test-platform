<script setup>
/**
 * 工具页容器（REQ-004）：按路由参数 /tool/:key 从注册表查找工具并渲染
 * - 未注册的 key → 跳 404
 * - 标题 / 说明来自注册表条目，页面组件渲染在卡片 body
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { findTool } from '@/utils/tools'

const route = useRoute()
const router = useRouter()

const entry = computed(() => findTool(route.params.key))

// 未注册的工具 key → 404
if (!entry.value) {
  router.replace('/404')
}
</script>

<template>
  <div v-if="entry" class="tool-page">
    <el-card shadow="never">
      <template #header>
        <div class="tool-header">
          <span class="tool-title">{{ entry.name }}</span>
          <span class="tool-desc">{{ entry.description }}</span>
        </div>
      </template>
      <component :is="entry.component" />
    </el-card>
  </div>
</template>

<style scoped>
.tool-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.tool-title {
  font-size: 16px;
  font-weight: 600;
}

.tool-desc {
  font-size: 13px;
  color: #909399;
}
</style>