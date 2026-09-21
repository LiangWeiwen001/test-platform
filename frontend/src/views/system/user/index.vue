<script setup>
/**
 * 用户管理（REQ-003 system:user:list）
 * 列表 / 新建 / 编辑 / 启用禁用 / 重置密码 / 删除
 * 按钮级权限：v-perm 指令（system:user:add/edit/delete/resetPwd）
 */
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createUser,
  deleteUser,
  listUsers,
  resetPassword,
  updateUser,
  updateUserStatus,
} from '@/api/user'
import { getAllRoles } from '@/api/role'

/** 列表状态 */
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', status: null })

/** 角色下拉选项（复用 TASK-006 的 roles/all，失败则空数组不阻塞） */
const roleOptions = ref([])

/** 新建/编辑对话框 */
const dialogVisible = ref(false)
const dialogMode = ref('create') // create | edit
const formRef = ref()
const form = reactive({
  id: null,
  username: '',
  nickname: '',
  password: '',
  email: '',
  phone: '',
  status: 1,
  roleIds: [],
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度需在 6-50 之间', trigger: 'blur' },
  ],
}

/** 重置密码对话框 */
const pwdVisible = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({ id: null, username: '', password: '' })
const pwdRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度需在 6-50 之间', trigger: 'blur' },
  ],
}

/** 查询列表 */
async function fetchList() {
  loading.value = true
  try {
    const data = await listUsers({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      status: query.status === null || query.status === '' ? undefined : query.status,
    })
    tableData.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 拉取角色下拉（roles/all 由 TASK-006 提供） */
async function fetchRoles() {
  try {
    roleOptions.value = await getAllRoles()
  } catch (e) {
    roleOptions.value = []
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.keyword = ''
  query.status = null
  query.page = 1
  fetchList()
}

/** 打开新建对话框 */
function openCreate() {
  dialogMode.value = 'create'
  Object.assign(form, {
    id: null,
    username: '',
    nickname: '',
    password: '',
    email: '',
    phone: '',
    status: 1,
    roleIds: [],
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 打开编辑对话框 */
function openEdit(row) {
  dialogMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    password: '',
    email: row.email || '',
    phone: row.phone || '',
    status: row.status,
    roleIds: row.roleIds || [],
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

/** 提交新建/编辑 */
async function submitForm() {
  await formRef.value.validate()
  if (dialogMode.value === 'create') {
    await createUser({
      username: form.username,
      nickname: form.nickname,
      password: form.password,
      email: form.email || null,
      phone: form.phone || null,
      status: form.status,
      roleIds: form.roleIds,
    })
    ElMessage.success('新建成功')
  } else {
    await updateUser(form.id, {
      nickname: form.nickname,
      email: form.email || null,
      phone: form.phone || null,
      status: form.status,
      roleIds: form.roleIds,
    })
    ElMessage.success('保存成功')
  }
  dialogVisible.value = false
  fetchList()
}

/** 启用/禁用 */
async function handleToggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  await updateUserStatus(row.id, next)
  ElMessage.success(next === 1 ? '已启用' : '已禁用')
  fetchList()
}

/** 打开重置密码对话框 */
function openResetPwd(row) {
  Object.assign(pwdForm, { id: row.id, username: row.username, password: '' })
  pwdVisible.value = true
  nextTick(() => pwdFormRef.value?.clearValidate())
}

/** 提交重置密码 */
async function submitResetPwd() {
  await pwdFormRef.value.validate()
  await resetPassword(pwdForm.id, pwdForm.password)
  ElMessage.success('密码重置成功')
  pwdVisible.value = false
}

/** 删除用户 */
async function handleDelete(row) {
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  // 删掉当前页最后一条时回退一页，避免空页
  if (tableData.value.length === 1 && query.page > 1) {
    query.page -= 1
  }
  fetchList()
}

onMounted(() => {
  fetchRoles()
  fetchList()
})
</script>

<template>
  <div class="user-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="用户名/昵称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="toolbar">
        <el-button v-perm="'system:user:add'" type="primary" @click="openCreate">新建用户</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="170" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="name in row.roleNames" :key="name" size="small" class="role-tag">
              {{ name }}
            </el-tag>
            <span v-if="!row.roleNames || !row.roleNames.length">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'system:user:edit'" link type="primary" @click="openEdit(row)">
              编辑
            </el-button>
            <el-popconfirm
              :title="row.status === 1 ? '确认禁用该用户？' : '确认启用该用户？'"
              @confirm="handleToggleStatus(row)"
            >
              <template #reference>
                <el-button
                  v-perm="'system:user:edit'"
                  link
                  :type="row.status === 1 ? 'warning' : 'success'"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-popconfirm>
            <el-button v-perm="'system:user:resetPwd'" link type="primary" @click="openResetPwd(row)">
              重置密码
            </el-button>
            <el-popconfirm title="确认删除该用户？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button v-perm="'system:user:delete'" link type="danger">删除</el-button>
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
      :title="dialogMode === 'create' ? '新建用户' : '编辑用户'"
      width="520px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="dialogMode === 'edit'" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="昵称/姓名" />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'create'" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="初始密码（6-50位）" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="选择角色" style="width: 100%">
            <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="pwdVisible" title="重置密码" width="420px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="用户">
          <span>{{ pwdForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="pwdForm.password" type="password" show-password placeholder="6-50位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确定</el-button>
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

.role-tag {
  margin-right: 4px;
}
</style>
