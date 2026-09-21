<script setup>
/**
 * 环境管理（REQ-005 env:env:list）
 * 列表 / 新建 / 编辑 / 启用禁用 / 健康自检 / 删除
 * 按钮级权限：v-perm 指令（env:env:add/edit/delete/health）
 */
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createEnv,
  deleteEnv,
  healthCheck,
  listEnvs,
  updateEnv,
  updateEnvStatus,
} from '@/api/env'

/** 环境类型选项（对应 env_code：dev/test/prod） */
const envTypeOptions = [
  { value: 'dev', label: 'DEV 开发' },
  { value: 'test', label: 'TEST 测试' },
  { value: 'prod', label: 'PROD 生产' },
]

/** 列表状态 */
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '' })

/** 新建/编辑对话框 */
const dialogVisible = ref(false)
const dialogMode = ref('create') // create | edit
const formRef = ref()
const form = reactive({
  id: null,
  envCode: '',
  envName: '',
  baseUrl: '',
  description: '',
  status: 1,
})

const rules = {
  envCode: [{ required: true, message: '请选择环境类型', trigger: 'change' }],
  envName: [{ required: true, message: '请输入环境名称', trigger: 'blur' }],
}

/** 查询列表 */
async function fetchList() {
  loading.value = true
  try {
    const data = await listEnvs({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
    })
    tableData.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.keyword = ''
  query.page = 1
  fetchList()
}

/** 打开新建对话框 */
function openCreate() {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    envCode: '',
    envName: '',
    baseUrl: '',
    description: '',
    status: 1,
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 打开编辑对话框 */
function openEdit(row) {
  dialogMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    envCode: row.envCode,
    envName: row.envName,
    baseUrl: row.baseUrl || '',
    description: row.description || '',
    status: row.status,
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 提交新建/编辑 */
async function submitForm() {
  await formRef.value.validate()
  const payload = {
    envCode: form.envCode,
    envName: form.envName,
    baseUrl: form.baseUrl || null,
    description: form.description || null,
    status: form.status,
  }
  if (dialogMode.value === 'create') {
    await createEnv(payload)
    ElMessage.success('新建成功')
  } else {
    await updateEnv(form.id, payload)
    ElMessage.success('保存成功')
  }
  dialogVisible.value = false
  fetchList()
}

/** 启用/禁用 */
async function handleToggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  await updateEnvStatus(row.id, next)
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
  fetchList()
}

/** 健康自检：调 health-check 接口，按 UP/DOWN 提示结果 */
async function handleHealthCheck(row) {
  const data = await healthCheck(row.id)
  const latency = data.latencyMs != null ? `${data.latencyMs}ms` : '-'
  if (data.status === 'UP') {
    ElMessage.success(`健康自检通过：UP（${latency}）`)
  } else {
    ElMessage.error(`健康自检失败：DOWN（${latency}）${data.errorMsg ? '：' + data.errorMsg : ''}`)
  }
}

/** 删除环境 */
async function handleDelete(row) {
  await deleteEnv(row.id)
  ElMessage.success('删除成功')
  // 删掉当前页最后一条时回退一页，避免空页
  if (tableData.value.length === 1 && query.page > 1) {
    query.page -= 1
  }
  fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div class="env-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="环境名称/编码/描述"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-perm="'env:env:add'" type="primary" @click="openCreate">新建环境</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="envName" label="环境名称" min-width="120" />
        <el-table-column prop="envCode" label="编码" min-width="100" />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.envCode.toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'env:env:edit'" link type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-popconfirm
              :title="row.status === 1 ? '确认禁用该环境？' : '确认启用该环境？'"
              @confirm="handleToggleStatus(row)"
            >
              <template #reference>
                <el-button
                  v-perm="'env:env:edit'"
                  link
                  :type="row.status === 1 ? 'warning' : 'success'"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-popconfirm>
            <el-button v-perm="'env:env:health'" link type="primary" @click="handleHealthCheck(row)">
              健康自检
            </el-button>
            <el-popconfirm title="确认删除该环境？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button v-perm="'env:env:delete'" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        class="pager"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="fetchList"
      />
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新建环境' : '编辑环境'"
      width="520px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="环境类型" prop="envCode">
          <el-select
            v-model="form.envCode"
            :disabled="dialogMode === 'edit'"
            placeholder="选择环境类型"
            style="width: 100%"
          >
            <el-option
              v-for="opt in envTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="环境名称" prop="envName">
          <el-input v-model="form.envName" placeholder="如：开发环境" />
        </el-form-item>
        <el-form-item label="地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="被测系统基础地址，如 http://localhost:8081" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter-card {
  margin-bottom: 12px;
}

.toolbar {
  margin-bottom: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>