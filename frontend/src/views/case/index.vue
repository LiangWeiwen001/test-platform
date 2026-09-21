<template>
  <div class="case-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="mb-16">
      <el-form :inline="true" :model="query" @submit.prevent="handleSearch">
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="用例名称/描述"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.caseType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="t in caseTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-select v-model="query.caseLevel" placeholder="全部" clearable style="width: 110px">
            <el-option v-for="l in caseLevelOptions" :key="l.value" :label="l.label" :value="l.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="query.tagId" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="t in tags" :key="t.id" :label="t.tagName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-perm="'case:case:add'" type="primary" @click="openCreate">新建用例</el-button>
        <el-button v-perm="'case:tag:add'" @click="openTagDialog">标签管理</el-button>
      </div>

      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="caseName" label="用例名称" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTagType(row.caseType)">{{ typeLabel(row.caseType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="levelTagType(row.caseLevel)" effect="plain">{{ row.caseLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="140">
          <template #default="{ row }">
            <el-tag
              v-for="name in row.tagNames"
              :key="name"
              size="small"
              type="info"
              effect="light"
              class="mr-4"
            >{{ name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'case:case:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-perm="'case:case:edit'"
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >{{ row.status === 1 ? '停用' : '启用' }}</el-button>
            <el-popconfirm
              :title="`确定删除用例「${row.caseName}」？`"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button v-perm="'case:case:delete'" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchList"
        />
      </div>
    </el-card>

    <!-- 新建/编辑用例对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用例' : '新建用例'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用例名称" prop="caseName">
          <el-input v-model="form.caseName" placeholder="请输入用例名称" maxlength="200" />
        </el-form-item>
        <el-form-item label="用例类型" prop="caseType">
          <el-select v-model="form.caseType" style="width: 100%">
            <el-option v-for="t in caseTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="caseLevel">
          <el-select v-model="form.caseLevel" style="width: 100%">
            <el-option v-for="l in caseLevelOptions" :key="l.value" :label="l.label" :value="l.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tagIdList" multiple clearable placeholder="选择标签" style="width: 100%">
            <el-option v-for="t in tags" :key="t.id" :label="t.tagName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="用例描述（可选）" maxlength="1000" />
        </el-form-item>
        <el-form-item label="请求配置">
          <el-input v-model="form.requestConfig" type="textarea" :rows="3" placeholder="JSON 格式请求配置（可选）" />
        </el-form-item>
        <el-form-item label="预期结果">
          <el-input v-model="form.expectedResult" type="textarea" :rows="3" placeholder="JSON 格式预期结果（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 标签管理对话框 -->
    <el-dialog v-model="tagDialogVisible" title="标签管理" width="460px">
      <div class="tag-create-row">
        <el-input v-model="newTagName" placeholder="新标签名称" maxlength="50" style="width: 200px" />
        <el-color-picker v-model="newTagColor" />
        <el-button type="primary" :disabled="!newTagName.trim()" @click="handleCreateTag">新建</el-button>
      </div>
      <el-table :data="tags" size="small" style="margin-top: 12px">
        <el-table-column label="标签" min-width="160">
          <template #default="{ row }">
            <el-tag :color="row.tagColor" effect="dark" style="border: none">{{ row.tagName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="caseCount" label="用例数" width="80" align="center" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该标签？将同时解除用例关联" @confirm="handleDeleteTag(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listCases,
  createCase,
  updateCase,
  deleteCase,
  updateCaseStatus,
  listCaseTags,
  createCaseTag,
  deleteCaseTag,
} from '@/api/case'

const caseTypeOptions = [
  { value: 'API', label: '接口' },
  { value: 'PERF', label: '性能' },
  { value: 'DB', label: '数据库' },
  { value: 'COMMON', label: '通用' },
]
const caseLevelOptions = [
  { value: 'P0', label: 'P0' },
  { value: 'P1', label: 'P1' },
  { value: 'P2', label: 'P2' },
]

function typeLabel(t) {
  return caseTypeOptions.find((o) => o.value === t)?.label || t
}
function typeTagType(t) {
  return { API: 'primary', PERF: 'warning', DB: 'success', COMMON: 'info' }[t] || 'info'
}
function levelTagType(l) {
  return { P0: 'danger', P1: 'warning', P2: 'info' }[l] || 'info'
}

const loading = ref(false)
const records = ref([])
const total = ref(0)
const tags = ref([])
const query = reactive({ page: 1, size: 10, keyword: '', caseType: '', caseLevel: '', tagId: '' })

const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  caseName: '',
  caseType: 'API',
  caseLevel: 'P1',
  description: '',
  requestConfig: '',
  expectedResult: '',
  tagIdList: [],
})
const rules = {
  caseName: [{ required: true, message: '请输入用例名称', trigger: 'blur' }],
  caseType: [{ required: true, message: '请选择用例类型', trigger: 'change' }],
  caseLevel: [{ required: true, message: '请选择优先级', trigger: 'change' }],
}

const tagDialogVisible = ref(false)
const newTagName = ref('')
const newTagColor = ref('#409EFF')

async function fetchList() {
  loading.value = true
  try {
    const res = await listCases({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      caseType: query.caseType || undefined,
      caseLevel: query.caseLevel || undefined,
      tagId: query.tagId || undefined,
    })
    records.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function fetchTags() {
  const res = await listCaseTags()
  tags.value = res.data || []
}

function handleSearch() {
  query.page = 1
  fetchList()
}
function handleReset() {
  query.keyword = ''
  query.caseType = ''
  query.caseLevel = ''
  query.tagId = ''
  query.page = 1
  fetchList()
}

function openCreate() {
  Object.assign(form, {
    id: null,
    caseName: '',
    caseType: 'API',
    caseLevel: 'P1',
    description: '',
    requestConfig: '',
    expectedResult: '',
    tagIdList: [],
  })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    caseName: row.caseName,
    caseType: row.caseType,
    caseLevel: row.caseLevel,
    description: row.description || '',
    requestConfig: row.requestConfig || '',
    expectedResult: row.expectedResult || '',
    tagIdList: row.tagIds ? row.tagIds.split(',').map(Number) : [],
  })
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = {
      caseName: form.caseName,
      caseType: form.caseType,
      caseLevel: form.caseLevel,
      description: form.description,
      requestConfig: form.requestConfig,
      expectedResult: form.expectedResult,
      tagIds: form.tagIdList.join(','),
    }
    if (form.id) {
      await updateCase(form.id, payload)
      ElMessage.success('用例已更新，版本自动递增')
    } else {
      await createCase({ ...payload, status: 1 })
      ElMessage.success('用例创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row) {
  await updateCaseStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success(row.status === 1 ? '已停用' : '已启用')
  fetchList()
}

async function handleDelete(row) {
  await deleteCase(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

function openTagDialog() {
  fetchTags()
  newTagName.value = ''
  newTagColor.value = '#409EFF'
  tagDialogVisible.value = true
}

async function handleCreateTag() {
  await createCaseTag({ tagName: newTagName.value.trim(), tagColor: newTagColor.value })
  ElMessage.success('标签创建成功')
  newTagName.value = ''
  fetchTags()
}

async function handleDeleteTag(row) {
  await deleteCaseTag(row.id)
  ElMessage.success('标签已删除')
  fetchTags()
}

onMounted(() => {
  fetchList()
  fetchTags()
})
</script>

<style scoped>
.mb-16 {
  margin-bottom: 16px;
}
.mr-4 {
  margin-right: 4px;
}
.toolbar {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.tag-create-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>