<script setup>
// 工作台（REQ-007）：统计卡片 / 近7日趋势 / 环境状态 / 操作动态
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOverview, getTrend, getEnvStatus, getActivities } from '@/api/dashboard'

const loading = ref(false)
const overview = ref({ userCount: 0, envCount: 0, caseCount: 0, healthUpCount: 0 })
const trend = ref({ dates: [], caseCounts: [], healthChecks: [] })
const envStatusList = ref([])
const activities = ref([])

// 环境类型标签色
const envTypeTagMap = {
  DEV: 'success',
  TEST: 'warning',
  PROD: 'danger',
  OTHER: 'info',
}

async function load() {
  loading.value = true
  try {
    const [ov, tr, es, ac] = await Promise.all([
      getOverview(),
      getTrend(),
      getEnvStatus(),
      getActivities(),
    ])
    overview.value = ov.data || {}
    trend.value = tr.data || {}
    envStatusList.value = es.data || []
    activities.value = ac.data || []
  } catch (e) {
    ElMessage.error('工作台数据加载失败')
  } finally {
    loading.value = false
  }
}

// 统计卡片配置
const statCards = [
  { label: '用户总数', key: 'userCount', icon: 'User', color: '#409EFF' },
  { label: '环境总数', key: 'envCount', icon: 'Connection', color: '#67C23A' },
  { label: '用例总数', key: 'caseCount', icon: 'Document', color: '#E6A23C' },
  { label: '健康环境', key: 'healthUpCount', icon: 'CircleCheck', color: '#F56C6C' },
]

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col v-for="card in statCards" :key="card.key" :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <el-icon :size="36" :color="card.color" class="stat-icon">
              <component :is="card.icon" />
            </el-icon>
            <div class="stat-meta">
              <div class="stat-num">{{ overview[card.key] ?? 0 }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近 7 日趋势 + 操作动态 -->
    <el-row :gutter="16" class="mid-row">
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header>
            <span>近 7 日趋势</span>
          </template>
          <el-table :data="trendDates" size="small" stripe>
            <el-table-column prop="date" label="日期" width="140" />
            <el-table-column prop="cases" label="新增用例" align="center" />
            <el-table-column prop="checks" label="健康检查" align="center" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header>
            <span>操作动态</span>
          </template>
          <el-table :data="activities" size="small" stripe>
            <el-table-column prop="username" label="用户" width="90" />
            <el-table-column prop="operation" label="操作" show-overflow-tooltip />
            <el-table-column prop="result" label="结果" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.result === 'success' ? 'success' : 'danger'" size="small">
                  {{ row.result === 'success' ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 环境状态 -->
    <el-card shadow="never" class="env-card">
      <template #header>
        <span>环境状态</span>
      </template>
      <el-table :data="envStatusList" size="small" stripe>
        <el-table-column prop="envName" label="环境名称" min-width="120" />
        <el-table-column prop="envCode" label="编码" width="100" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="envTypeTagMap[row.envType] || 'info'" size="small">{{ row.envType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="地址" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="健康" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.healthStatus === 'UP' ? 'success' : row.healthStatus === 'DOWN' ? 'danger' : 'info'"
              size="small"
            >
              {{ row.healthStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="latencyMs" label="延迟(ms)" width="90" align="center">
          <template #default="{ row }">{{ row.latencyMs ?? '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
// 计算属性：把 dates/caseCounts/healthChecks 合并为行数据（放第二块 script 避免冲突）
export default {
  computed: {
    trendDates() {
      const { dates = [], caseCounts = [], healthChecks = [] } = this.trend || {}
      return dates.map((d, i) => ({
        date: d,
        cases: caseCounts[i] ?? 0,
        checks: healthChecks[i] ?? 0,
      }))
    },
  },
}
</script>

<style scoped>
.dashboard {
  padding: 4px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon {
  flex-shrink: 0;
}
.stat-num {
  font-size: 26px;
  font-weight: 600;
  line-height: 1.2;
}
.stat-label {
  color: #909399;
  font-size: 13px;
}
.mid-row {
  margin-bottom: 16px;
}
.env-card {
  margin-bottom: 8px;
}
</style>