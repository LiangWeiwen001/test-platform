<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listRoles,
  createRole,
  updateRole,
  deleteRole,
  getPermissionTree,
  assignPermissions,
} from '@/api/role'

// ---- 列表状态 ----
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', status: null })

async function fetchData() {
  loading.value = true
  try {
    const res = await listRoles(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchData()
}

function handleSizeChange(val) {
  query.size = val
  query.page = 1
  fetchData()
}

function handleCurrentChange(val) {
  query.page = val
  fetchData()
}

// ---- 新建/编辑对话框 ----
const dialogVisible = ref(false)
const dialogTitle = ref('新建角色')
const isEdit = ref(false)
const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1,
})

function resetForm() {
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
}

function handleCreate() {
  resetForm()
  dialogTitle.value = '新建角色'
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(row) {
  form.id = row.id
  form.roleName = row.roleName
  form.roleCode = row.roleCode
  form.description = row.description
  form.status = row.status
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value) {
      await updateRole(form.id, { ...form })
      ElMessage.success('编辑成功')
    } else {
      await createRole({ ...form })
      ElMessage.success('新建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // request.js 已统一处理错误提示
  }
}

// ---- 删除 ----
async function handleDelete(row) {
  try {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // request.js 已统一处理
  }
}

// ---- 分配权限 ----
const permDialogVisible = ref(false)
const permLoading = ref(false)
const permTreeData = ref([])
const currentRoleId = ref(null)
const checkedKeys = ref([])

async function loadPermTree() {
  if (permTreeData.value.length > 0) return
  try {
    permTreeData.value = await getPermissionTree()
  } catch {
    // request.js 处理
  }
}

async function handleAssignPerm(row) {
  currentRoleId.value = row.id
  checkedKeys.value = row.permIds || []
  permLoading.value = true
  permDialogVisible.value = true
  await loadPermTree()
  permLoading.value = false
}

async function handlePermSubmit() {
  // 收集 checkedKeys + halfCheckedKeys
  const treeRef = document.querySelector('.perm-tree')
  // 使用 el-tree 的 getCheckedKeys / getHalfCheckedKeys
  // 通过 ref 获取——但由于动态，这里用 v-model 方式
  // 实际用 template ref
  if (!permTreeRef.value) return
  const checked = permTreeRef.value.getCheckedKeys()
  const halfChecked = permTreeRef.value.getHalfCheckedKeys()
  const allIds = [...checked, ...halfChecked]
  try {
    await assignPermissions(currentRoleId.value, allIds)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
    fetchData()
  } catch {
    // request.js 处理
  }
}

const permTreeRef = ref(null)

// ---- 格式化 ----
function formatTime(val) {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

// ---- 初始化 ----
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="role-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="角色名/编码"
            clearable
            @keyup.enter="handleSearch"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button v-perm="'system:role:add'" type="success" @click="handleCreate">新建角色</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="roleName" label="角色名称" min-width="120" />
        <el-table-column prop="roleCode" label="角色编码" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-perm="'system:role:edit'" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-perm="'system:role:assignPerm'" link type="warning" @click="handleAssignPerm(row)">分配权限</el-button>
            <el-popconfirm title="确定删除该角色？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button v-perm="'system:role:delete'" link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        :page-sizes="[10, 20, 50]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.roleCode" placeholder="如 admin、test" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="角色描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="500px" destroy-on-close>
      <div v-loading="permLoading" style="min-height: 200px;">
        <el-tree
          ref="permTreeRef"
          :data="permTreeData"
          :props="{ label: 'permName', children: 'children' }"
          node-key="id"
          show-checkbox
          :default-checked-keys="checkedKeys"
          :check-strictly="false"
        />
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.role-page {
  padding: 0;
}
.search-card {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
