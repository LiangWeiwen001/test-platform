<script setup>
/**
 * 主布局：侧边动态菜单（按权限码过滤）+ 顶栏 + 内容区
 * 菜单权限码：dashboard:view / tool:view / env:env:list / case:case:list / system:menu
 * （系统管理子菜单：system:user:list / system:role:list / system:log:list）
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { hasPerm } from '@/utils/permission'

const route = useRoute()
const userStore = useUserStore()

/** 菜单配置：path + 标题 + 所需权限码 */
const menuConfig = [
  { path: '/dashboard', title: '工作台', perm: 'dashboard:view' },
  { path: '/tool', title: '工具菜单', perm: 'tool:view' },
  { path: '/env', title: '环境管理', perm: 'env:env:list' },
  { path: '/case', title: '用例管理', perm: 'case:case:list' },
  {
    title: '系统管理',
    perm: 'system:menu',
    children: [
      { path: '/system/user', title: '用户管理', perm: 'system:user:list' },
      { path: '/system/role', title: '角色权限', perm: 'system:role:list' },
      { path: '/system/log', title: '操作日志', perm: 'system:log:list' },
    ],
  },
]

/** 按权限码过滤后的菜单 */
const visibleMenus = computed(() =>
  menuConfig
    .map((item) => {
      if (item.children) {
        const children = item.children.filter((child) => hasPerm(child.perm))
        if (children.length || hasPerm(item.perm)) {
          return { ...item, children }
        }
        return null
      }
      return hasPerm(item.perm) ? item : null
    })
    .filter(Boolean),
)
</script>

<template>
  <el-container class="main-layout">
    <el-aside width="200px" class="layout-aside">
      <div class="logo">测试平台</div>
      <el-menu :default-active="route.path" router background-color="#1f2d3d" text-color="#c0c4cc">
        <template v-for="item in visibleMenus" :key="item.path || item.title">
          <el-sub-menu v-if="item.children" :index="item.title">
            <template #title>{{ item.title }}</template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              {{ child.title }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">{{ item.title }}</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <span class="app-name">测试平台</span>
        <span class="user-area">
          <span class="nickname">{{ userStore.nickname }}</span>
          <el-button link type="primary" @click="userStore.logout()">退出登录</el-button>
        </span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.main-layout {
  height: 100%;
}

.layout-aside {
  background-color: #1f2d3d;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-weight: 600;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.app-name {
  font-weight: 600;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nickname {
  color: #606266;
}
</style>
