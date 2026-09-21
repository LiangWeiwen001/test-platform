<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

// 骨架：完整登录逻辑（调用 auth API、存 token、跳转）由 TASK-004 实现
const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    loading.value = true
    // TODO(TASK-004)：调用 login API 保存 token 并跳转首页
    ElMessage.info('登录功能将在 TASK-004 接入')
    loading.value = false
  })
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="login-title">测试平台</div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #1f2d3d 0%, #3a4a5e 100%);
}

.login-card {
  width: 360px;
}

.login-title {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
}

.login-btn {
  width: 100%;
}
</style>
